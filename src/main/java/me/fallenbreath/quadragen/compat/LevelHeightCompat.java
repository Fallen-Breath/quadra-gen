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

package me.fallenbreath.quadragen.compat;

import net.minecraft.world.level.LevelHeightAccessor;

public final class LevelHeightCompat
{
	private LevelHeightCompat()
	{
	}

	public static int minY(LevelHeightAccessor accessor)
	{
		//#if MC >= 1.21.3
		return accessor.getMinY();
		//#elseif MC >= 1.20.6
		//$$ return accessor.getMinBuildHeight();
		//#else
		//$$ // TODO: Port this method against the target MC source.
		//$$ return TODO_PORT_MC_VERSION;
		//#endif
	}

	public static int maxYInclusive(LevelHeightAccessor accessor)
	{
		//#if MC >= 1.21.3
		return accessor.getMaxY();
		//#elseif MC >= 1.20.6
		//$$ return accessor.getMaxBuildHeight() - 1;
		//#else
		//$$ // TODO: Port this method against the target MC source.
		//$$ return TODO_PORT_MC_VERSION;
		//#endif
	}

	public static boolean contains(LevelHeightAccessor accessor, int y)
	{
		return !accessor.isOutsideBuildHeight(y);
	}
}
