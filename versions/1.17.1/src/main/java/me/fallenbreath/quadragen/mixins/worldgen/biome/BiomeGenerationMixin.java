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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * mc >= 26.3: subproject 26.3
 * mc in [1.18.2, 26.3): subproject 26.2 (main project)
 * mc < 1.18.2: subproject 1.17.1                    <--------
 * <p>
 * Biome generation is implemented by ChunkGenerator in this interval.
 */
@Mixin(ChunkGenerator.class)
public abstract class BiomeGenerationMixin
{
	@ModifyExpressionValue(
			method =
			//#if MC >= 1.16.5
			"createBiomes(Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
			//#else
			//$$ "createBiomes(Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
			//#endif
			//#if MC >= 1.16.5
			at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;runtimeBiomeSource:Lnet/minecraft/world/level/biome/BiomeSource;")
			//#else
			//$$ at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;biomeSource:Lnet/minecraft/world/level/biome/BiomeSource;")
			//#endif
	)
	private BiomeSource useBiomeSourceAtChunk(BiomeSource original, @Local(argsOnly = true) ChunkAccess chunk)
	{
		LevelContext context = ((GeneratorContextAccess)this).getLevelContext$quadragen();
		if (context == null)
		{
			return original;
		}
		QuadrantPlan plan = context.getPlanAt(chunk.getPos());
		return plan.isFlat() ? plan.getFlat().getFlatGenerator().getBiomeSource() : original;
	}
}
