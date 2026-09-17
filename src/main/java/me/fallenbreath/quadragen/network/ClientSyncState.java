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

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class ClientSyncState
{
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
		state = new State(
				NbtCompat.getBoolean(data, "active", false),
				readQuadrantInfos(NbtCompat.getCompoundOrEmpty(data, "quadrants"))
		);
	}

	public static State getState()
	{
		return state;
	}

	private static Map<Quadrant, QuadrantInfo> readQuadrantInfos(CompoundTag quadrants)
	{
		Map<Quadrant, QuadrantInfo> infos = new EnumMap<Quadrant, QuadrantInfo>(Quadrant.class);
		for (Quadrant quadrant : Quadrant.values())
		{
			CompoundTag info = NbtCompat.getCompoundOrEmpty(quadrants, quadrant.getConfigKey());
			boolean flat = NbtCompat.getBoolean(info, "is_flat", false);
			infos.put(quadrant, new QuadrantInfo(flat, flat ? NbtCompat.getInt(info, "sea_level", 0) : 0));
		}
		return infos;
	}

	public static final class State
	{
		private final boolean active;
		private final Map<Quadrant, QuadrantInfo> quadrants;

		private State(boolean active, Map<Quadrant, QuadrantInfo> quadrants)
		{
			this.active = active;
			this.quadrants = Collections.unmodifiableMap(quadrants);
		}

		private static State inactive()
		{
			return new State(false, readQuadrantInfos(new CompoundTag()));
		}

		public boolean isActive()
		{
			return this.active;
		}

		public boolean isFlat(Quadrant quadrant)
		{
			return this.getInfo(quadrant).isFlat();
		}

		public int getSeaLevel(Quadrant quadrant)
		{
			return this.getInfo(quadrant).getSeaLevel();
		}

		private QuadrantInfo getInfo(Quadrant quadrant)
		{
			QuadrantInfo info = this.quadrants.get(quadrant);
			if (info == null)
			{
				throw new IllegalArgumentException("Missing quadrant info " + quadrant);
			}
			return info;
		}
	}

	private static final class QuadrantInfo
	{
		private final boolean flat;
		private final int seaLevel;

		private QuadrantInfo(boolean flat, int seaLevel)
		{
			this.flat = flat;
			this.seaLevel = seaLevel;
		}

		private boolean isFlat()
		{
			return this.flat;
		}

		private int getSeaLevel()
		{
			return this.seaLevel;
		}
	}
}
