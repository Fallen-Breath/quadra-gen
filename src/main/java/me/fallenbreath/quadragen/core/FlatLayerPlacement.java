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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.List;

//#if MC < 1.15.2
//$$ import net.minecraft.world.level.levelgen.feature.LayerConfiguration;
//#else
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
//#endif

//#if MC >= 1.19.4
import net.minecraft.util.RandomSource;
//#else
//$$ import java.util.Random;
//#endif

//#if MC >= 1.16.5
import net.minecraft.world.level.WorldGenLevel;
//#else
//$$ import net.minecraft.server.level.WorldGenRegion;
//#endif

public final class FlatLayerPlacement
{
	private FlatLayerPlacement()
	{
	}

	public static void placeDelayedLayers(
			//#if MC >= 1.16.5
			WorldGenLevel level,
			//#else
			//$$ WorldGenRegion level,
			//#endif
			ChunkAccess chunk, FlatGenerationPlan plan)
	{
		BlockPos origin = new BlockPos(chunk.getPos().getMinBlockX(), LevelHeightCompat.minY(level) + 1, chunk.getPos().getMinBlockZ());
		// NOTE: {@link net.minecraft.world.level.levelgen.feature.FillLayerFeature#place} does not consume random in supported versions;
		// re-audit before relying on this unseeded source.
		//#if MC >= 1.19.4
		RandomSource random = RandomSource.create();
		//#else
		//$$ Random random = new Random();
		//#endif
		List<BlockState> layers = plan.getLayers();
		for (int index = 0; index < layers.size(); index++)
		{
			if (plan.isDelayedLayer(index))
			{
				//#if MC >= 1.18.2
				Feature.FILL_LAYER.place(new LayerConfiguration(index, layers.get(index)), level, plan.getFlatGenerator(), random, origin);
				//#elseif MC >= 1.15.2
				//$$ Feature.FILL_LAYER.configured(new LayerConfiguration(index, layers.get(index))).place(level, plan.getFlatGenerator(), random, origin);
				//#else
				//$$ Feature.FILL_LAYER.place(level, plan.getFlatGenerator(), random, origin, new LayerConfiguration(index, layers.get(index)));
				//#endif
			}
		}
	}
}
