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

//#if MC >= 1.17.1
import net.minecraft.world.level.LevelHeightAccessor;
//#else
//$$ import net.minecraft.world.level.Level;
//$$ import net.minecraft.world.level.LevelAccessor;
//#endif

public final class LevelHeightCompat
{
	private LevelHeightCompat()
	{
	}

	public static int minY(
			//#if MC >= 1.17.1
			LevelHeightAccessor accessor
			//#else
			//$$ LevelAccessor accessor
			//#endif
	)
	{
		//#if MC >= 1.21.3
		return accessor.getMinY();
		//#elseif MC >= 1.17.1
		//$$ return accessor.getMinBuildHeight();
		//#else
		//$$ return 0;
		//#endif
	}

	public static int maxYInclusive(
			//#if MC >= 1.17.1
			LevelHeightAccessor accessor
			//#else
			//$$ LevelAccessor accessor
			//#endif
	)
	{
		//#if MC >= 1.21.3
		return accessor.getMaxY();
		//#elseif MC >= 1.17.1
		//$$ return accessor.getMaxBuildHeight() - 1;
		//#else
		//$$ return 255;
		//#endif
	}

	public static boolean contains(
			//#if MC >= 1.17.1
			LevelHeightAccessor accessor,
			//#else
			//$$ LevelAccessor accessor,
			//#endif
			int y)
	{
		//#if MC >= 1.17.1
		return !accessor.isOutsideBuildHeight(y);
		//#else
		//$$ return !Level.isOutsideBuildHeight(y);
		//#endif
	}
}
