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
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StrongholdFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * mc >= 1.16.5: main project
 * mc <= 1.15.2: subproject 1.15.2                    <--------
 * <p>
 * The legacy stronghold locate path uses precomputed positions without validating structure starts.
 */
@Mixin(StrongholdFeature.class)
public abstract class StrongholdFeatureMixin
{
	/**
	 * Filters the positions consumed by
	 * {@link net.minecraft.world.level.levelgen.feature.StrongholdFeature#getNearestGeneratedFeature} so Flat quadrants
	 * cannot be reported as stronghold sources.
	 */
	@ModifyExpressionValue(
			method = "getNearestGeneratedFeature",
			at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/levelgen/feature/StrongholdFeature;strongholdPos:[Lnet/minecraft/world/level/ChunkPos;")
	)
	private ChunkPos[] filterFlatCandidates(ChunkPos[] original, @Local(argsOnly = true) ChunkGenerator<?> generator)
	{
		LevelContext context = ((GeneratorContextAccess)generator).getLevelContext$quadragen();
		if (context == null)
		{
			return original;
		}
		int count = 0;
		for (ChunkPos candidate : original)
		{
			if (context.getPlanAt(candidate).isNoise())
			{
				count++;
			}
		}
		ChunkPos[] filtered = new ChunkPos[count];
		int index = 0;
		for (ChunkPos candidate : original)
		{
			if (context.getPlanAt(candidate).isNoise())
			{
				filtered[index++] = candidate;
			}
		}
		return filtered;
	}
}
