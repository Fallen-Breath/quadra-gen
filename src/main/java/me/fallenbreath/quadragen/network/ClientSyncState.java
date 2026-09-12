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

package me.fallenbreath.quadragen.network;

import me.fallenbreath.quadragen.compat.NbtCompat;
import me.fallenbreath.quadragen.core.Quadrant;
import net.minecraft.nbt.CompoundTag;

public final class ClientSyncState
{
	private static final int QUADRANT_COUNT = 4;
	private static volatile State state = State.inactive();

	private ClientSyncState()
	{
	}

	public static void reset()
	{
		state = State.inactive();
	}

	public static void apply(CompoundTag data)
	{
		boolean active = NbtCompat.getBoolean(data, "active", false);
		int flatQuadrants = NbtCompat.getInt(data, "flat_quadrants", 0) & ((1 << QUADRANT_COUNT) - 1);
		int[] flatSeaLevels = NbtCompat.getIntArray(data, "flat_sea_levels");
		int[] normalizedSeaLevels = new int[QUADRANT_COUNT];
		for (int i = 0; i < normalizedSeaLevels.length && i < flatSeaLevels.length; i++)
		{
			normalizedSeaLevels[i] = flatSeaLevels[i];
		}
		state = new State(active, flatQuadrants, normalizedSeaLevels);
	}

	public static State getState()
	{
		return state;
	}

	public static final class State
	{
		private final boolean active;
		private final int flatQuadrants;
		private final int[] flatSeaLevels;

		private State(boolean active, int flatQuadrants, int[] flatSeaLevels)
		{
			this.active = active;
			this.flatQuadrants = flatQuadrants;
			this.flatSeaLevels = flatSeaLevels;
		}

		private static State inactive()
		{
			return new State(false, 0, new int[QUADRANT_COUNT]);
		}

		public boolean isActive()
		{
			return this.active;
		}

		public int getFlatQuadrants()
		{
			return this.flatQuadrants;
		}

		public int[] getFlatSeaLevels()
		{
			return this.flatSeaLevels.clone();
		}

		public int getFlatSeaLevel(Quadrant quadrant)
		{
			return this.flatSeaLevels[quadrant.ordinal()];
		}
	}
}
