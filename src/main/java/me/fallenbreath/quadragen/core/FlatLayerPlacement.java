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

package me.fallenbreath.quadragen.core;

import me.fallenbreath.quadragen.compat.LevelHeightCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;

import java.util.List;

public final class FlatLayerPlacement
{
	private FlatLayerPlacement()
	{
	}

	public static void placeDelayedLayers(WorldGenLevel level, ChunkAccess chunk, FlatGenerationPlan plan)
	{
		BlockPos origin = new BlockPos(chunk.getPos().getMinBlockX(), LevelHeightCompat.minY(level) + 1, chunk.getPos().getMinBlockZ());
		// NOTE: {@link net.minecraft.world.level.levelgen.feature.FillLayerFeature#place} does not consume random in supported versions;
		// re-audit before relying on this unseeded source.
		RandomSource random = RandomSource.create();
		List<BlockState> layers = plan.getLayers();
		for (int index = 0; index < layers.size(); index++)
		{
			if (plan.isDelayedLayer(index))
			{
				Feature.FILL_LAYER.place(new LayerConfiguration(index, layers.get(index)), level, plan.getFlatGenerator(), random, origin);
			}
		}
	}
}
