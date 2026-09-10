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

package me.fallenbreath.quadragen.mixins.worldgen.terrain;

import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

//#if MC >= 1.18.2
import net.minecraft.world.level.levelgen.blending.Blender;
//#endif

//#if MC < 1.17.1
//$$ import net.minecraft.world.level.LevelAccessor;
//#endif

//#if MC >= 1.19.4
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.RandomState;
//#elseif MC >= 1.16.5
//$$ import net.minecraft.world.level.StructureFeatureManager;
//#endif

//#if 1.17.1 <= MC && MC < 1.21.1
//$$ import java.util.concurrent.Executor;
//#endif

/**
 * mc >= 26.3: subproject 26.3
 * mc <= 26.2: subproject 26.2 (main project)       <--------
 * <p>
 * Noise fill and surface remain separate generator stages through 26.2.
 */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class TerrainGenerationMixin
{
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
			//#else
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
