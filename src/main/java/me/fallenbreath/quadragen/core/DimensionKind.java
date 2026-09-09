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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

//#if MC < 1.16.5
//$$ import net.minecraft.world.level.dimension.DimensionType;
//#endif

public enum DimensionKind
{
	OVERWORLD,
	NETHER,
	UNSUPPORTED;

	public static DimensionKind from(ServerLevel level)
	{
		//#if MC >= 1.16.5
		if (Level.OVERWORLD.equals(level.dimension()))
		{
			return OVERWORLD;
		}
		if (Level.NETHER.equals(level.dimension()))
		{
			return NETHER;
		}
		//#else
		//$$ if (level.getDimension().getType() == DimensionType.OVERWORLD)
		//$$ {
		//$$ 	return OVERWORLD;
		//$$ }
		//$$ if (level.getDimension().getType() == DimensionType.NETHER)
		//$$ {
		//$$ 	return NETHER;
		//$$ }
		//#endif
		return UNSUPPORTED;
	}
}
