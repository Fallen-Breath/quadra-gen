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

public final class QuadrantRouter
{
	public Quadrant forBlock(int blockX, int blockZ)
	{
		return select(blockX >= 0, blockZ >= 0);
	}

	public Quadrant forChunk(int chunkX, int chunkZ)
	{
		return select(chunkX >= 0, chunkZ >= 0);
	}

	private static Quadrant select(boolean xPositive, boolean zPositive)
	{
		if (xPositive)
		{
			return zPositive ? Quadrant.X_POSITIVE_Z_POSITIVE : Quadrant.X_POSITIVE_Z_NEGATIVE;
		}
		return zPositive ? Quadrant.X_NEGATIVE_Z_POSITIVE : Quadrant.X_NEGATIVE_Z_NEGATIVE;
	}
}
