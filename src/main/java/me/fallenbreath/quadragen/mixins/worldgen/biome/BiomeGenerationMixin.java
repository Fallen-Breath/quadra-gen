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

package me.fallenbreath.quadragen.mixins.worldgen.biome;

import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

//#if 1.18.2 <= MC && MC < 1.19.4
//$$ import net.minecraft.core.Registry;
//$$ import net.minecraft.world.level.biome.Biome;
//#endif

//#if 1.18.2 <= MC && MC < 1.21.1
//$$ import java.util.concurrent.Executor;
//#endif

//#if MC >= 1.19.4
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.RandomState;
//#else
//$$ import net.minecraft.world.level.StructureFeatureManager;
//#endif

/**
 * mc >= 26.3: subproject 26.3
 * 1.18.2 <= mc <= 26.2: subproject 26.2 (main project)       <--------
 * mc <= 1.17.1: subproject 1.17.1
 * <p>
 * Biome generation is implemented by NoiseBasedChunkGenerator in this interval.
 */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class BiomeGenerationMixin
{
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
			//#else
			//$$ @Local(argsOnly = true) StructureFeatureManager structureManager,
			//#endif
			@Local(argsOnly = true) ChunkAccess chunk)
	{
		LevelContext context = ((GeneratorContextAccess)this).getLevelContext$quadragen();
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
}
