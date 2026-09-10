/*
 * This file is part of the Quadra Gen project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Fallen_Breath and contributors
 *
 * Quadra Gen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quadra Gen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Quadra Gen.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.fallenbreath.quadragen.mixins.worldgen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.compat.ChunkPosCompat;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * mc >= 26.3: subproject 26.3                    <--------
 * mc <= 26.2: subproject 26.2 (main project)
 * <p>
 * 26.3 combines Noise fill, surface, and carvers into
 * {@link NoiseBasedChunkGenerator#buildTerrain}; biome generation remains a separate status owned by the base generator.
 */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin
{
	@Inject(
			method = "buildTerrain(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/biome/BiomeManager;Lnet/minecraft/server/level/WorldGenRegion;Ljava/util/Set;)Ljava/util/concurrent/CompletableFuture;",
			at = @At("HEAD"),
			cancellable = true
	)
	private void buildTerrain(
			CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir,
			@Local(argsOnly = true) ChunkAccess chunk,
			@Local(argsOnly = true) Blender blender,
			@Local(argsOnly = true) RandomState randomState,
			@Local(argsOnly = true) StructureManager structureManager,
			@Local(argsOnly = true) BiomeManager biomeManager,
			@Local(argsOnly = true) WorldGenRegion carverBiomeRegion,
			@Local(argsOnly = true) Set<Holder<Biome>> possibleBiomes)
	{
		LevelContext context = this.getContext$quadragen();
		if (context != null && !this.isUpgrading$quadragen(chunk))
		{
			QuadrantPlan plan = context.getPlanAt(chunk.getPos());
			if (!plan.isOrdinaryNoise())
			{
				if (plan.isFlat() && !plan.isClearGeneratedContent())
				{
					cir.setReturnValue(plan.getFlat().getFlatGenerator().buildTerrain(
							chunk,
							blender,
							randomState,
							structureManager,
							biomeManager,
							carverBiomeRegion,
							possibleBiomes
					));
				}
				else
				{
					cir.setReturnValue(CompletableFuture.completedFuture(chunk));
				}
			}
		}
	}

	@ModifyExpressionValue(
			method = "generateCarvers",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/carver/WorldCarver;isStartChunk(Lnet/minecraft/util/RandomSource;)Z")
	)
	private boolean filterCarverSource(
			boolean isStartChunk,
			@Local(argsOnly = true) ChunkAccess chunk,
			@Local(ordinal = 1) ChunkPos sourcePos)
	{
		if (!isStartChunk || this.isUpgrading$quadragen(chunk))
		{
			return isStartChunk;
		}
		LevelContext context = this.getContext$quadragen();
		return context == null || context.getPlanAt(sourcePos).isOrdinaryNoise();
	}

	@Inject(method = "spawnOriginalMobs", at = @At("HEAD"), cancellable = true)
	private void spawnOriginalMobs(WorldGenRegion region, CallbackInfo ci)
	{
		LevelContext context = this.getContext$quadragen();
		if (context == null)
		{
			return;
		}
		ChunkAccess chunk = region.getChunk(ChunkPosCompat.x(region.getCenter()), ChunkPosCompat.z(region.getCenter()));
		if (!this.isUpgrading$quadragen(chunk) && !context.getPlanAt(chunk.getPos()).isOrdinaryNoise())
		{
			ci.cancel();
		}
	}

	@Inject(method = "getBaseHeight", at = @At("HEAD"), cancellable = true)
	private void getBaseHeight(
			int x,
			int z,
			Heightmap.Types type,
			LevelHeightAccessor heightAccessor,
			RandomState randomState,
			CallbackInfoReturnable<Integer> cir)
	{
		LevelContext context = this.getContext$quadragen();
		if (context != null && !this.isUpgradingHeightAccessor$quadragen(heightAccessor))
		{
			QuadrantPlan plan = context.getPlanAt(x, z);
			if (plan.isFlat())
			{
				cir.setReturnValue(plan.getFlat().getBaseHeightFromConfiguredLayers(type, heightAccessor));
			}
		}
	}

	@Inject(method = "getBaseColumn", at = @At("HEAD"), cancellable = true)
	private void getBaseColumn(
			int x,
			int z,
			LevelHeightAccessor heightAccessor,
			RandomState randomState,
			CallbackInfoReturnable<NoiseColumn> cir)
	{
		LevelContext context = this.getContext$quadragen();
		if (context != null && !this.isUpgradingHeightAccessor$quadragen(heightAccessor))
		{
			QuadrantPlan plan = context.getPlanAt(x, z);
			if (plan.isFlat())
			{
				cir.setReturnValue(plan.getFlat().getBaseColumnFromConfiguredLayers(heightAccessor));
			}
		}
	}

	@Unique
	private boolean isUpgradingHeightAccessor$quadragen(LevelHeightAccessor heightAccessor)
	{
		return heightAccessor == BelowZeroRetrogen.UPGRADE_HEIGHT_ACCESSOR || heightAccessor instanceof ChunkAccess && ((ChunkAccess)heightAccessor).isUpgrading();
	}

	@Unique
	private boolean isUpgrading$quadragen(ChunkAccess chunk)
	{
		return chunk.isUpgrading();
	}

	@Unique
	private LevelContext getContext$quadragen()
	{
		return ((GeneratorContextAccess)this).getLevelContext$quadragen();
	}
}
