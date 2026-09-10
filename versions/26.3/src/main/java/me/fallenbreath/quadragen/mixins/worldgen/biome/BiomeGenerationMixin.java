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
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

/**
 * mc >= 26.3: subproject 26.3                    <--------
 * 1.18.2 <= mc <= 26.2: subproject 26.2 (main project)
 * mc <= 1.17.1: subproject 1.17.1
 * <p>
 * 26.3 moves biome generation from
 * {@link net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator} to {@link ChunkGenerator}.
 */
@Mixin(ChunkGenerator.class)
public abstract class BiomeGenerationMixin
{
	@Inject(
			method = "createBiomes(Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
			at = @At("HEAD"),
			cancellable = true
	)
	private void createBiomes(
			CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir,
			@Local(argsOnly = true) RandomState randomState,
			@Local(argsOnly = true) Blender blender,
			@Local(argsOnly = true) StructureManager structureManager,
			@Local(argsOnly = true) ChunkAccess chunk)
	{
		LevelContext context = ((GeneratorContextAccess)this).getLevelContext$quadragen();
		if (context != null && !chunk.isUpgrading())
		{
			QuadrantPlan plan = context.getPlanAt(chunk.getPos());
			if (plan.isFlat())
			{
				cir.setReturnValue(plan.getFlat().getFlatGenerator().createBiomes(randomState, blender, structureManager, chunk));
			}
		}
	}
}
