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

package me.fallenbreath.quadragen.runtime;

import com.mojang.datafixers.util.Pair;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Predicate;

//#if MC >= 1.17.1
import net.minecraft.core.QuartPos;
//#endif

//#if MC >= 1.18.2
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Climate;
//#endif

/**
 * mc >= 1.19: subproject 26.2 (main project)        <--------
 * mc < 1.19: subproject 1.18.2 (supports mc >= 1.16.5)
 * <p>
 */
public final class BiomeQuery
{
	private BiomeQuery()
	{
	}

	//#if MC >= 1.16.5
	/**
	 * Mirrors the closest-first branch of {@link net.minecraft.world.level.biome.BiomeSource#findBiomeHorizontal}
	 * while sampling quadrant-aware Flat biomes.
	 */
	public static
			//#if MC >= 1.18.2
			Pair<BlockPos, Holder<Biome>>
			//#else
			//$$ BlockPos
			//#endif
			findNearestBiome(
			LevelContext context,
			//#if MC >= 1.18.2
			Predicate<Holder<Biome>> allowed,
			//#else
			//$$ Biome allowed,
			//#endif
			BlockPos origin,
			int searchRadius,
			int sampleResolution)
	{
		//#if MC >= 1.17.1
		int originQuartX = QuartPos.fromBlock(origin.getX());
		int originQuartY = QuartPos.fromBlock(origin.getY());
		int originQuartZ = QuartPos.fromBlock(origin.getZ());
		int sampleRadius = QuartPos.fromBlock(searchRadius);
		//#elseif MC >= 1.16.5
		//$$ int originQuartX = origin.getX() >> 2;
		//$$ int originQuartY = origin.getY() >> 2;
		//$$ int originQuartZ = origin.getZ() >> 2;
		//$$ int sampleRadius = searchRadius >> 2;
		//#endif
		//#if MC >= 1.18.2
		Climate.Sampler sampler = context.getNoiseGenerator().climateSampler();
		//#endif
		for (int radius = 0; radius <= sampleRadius; radius += sampleResolution)
		{
			//#if MC >= 1.18.2
			int startZ = SharedConstants.debugGenerateSquareTerrainWithoutNoise ? 0 : -radius;
			//#else
			//$$ int startZ = -radius;
			//#endif
			for (int offsetZ = startZ; offsetZ <= radius; offsetZ += sampleResolution)
			{
				boolean zEdge = Math.abs(offsetZ) == radius;
				for (int offsetX = -radius; offsetX <= radius; offsetX += sampleResolution)
				{
					if (Math.abs(offsetX) != radius && !zEdge)
					{
						continue;
					}
					int quartX = originQuartX + offsetX;
					int quartZ = originQuartZ + offsetZ;
					//#if MC >= 1.17.1
					int blockX = QuartPos.toBlock(quartX);
					int blockZ = QuartPos.toBlock(quartZ);
					//#elseif MC >= 1.16.5
					//$$ int blockX = quartX << 2;
					//$$ int blockZ = quartZ << 2;
					//#endif
					QuadrantPlan plan = context.getPlanAt(blockX, blockZ);
					//#if MC >= 1.18.2
					Holder<Biome> biome = plan.isFlat()
							? plan.getFlat().getBiome()
							: context.getNoiseGenerator().getBiomeSource().getNoiseBiome(quartX, originQuartY, quartZ, sampler);
					if (allowed.test(biome))
					{
						return Pair.of(new BlockPos(blockX, origin.getY(), blockZ), biome);
					}
					//#elseif MC >= 1.16.5
					//$$ Biome biome = plan.isFlat()
					//$$ 		? plan.getFlat().getBiome()
					//$$ 		: context.getNoiseGenerator().getBiomeSource().getNoiseBiome(quartX, originQuartY, quartZ);
					//$$ if (biome == allowed)
					//$$ {
					//$$ 	return new BlockPos(blockX, origin.getY(), blockZ);
					//$$ }
					//#endif
				}
			}
		}
		return null;
	}
	//#endif
}
