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

package me.fallenbreath.quadragen.worldgen;

import com.mojang.datafixers.util.Pair;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

import java.util.function.Predicate;

public final class BiomeQuery
{
	private BiomeQuery()
	{
	}

	public static Pair<BlockPos, Holder<Biome>> findClosestBiome3d(
			LevelContext context,
			ServerLevel level,
			Predicate<Holder<Biome>> allowed,
			BlockPos origin,
			int searchRadius,
			int horizontalResolution,
			int verticalResolution)
	{
		int sampleRadius = Math.floorDiv(searchRadius, horizontalResolution);
		int[] sampleYs = Mth.outFromOrigin(origin.getY(), level.getMinY() + 1, level.getMaxY() + 1, verticalResolution).toArray();
		Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
		for (BlockPos.MutableBlockPos sampleColumn : BlockPos.spiralAround(BlockPos.ZERO, sampleRadius, Direction.EAST, Direction.SOUTH))
		{
			int blockX = origin.getX() + sampleColumn.getX() * horizontalResolution;
			int blockZ = origin.getZ() + sampleColumn.getZ() * horizontalResolution;
			for (int blockY : sampleYs)
			{
				QuadrantPlan plan = context.planForBlock(blockX, blockZ);
				Holder<Biome> biome = plan.isFlat()
						? plan.getFlat().getBiome()
						: context.getNoiseGenerator().getBiomeSource().getNoiseBiome(QuartPos.fromBlock(blockX), QuartPos.fromBlock(blockY), QuartPos.fromBlock(blockZ), sampler);
				if (allowed.test(biome))
				{
					return Pair.of(new BlockPos(blockX, blockY, blockZ), biome);
				}
			}
		}
		return null;
	}
}
