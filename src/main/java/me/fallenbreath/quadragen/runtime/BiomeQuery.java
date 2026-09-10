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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

//#if MC >= 26.3
//$$ import net.minecraft.world.level.biome.BiomeResolver;
//#else
import net.minecraft.world.level.biome.Climate;
//#endif

/**
 * mc >= 1.19: subproject 26.2 (main project)        <--------
 * mc < 1.19: subproject 1.18.2
 */
public final class BiomeQuery
{
	private BiomeQuery()
	{
	}

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
		//#if MC >= 26.3
		//$$ BiomeResolver resolver = context.getNoiseGenerator().getBiomeSource().createCachingResolver(level.getChunkSource().randomState());
		//#else
		Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
		//#endif
		for (BlockPos.MutableBlockPos sampleColumn : BlockPos.spiralAround(BlockPos.ZERO, sampleRadius, Direction.EAST, Direction.SOUTH))
		{
			int blockX = origin.getX() + sampleColumn.getX() * horizontalResolution;
			int blockZ = origin.getZ() + sampleColumn.getZ() * horizontalResolution;
			for (int blockY : sampleYs)
			{
				QuadrantPlan plan = context.getPlanAt(blockX, blockZ);
				Holder<Biome> biome;
				if (plan.isFlat())
				{
					biome = plan.getFlat().getBiome();
				}
				else
				{
					//#if MC >= 26.3
					//$$ biome = resolver.getNoiseBiome(QuartPos.fromBlock(blockX), QuartPos.fromBlock(blockY), QuartPos.fromBlock(blockZ));
					//#else
					biome = context.getNoiseGenerator().getBiomeSource().getNoiseBiome(QuartPos.fromBlock(blockX), QuartPos.fromBlock(blockY), QuartPos.fromBlock(blockZ), sampler);
					//#endif
				}
				if (candidates.contains(biome))
				{
					return Pair.of(new BlockPos(blockX, blockY, blockZ), biome);
				}
			}
		}
		return null;
	}
}
