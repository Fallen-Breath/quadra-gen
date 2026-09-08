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
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
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

import java.util.concurrent.CompletableFuture;

//#if MC >= 1.20.4
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
//#else
//$$ // TODO: Port upgrading-height detection against the target MC source.
//#endif

//#if 1.20.4 <= MC && MC < 1.21.1
//$$ import java.util.concurrent.Executor;
//#endif

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin
{
	@Inject(
			//#if MC >= 1.21.1
			method = "createBiomes(Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
			//#elseif MC >= 1.20.4
			//$$ method = "createBiomes(Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
			//#else
			//$$ // TODO: Port this descriptor against the target MC source.
			//$$ method = TODO_PORT_MC_VERSION,
			//#endif
			at = @At("HEAD"),
			cancellable = true
	)
	private void createBiomes(
			CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir,
			//#if 1.20.4 <= MC && MC < 1.21.1
			//$$ @Local(argsOnly = true) Executor executor,
			//#endif
			@Local(argsOnly = true) RandomState randomState,
			@Local(argsOnly = true) Blender blender,
			@Local(argsOnly = true) StructureManager structureManager,
			@Local(argsOnly = true) ChunkAccess chunk)
	{
		LevelContext context = this.getContext$quadragen();
		if (context != null && !chunk.isUpgrading())
		{
			QuadrantPlan plan = context.getPlanAt(chunk.getPos());
			if (plan.isFlat())
			{
				//#if MC >= 1.21.1
				cir.setReturnValue(plan.getFlat().getFlatGenerator().createBiomes(randomState, blender, structureManager, chunk));
				//#elseif MC >= 1.20.4
				//$$ cir.setReturnValue(plan.getFlat().getFlatGenerator().createBiomes(executor, randomState, blender, structureManager, chunk));
				//#else
				//$$ // TODO: Port this call against the target MC source.
				//$$ TODO_PORT_MC_VERSION();
				//#endif
			}
		}
	}

	@Inject(
			//#if MC >= 1.21.1
			method = "fillFromNoise(Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
			//#elseif MC >= 1.20.4
			//$$ method = "fillFromNoise(Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
			//#else
			//$$ // TODO: Port this descriptor against the target MC source.
			//$$ method = TODO_PORT_MC_VERSION,
			//#endif
			at = @At("HEAD"),
			cancellable = true
	)
	private void fillFromNoise(
			CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir,
			//#if 1.20.4 <= MC && MC < 1.21.1
			//$$ @Local(argsOnly = true) Executor executor,
			//#endif
			@Local(argsOnly = true) Blender blender,
			@Local(argsOnly = true) RandomState randomState,
			@Local(argsOnly = true) StructureManager structureManager,
			@Local(argsOnly = true) ChunkAccess chunk)
	{
		LevelContext context = this.getContext$quadragen();
		if (context != null && !chunk.isUpgrading())
		{
			QuadrantPlan plan = context.getPlanAt(chunk.getPos());
			if (!plan.isOrdinaryNoise())
			{
				if (plan.isFlat() && !plan.isClearGeneratedContent())
				{
					//#if MC >= 1.21.1
					cir.setReturnValue(plan.getFlat().getFlatGenerator().fillFromNoise(blender, randomState, structureManager, chunk));
					//#elseif MC >= 1.20.4
					//$$ cir.setReturnValue(plan.getFlat().getFlatGenerator().fillFromNoise(executor, blender, randomState, structureManager, chunk));
					//#else
					//$$ // TODO: Port this call against the target MC source.
					//$$ TODO_PORT_MC_VERSION();
					//#endif
				}
				else
				{
					cir.setReturnValue(CompletableFuture.completedFuture(chunk));
				}
			}
		}
	}

	@Inject(
			method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private void buildSurface(CallbackInfo ci, @Local(argsOnly = true) ChunkAccess chunk)
	{
		LevelContext context = this.getContext$quadragen();
		if (context != null && !chunk.isUpgrading() && !context.getPlanAt(chunk.getPos()).isOrdinaryNoise())
		{
			ci.cancel();
		}
	}

	@Inject(
			method = "applyCarvers",
			at = @At("HEAD"),
			cancellable = true
	)
	private void applyCarvers(CallbackInfo ci, @Local(argsOnly = true) ChunkAccess chunk)
	{
		LevelContext context = this.getContext$quadragen();
		if (context == null || chunk.isUpgrading())
		{
			return;
		}
		if (!context.getPlanAt(chunk.getPos()).isOrdinaryNoise())
		{
			ci.cancel();
		}
	}

	@ModifyExpressionValue(
			method = "applyCarvers",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/carver/ConfiguredWorldCarver;isStartChunk(Lnet/minecraft/util/RandomSource;)Z")
	)
	private boolean filterCarverSource(
			boolean isStartChunk,
			@Local(argsOnly = true) ChunkAccess chunk,
			@Local(ordinal = 1) ChunkPos sourcePos)
	{
		if (!isStartChunk || chunk.isUpgrading())
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
		if (!chunk.isUpgrading() && !context.getPlanAt(chunk.getPos()).isOrdinaryNoise())
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
		//#if MC >= 1.20.4
		return heightAccessor == BelowZeroRetrogen.UPGRADE_HEIGHT_ACCESSOR || heightAccessor instanceof ChunkAccess && ((ChunkAccess)heightAccessor).isUpgrading();
		//#else
		//$$ // TODO: Port upgrading-height detection against the target MC source.
		//$$ return TODO_PORT_MC_VERSION;
		//#endif
	}

	@Unique
	private LevelContext getContext$quadragen()
	{
		return ((GeneratorContextAccess)this).getLevelContext$quadragen();
	}
}
