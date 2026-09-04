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

import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import me.fallenbreath.quadragen.runtime.access.ProtoChunkContextAccess;
import me.fallenbreath.quadragen.worldgen.GenerationHooks;
import me.fallenbreath.quadragen.worldgen.HeightQuery;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin
{
	@Inject(
			method = "createBiomes(Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
			at = @At("HEAD"),
			cancellable = true
	)
	private void quadragen$createBiomes(
			RandomState randomState,
			Blender blender,
			StructureManager structureManager,
			ChunkAccess chunk,
			CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir)
	{
		LevelContext context = this.quadragen$getContext();
		if (context != null && !chunk.isUpgrading() && GenerationHooks.plan(context, chunk).isFlat())
		{
			cir.setReturnValue(GenerationHooks.createBiomes(context, randomState, chunk));
		}
	}

	@Inject(
			method = "fillFromNoise(Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
			at = @At("HEAD"),
			cancellable = true
	)
	private void quadragen$fillFromNoise(
			Blender blender,
			RandomState randomState,
			StructureManager structureManager,
			ChunkAccess chunk,
			CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir)
	{
		LevelContext context = this.quadragen$getContext();
		if (context != null && !chunk.isUpgrading() && !GenerationHooks.plan(context, chunk).isOrdinaryNoise())
		{
			cir.setReturnValue(GenerationHooks.fill(context, chunk));
		}
	}

	@Inject(
			method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private void quadragen$buildSurface(
			WorldGenRegion region,
			StructureManager structureManager,
			RandomState randomState,
			ChunkAccess chunk,
			CallbackInfo ci)
	{
		LevelContext context = this.quadragen$getContext();
		if (context != null && !chunk.isUpgrading() && !GenerationHooks.plan(context, chunk).shouldRunSurface())
		{
			ci.cancel();
		}
	}

	@Inject(
			method = "applyCarvers",
			at = @At("HEAD"),
			cancellable = true
	)
	private void quadragen$applyCarvers(
			WorldGenRegion region,
			long seed,
			RandomState randomState,
			BiomeManager biomeManager,
			StructureManager structureManager,
			ChunkAccess chunk,
			CallbackInfo ci)
	{
		LevelContext context = this.quadragen$getContext();
		if (context == null || chunk.isUpgrading())
		{
			return;
		}
		if (!GenerationHooks.plan(context, chunk).shouldRunCarvers())
		{
			ci.cancel();
			return;
		}
		if (chunk instanceof ProtoChunkContextAccess)
		{
			((ProtoChunkContextAccess)chunk).quadragen$setLevelContext(context);
		}
	}

	@Inject(method = "spawnOriginalMobs", at = @At("HEAD"), cancellable = true)
	private void quadragen$spawnOriginalMobs(WorldGenRegion region, CallbackInfo ci)
	{
		LevelContext context = this.quadragen$getContext();
		if (context == null)
		{
			return;
		}
		ChunkAccess chunk = region.getChunk(region.getCenter().x(), region.getCenter().z());
		if (!chunk.isUpgrading() && !GenerationHooks.plan(context, chunk).shouldRunWorldgenMobs())
		{
			ci.cancel();
		}
	}

	@Inject(method = "getBaseHeight", at = @At("HEAD"), cancellable = true)
	private void quadragen$getBaseHeight(
			int x,
			int z,
			Heightmap.Types type,
			LevelHeightAccessor heightAccessor,
			RandomState randomState,
			CallbackInfoReturnable<Integer> cir)
	{
		LevelContext context = this.quadragen$getContext();
		if (context != null)
		{
			QuadrantPlan plan = context.planForBlock(x, z);
			if (plan.isFlat())
			{
				cir.setReturnValue(HeightQuery.getBaseHeight(plan.getFlat(), type, heightAccessor));
			}
		}
	}

	@Inject(method = "getBaseColumn", at = @At("HEAD"), cancellable = true)
	private void quadragen$getBaseColumn(
			int x,
			int z,
			LevelHeightAccessor heightAccessor,
			RandomState randomState,
			CallbackInfoReturnable<NoiseColumn> cir)
	{
		LevelContext context = this.quadragen$getContext();
		if (context != null)
		{
			QuadrantPlan plan = context.planForBlock(x, z);
			if (plan.isFlat())
			{
				cir.setReturnValue(HeightQuery.getBaseColumn(plan.getFlat(), heightAccessor));
			}
		}
	}

	private LevelContext quadragen$getContext()
	{
		return ((GeneratorContextAccess)this).quadragen$getLevelContext();
	}
}
