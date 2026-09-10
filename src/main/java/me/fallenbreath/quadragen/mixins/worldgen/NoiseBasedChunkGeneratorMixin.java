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
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

//#if MC >= 1.17.1
import net.minecraft.world.level.LevelHeightAccessor;
//#else
//$$ import net.minecraft.world.level.LevelAccessor;
//#endif

//#if MC >= 1.16.5
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.NoiseColumn;
//#endif

//#if MC >= 1.18.2
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.blending.Blender;
//#endif

//#if MC >= 1.19.4
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.RandomState;
//#elseif MC >= 1.18.2
//$$ import net.minecraft.core.Registry;
//$$ import net.minecraft.world.level.StructureFeatureManager;
//$$ import net.minecraft.world.level.biome.Biome;
//#elseif MC >= 1.16.5
//$$ import net.minecraft.world.level.StructureFeatureManager;
//#endif

//#if 1.17.1 <= MC && MC < 1.21.1
//$$ import java.util.concurrent.Executor;
//#endif

/**
 * mc >= 26.3: subproject 26.3
 * mc <= 26.2: subproject 26.2 (main project)       <--------
 */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin
{
	//#if MC >= 1.18.2
	@Inject(
			//#if MC >= 1.21.1
			method = "createBiomes(Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
			//#elseif MC >= 1.19.4
			//$$ method = "createBiomes(Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
			//#else
			//$$ method = "createBiomes(Lnet/minecraft/core/Registry;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/StructureFeatureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
			//#endif
			at = @At("HEAD"),
			cancellable = true
	)
	private void createBiomes(
			CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir,
			//#if 1.18.2 <= MC && MC < 1.19.4
			//$$ @Local(argsOnly = true) Registry<Biome> biomeRegistry,
			//#endif
			//#if 1.18.2 <= MC && MC < 1.21.1
			//$$ @Local(argsOnly = true) Executor executor,
			//#endif
			//#if MC >= 1.19.4
			@Local(argsOnly = true) RandomState randomState,
			//#endif
			@Local(argsOnly = true) Blender blender,
			//#if MC >= 1.19.4
			@Local(argsOnly = true) StructureManager structureManager,
			//#elseif MC >= 1.18.2
			//$$ @Local(argsOnly = true) StructureFeatureManager structureManager,
			//#endif
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
				//#elseif MC >= 1.19.4
				//$$ cir.setReturnValue(plan.getFlat().getFlatGenerator().createBiomes(executor, randomState, blender, structureManager, chunk));
				//#else
				//$$ cir.setReturnValue(plan.getFlat().getFlatGenerator().createBiomes(biomeRegistry, executor, blender, structureManager, chunk));
				//#endif
			}
		}
	}
	//#endif

	//#if MC >= 1.17.1
	@Inject(
			//#if MC >= 1.21.1
			method = "fillFromNoise(Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
			//#elseif MC >= 1.19.4
			//$$ method = "fillFromNoise(Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
			//#elseif MC >= 1.18.2
			//$$ method = "fillFromNoise(Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/StructureFeatureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
			//#else
			//$$ method = "fillFromNoise(Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/StructureFeatureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
			//#endif
			at = @At("HEAD"),
			cancellable = true
	)
	private void fillFromNoise(
			CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir,
			//#if 1.17.1 <= MC && MC < 1.21.1
			//$$ @Local(argsOnly = true) Executor executor,
			//#endif
			//#if MC >= 1.18.2
			@Local(argsOnly = true) Blender blender,
			//#endif
			//#if MC >= 1.19.4
			@Local(argsOnly = true) RandomState randomState,
			//#endif
			//#if MC >= 1.19.4
			@Local(argsOnly = true) StructureManager structureManager,
			//#elseif MC >= 1.18.2
			//$$ @Local(argsOnly = true) StructureFeatureManager structureManager,
			//#elseif MC >= 1.17.1
			//$$ @Local(argsOnly = true) StructureFeatureManager structureManager,
			//#endif
			@Local(argsOnly = true) ChunkAccess chunk)
	{
		LevelContext context = this.getContext$quadragen();
		if (context != null && !this.isUpgrading$quadragen(chunk))
		{
			QuadrantPlan plan = context.getPlanAt(chunk.getPos());
			if (!plan.isOrdinaryNoise())
			{
				if (plan.isFlat() && !plan.isClearGeneratedContent())
				{
					//#if MC >= 1.21.1
					cir.setReturnValue(plan.getFlat().getFlatGenerator().fillFromNoise(blender, randomState, structureManager, chunk));
					//#elseif MC >= 1.19.4
					//$$ cir.setReturnValue(plan.getFlat().getFlatGenerator().fillFromNoise(executor, blender, randomState, structureManager, chunk));
					//#elseif MC >= 1.18.2
					//$$ cir.setReturnValue(plan.getFlat().getFlatGenerator().fillFromNoise(executor, blender, structureManager, chunk));
					//#else
					//$$ cir.setReturnValue(plan.getFlat().getFlatGenerator().fillFromNoise(executor, structureManager, chunk));
					//#endif
				}
				else
				{
					cir.setReturnValue(CompletableFuture.completedFuture(chunk));
				}
			}
		}
	}
	//#else
	//$$ @Inject(
	//$$ 		method =
	//$$ //#if MC >= 1.16.5
	//$$ 		"fillFromNoise(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/level/StructureFeatureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
	//$$ //#else
	//$$ //$$ 		"fillFromNoise(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
	//$$ //#endif
	//$$ 		at = @At("HEAD"),
	//$$ 		cancellable = true
	//$$ )
	//$$ private void fillFromNoise(
	//$$ 		CallbackInfo ci,
	//$$ 		@Local(argsOnly = true) LevelAccessor level,
	//$$ //#if MC >= 1.16.5
	//$$ 		@Local(argsOnly = true) StructureFeatureManager structureManager,
	//$$ //#endif
	//$$ 		@Local(argsOnly = true) ChunkAccess chunk)
	//$$ {
	//$$ 	LevelContext context = this.getContext$quadragen();
	//$$ 	if (context != null)
	//$$ 	{
	//$$ 		QuadrantPlan plan = context.getPlanAt(chunk.getPos());
	//$$ 		if (!plan.isOrdinaryNoise())
	//$$ 		{
	//$$ 			if (plan.isFlat() && !plan.isClearGeneratedContent())
	//$$ 			{
	//$$ 				plan.getFlat().getFlatGenerator().fillFromNoise(
	//$$ 						level,
	//$$ //#if MC >= 1.16.5
	//$$ 						structureManager,
	//$$ //#endif
	//$$ 						chunk
	//$$ 				);
	//$$ 			}
	//$$ 			ci.cancel();
	//$$ 		}
	//$$ 	}
	//$$ }
	//#endif

	@Inject(
			//#if MC >= 1.19.4
			method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
			//#elseif MC >= 1.18.2
			//$$ method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureFeatureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
			//#elseif MC >= 1.15.2
			//$$ method = "buildSurfaceAndBedrock(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
			//#else
			//$$ method = "buildSurfaceAndBedrock(Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
			//#endif
			at = @At("HEAD"),
			cancellable = true
	)
	private void buildSurface(CallbackInfo ci, @Local(argsOnly = true) ChunkAccess chunk)
	{
		LevelContext context = this.getContext$quadragen();
		if (context != null && !this.isUpgrading$quadragen(chunk) && !context.getPlanAt(chunk.getPos()).isOrdinaryNoise())
		{
			ci.cancel();
		}
	}

	//#if MC >= 1.18.2
	@Inject(
			method = "applyCarvers",
			at = @At("HEAD"),
			cancellable = true
	)
	private void applyCarvers(CallbackInfo ci, @Local(argsOnly = true) ChunkAccess chunk)
	{
		LevelContext context = this.getContext$quadragen();
		if (context == null || this.isUpgrading$quadragen(chunk))
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
			//#if MC >= 1.19.4
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/carver/ConfiguredWorldCarver;isStartChunk(Lnet/minecraft/util/RandomSource;)Z")
			//#elseif MC >= 1.18.2
			//$$ at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/carver/ConfiguredWorldCarver;isStartChunk(Ljava/util/Random;)Z")
			//#endif
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
	//#endif

	//#if MC >= 1.16.5
	@Inject(method = "spawnOriginalMobs", at = @At("HEAD"), cancellable = true)
	private void spawnOriginalMobs(WorldGenRegion region, CallbackInfo ci)
	{
		LevelContext context = this.getContext$quadragen();
		if (context == null)
		{
			return;
		}
		ChunkAccess chunk = region.getChunk(
				//#if MC >= 1.17.1
				ChunkPosCompat.x(region.getCenter()), ChunkPosCompat.z(region.getCenter())
				//#elseif MC >= 1.16.5
				//$$ region.getCenterX(), region.getCenterZ()
				//#endif
		);
		if (!this.isUpgrading$quadragen(chunk) && !context.getPlanAt(chunk.getPos()).isOrdinaryNoise())
		{
			ci.cancel();
		}
	}
	//#endif

	@Inject(method = "getBaseHeight", at = @At("HEAD"), cancellable = true)
	private void getBaseHeight(
			int x,
			int z,
			Heightmap.Types type,
			//#if MC >= 1.17.1
			LevelHeightAccessor heightAccessor,
			//#endif
			//#if MC >= 1.19.4
			RandomState randomState,
			//#endif
			CallbackInfoReturnable<Integer> cir)
	{
		LevelContext context = this.getContext$quadragen();
		if (context != null
				//#if MC >= 1.17.1
				&& !this.isUpgradingHeightAccessor$quadragen(heightAccessor)
				//#endif
		)
		{
			QuadrantPlan plan = context.getPlanAt(x, z);
			if (plan.isFlat())
			{
				cir.setReturnValue(plan.getFlat().getBaseHeightFromConfiguredLayers(
						type
						//#if MC >= 1.17.1
						, heightAccessor
						//#endif
				));
			}
		}
	}

	//#if MC >= 1.16.5
	@Inject(method = "getBaseColumn", at = @At("HEAD"), cancellable = true)
	private void getBaseColumn(
			int x,
			int z,
			//#if MC >= 1.17.1
			LevelHeightAccessor heightAccessor,
			//#endif
			//#if MC >= 1.19.4
			RandomState randomState,
			//#endif
			CallbackInfoReturnable<
					//#if MC >= 1.17.1
					NoiseColumn
					//#elseif MC >= 1.16.5
					//$$ BlockGetter
					//#endif
			> cir)
	{
		LevelContext context = this.getContext$quadragen();
		if (context != null
				//#if MC >= 1.17.1
				&& !this.isUpgradingHeightAccessor$quadragen(heightAccessor)
				//#endif
		)
		{
			QuadrantPlan plan = context.getPlanAt(x, z);
			if (plan.isFlat())
			{
				cir.setReturnValue(plan.getFlat().getBaseColumnFromConfiguredLayers(
						//#if MC >= 1.17.1
						heightAccessor
						//#endif
				));
			}
		}
	}
	//#endif

	//#if MC >= 1.17.1
	@Unique
	private boolean isUpgradingHeightAccessor$quadragen(LevelHeightAccessor heightAccessor)
	{
		//#if MC >= 1.18.2
		return heightAccessor == BelowZeroRetrogen.UPGRADE_HEIGHT_ACCESSOR || heightAccessor instanceof ChunkAccess && ((ChunkAccess)heightAccessor).isUpgrading();
		//#elseif MC >= 1.17.1
		//$$ return false;
		//#endif
	}
	//#endif

	@Unique
	private boolean isUpgrading$quadragen(ChunkAccess chunk)
	{
		//#if MC >= 1.18.2
		return chunk.isUpgrading();
		//#else
		//$$ return false;
		//#endif
	}

	@Unique
	private LevelContext getContext$quadragen()
	{
		return ((GeneratorContextAccess)this).getLevelContext$quadragen();
	}
}
