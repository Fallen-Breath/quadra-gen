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

import me.fallenbreath.quadragen.compat.SeaLevelCompat;
import me.fallenbreath.quadragen.runtime.access.ServerLevelContextAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

public final class SeaLevelQuery
{
	private SeaLevelQuery()
	{
	}

	public static int getSeaLevelAt(LevelReader level, BlockPos pos, int originalSeaLevel)
	{
		return SeaLevelQuery.getSeaLevelAt(level, pos.getX(), pos.getZ(), originalSeaLevel);
	}

	public static int getSeaLevelAt(LevelReader level, int blockX, int blockZ, int originalSeaLevel)
	{
		// Do not unwrap ServerLevelAccessor: WorldGenRegion must retain the Noise sea level.
		if (level instanceof ServerLevelContextAccess)
		{
			LevelContext context = ((ServerLevelContextAccess)level).getLevelContext$quadragen();
			if (context != null && context.getPlanAt(blockX, blockZ).isFlat())
			{
				return SeaLevelCompat.getFlatSeaLevel();
			}
		}
		return originalSeaLevel;
	}
}
