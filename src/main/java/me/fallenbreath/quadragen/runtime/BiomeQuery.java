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
import me.fallenbreath.quadragen.compat.LevelHeightCompat;
import me.fallenbreath.quadragen.core.Quadrant;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public final class BiomeQuery
{
	private BiomeQuery()
	{
	}

	//#if MC >= 1.19.4
	/**
	 * Mirrors {@link net.minecraft.world.level.biome.BiomeSource#findClosestBiome3d} search order while extending its candidate
	 * set and sampled biome source with quadrant-aware Flat biomes.
	 */
	public static Pair<BlockPos, Holder<Biome>> findClosestBiome3d(
			LevelContext context,
			ServerLevel level,
			Predicate<Holder<Biome>> allowed,
			BlockPos origin,
			int searchRadius,
			int horizontalResolution,
			int verticalResolution)
	{
		Set<Holder<Biome>> candidates = new HashSet<Holder<Biome>>(context.getNoiseGenerator().getBiomeSource().possibleBiomes());
		for (Quadrant quadrant : Quadrant.values())
		{
			QuadrantPlan plan = context.getPlan(quadrant);
			if (plan.isFlat())
			{
				candidates.add(plan.getFlat().getBiome());
			}
		}
		candidates.removeIf(biome -> !allowed.test(biome));
		if (candidates.isEmpty())
		{
			return null;
		}

		int sampleRadius = Math.floorDiv(searchRadius, horizontalResolution);
		int[] sampleYs = Mth.outFromOrigin(
				origin.getY(),
				LevelHeightCompat.minY(level) + 1,
				LevelHeightCompat.maxYInclusive(level) + 1,
				verticalResolution
		).toArray();
		Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
		for (BlockPos.MutableBlockPos sampleColumn : BlockPos.spiralAround(BlockPos.ZERO, sampleRadius, Direction.EAST, Direction.SOUTH))
		{
			int blockX = origin.getX() + sampleColumn.getX() * horizontalResolution;
			int blockZ = origin.getZ() + sampleColumn.getZ() * horizontalResolution;
			for (int blockY : sampleYs)
			{
				QuadrantPlan plan = context.getPlanAt(blockX, blockZ);
				Holder<Biome> biome = plan.isFlat()
						? plan.getFlat().getBiome()
						: context.getNoiseGenerator().getBiomeSource().getNoiseBiome(QuartPos.fromBlock(blockX), QuartPos.fromBlock(blockY), QuartPos.fromBlock(blockZ), sampler);
				if (candidates.contains(biome))
				{
					return Pair.of(new BlockPos(blockX, blockY, blockZ), biome);
				}
			}
		}
		return null;
	}
	//#elseif MC >= 1.18.2
	//$$ /**
	//$$  * Mirrors the closest-first branch of {@link net.minecraft.world.level.biome.BiomeSource#findBiomeHorizontal}
	//$$  * while sampling quadrant-aware Flat biomes.
	//$$  */
	//$$ public static Pair<BlockPos, Holder<Biome>> findNearestBiome(
	//$$ 		LevelContext context,
	//$$ 		Predicate<Holder<Biome>> allowed,
	//$$ 		BlockPos origin,
	//$$ 		int searchRadius,
	//$$ 		int sampleResolution)
	//$$ {
	//$$ 	int originQuartX = QuartPos.fromBlock(origin.getX());
	//$$ 	int originQuartY = QuartPos.fromBlock(origin.getY());
	//$$ 	int originQuartZ = QuartPos.fromBlock(origin.getZ());
	//$$ 	int sampleRadius = QuartPos.fromBlock(searchRadius);
	//$$ 	Climate.Sampler sampler = context.getNoiseGenerator().climateSampler();
	//$$ 	for (int radius = 0; radius <= sampleRadius; radius += sampleResolution)
	//$$ 	{
	//$$ 		int startZ = SharedConstants.debugGenerateSquareTerrainWithoutNoise ? 0 : -radius;
	//$$ 		for (int offsetZ = startZ; offsetZ <= radius; offsetZ += sampleResolution)
	//$$ 		{
	//$$ 			boolean zEdge = Math.abs(offsetZ) == radius;
	//$$ 			for (int offsetX = -radius; offsetX <= radius; offsetX += sampleResolution)
	//$$ 			{
	//$$ 				if (Math.abs(offsetX) != radius && !zEdge)
	//$$ 				{
	//$$ 					continue;
	//$$ 				}
	//$$ 				int quartX = originQuartX + offsetX;
	//$$ 				int quartZ = originQuartZ + offsetZ;
	//$$ 				int blockX = QuartPos.toBlock(quartX);
	//$$ 				int blockZ = QuartPos.toBlock(quartZ);
	//$$ 				QuadrantPlan plan = context.getPlanAt(blockX, blockZ);
	//$$ 				Holder<Biome> biome = plan.isFlat()
	//$$ 						? plan.getFlat().getBiome()
	//$$ 						: context.getNoiseGenerator().getBiomeSource().getNoiseBiome(quartX, originQuartY, quartZ, sampler);
	//$$ 				if (allowed.test(biome))
	//$$ 				{
	//$$ 					return Pair.of(new BlockPos(blockX, origin.getY(), blockZ), biome);
	//$$ 				}
	//$$ 			}
	//$$ 		}
	//$$ 	}
	//$$ 	return null;
	//$$ }
	//#endif
}
