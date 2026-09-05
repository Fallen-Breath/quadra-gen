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

import me.fallenbreath.quadragen.compat.HeightCompat;
import me.fallenbreath.quadragen.core.FlatGenerationPlan;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

public final class HeightQuery
{
	private HeightQuery()
	{
	}

	public static int getBaseHeight(FlatGenerationPlan plan, Heightmap.Types type, LevelHeightAccessor heightAccessor)
	{
		List<BlockState> layers = plan.getLayers();
		int minY = HeightCompat.minY(heightAccessor);
		int maxIndex = Math.min(layers.size() - 1, HeightCompat.maxYInclusive(heightAccessor) - plan.getBaseY());
		for (int index = maxIndex; index >= 0; index--)
		{
			int y = plan.getBaseY() + index;
			if (y >= minY && type.isOpaque().test(layers.get(index)))
			{
				return y + 1;
			}
		}
		return minY;
	}

	public static NoiseColumn getBaseColumn(FlatGenerationPlan plan, LevelHeightAccessor heightAccessor)
	{
		List<BlockState> layers = plan.getLayers();
		int minY = HeightCompat.minY(heightAccessor);
		int firstY = Math.max(plan.getBaseY(), minY);
		int lastY = Math.min(plan.getBaseY() + layers.size() - 1, HeightCompat.maxYInclusive(heightAccessor));
		if (lastY < firstY)
		{
			return new NoiseColumn(minY, new BlockState[0]);
		}
		BlockState[] column = new BlockState[lastY - firstY + 1];
		for (int y = firstY; y <= lastY; y++)
		{
			column[y - firstY] = layers.get(y - plan.getBaseY());
		}
		return new NoiseColumn(firstY, column);
	}
}
