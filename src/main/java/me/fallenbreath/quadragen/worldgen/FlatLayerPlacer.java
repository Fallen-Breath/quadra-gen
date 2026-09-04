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

import me.fallenbreath.quadragen.compat.ChunkBlockWriter;
import me.fallenbreath.quadragen.core.FlatGenerationPlan;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

public final class FlatLayerPlacer
{
	private FlatLayerPlacer()
	{
	}

	public static void placeDirectLayers(ChunkAccess chunk, FlatGenerationPlan plan)
	{
		List<BlockState> layers = plan.getLayers();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		for (int index = 0; index < layers.size(); index++)
		{
			if (plan.isDelayedLayer(index))
			{
				continue;
			}
			int y = plan.getBaseY() + index;
			if (chunk.isOutsideBuildHeight(y))
			{
				continue;
			}
			BlockState state = layers.get(index);
			for (int x = 0; x < 16; x++)
			{
				for (int z = 0; z < 16; z++)
				{
					ChunkBlockWriter.set(chunk, pos.set(x, y, z), state);
					oceanFloor.update(x, y, z, state);
					worldSurface.update(x, y, z, state);
				}
			}
		}
	}

	public static void placeDelayedLayers(WorldGenLevel level, ChunkAccess chunk, FlatGenerationPlan plan)
	{
		List<BlockState> layers = plan.getLayers();
		ChunkPos chunkPos = chunk.getPos();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int index = 0; index < layers.size(); index++)
		{
			if (!plan.isDelayedLayer(index))
			{
				continue;
			}
			int y = plan.getBaseY() + index;
			if (level.isOutsideBuildHeight(y))
			{
				continue;
			}
			BlockState state = layers.get(index);
			for (int dx = 0; dx < 16; dx++)
			{
				for (int dz = 0; dz < 16; dz++)
				{
					pos.set(chunkPos.getMinBlockX() + dx, y, chunkPos.getMinBlockZ() + dz);
					if (level.getBlockState(pos).isAir())
					{
						level.setBlock(pos, state, 2);
					}
				}
			}
		}
	}
}
