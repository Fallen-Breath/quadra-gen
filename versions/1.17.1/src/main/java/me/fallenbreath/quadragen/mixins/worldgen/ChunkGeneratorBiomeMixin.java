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

import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

//#if MC >= 1.16.5
import net.minecraft.core.Registry;
//#endif

//#if MC >= 1.15.2
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ProtoChunk;
//#endif

/**
 * mc >= 1.18.2: subproject 26.2 (main project)
 * mc <= 1.17.1: subproject 1.17.1                    <--------
 */
@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorBiomeMixin
{
	/**
	 * Mirrors {@link net.minecraft.world.level.chunk.ChunkGenerator#createBiomes} with constant biome storage
	 * for Flat quadrants.
	 */
	@Inject(
			method =
			//#if MC >= 1.16.5
			"createBiomes(Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
			//#else
			//$$ "createBiomes(Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
			//#endif
			at = @At("HEAD"),
			cancellable = true
	)
	private void createBiomes(
			CallbackInfo ci,
			//#if MC >= 1.16.5
			@Local(argsOnly = true) Registry<Biome> biomeRegistry,
			//#endif
			@Local(argsOnly = true) ChunkAccess chunk)
	{
		LevelContext context = ((GeneratorContextAccess)this).getLevelContext$quadragen();
		if (context != null)
		{
			QuadrantPlan plan = context.getPlanAt(chunk.getPos());
			if (plan.isFlat())
			{
				//#if MC >= 1.17.1
				int[] biomeIds = new int[16 * ((chunk.getHeight() + 3) / 4)];
				Arrays.fill(biomeIds, biomeRegistry.getId(plan.getFlat().getBiome()));
				((ProtoChunk)chunk).setBiomes(new ChunkBiomeContainer(biomeRegistry, chunk, biomeIds));
				//#elseif MC >= 1.15.2
				//$$ Biome[] biomes = new Biome[ChunkBiomeContainer.BIOMES_SIZE];
				//$$ Arrays.fill(biomes, plan.getFlat().getBiome());
				//$$ //#if MC >= 1.16.5
				//$$ ((ProtoChunk)chunk).setBiomes(new ChunkBiomeContainer(biomeRegistry, biomes));
				//$$ //#else
				//$$ //$$ ((ProtoChunk)chunk).setBiomes(new ChunkBiomeContainer(biomes));
				//$$ //#endif
				//#else
				//$$ Biome[] biomes = new Biome[256];
				//$$ Arrays.fill(biomes, plan.getFlat().getBiome());
				//$$ chunk.setBiomes(biomes);
				//#endif
				ci.cancel();
			}
		}
	}
}
