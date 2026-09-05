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

import me.fallenbreath.quadragen.compat.ChunkPosCompat;
import me.fallenbreath.quadragen.core.Quadrant;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class LevelContext
{
	private final Map<Quadrant, QuadrantPlan> plans;
	private final NoiseBasedChunkGenerator noiseGenerator;

	public LevelContext(Map<Quadrant, QuadrantPlan> plans, NoiseBasedChunkGenerator noiseGenerator)
	{
		this.plans = Collections.unmodifiableMap(new EnumMap<Quadrant, QuadrantPlan>(plans));
		this.noiseGenerator = noiseGenerator;
	}

	public QuadrantPlan getPlan(Quadrant quadrant)
	{
		QuadrantPlan plan = this.plans.get(quadrant);
		if (plan == null)
		{
			throw new IllegalArgumentException("Missing quadrant plan " + quadrant);
		}
		return plan;
	}

	public NoiseBasedChunkGenerator getNoiseGenerator()
	{
		return this.noiseGenerator;
	}

	public QuadrantPlan getPlanAt(int x, int z)
	{
		return this.getPlan(Quadrant.fromCoordinates(x, z));
	}

	public QuadrantPlan getPlanAt(ChunkPos pos)
	{
		return this.getPlanAt(ChunkPosCompat.x(pos), ChunkPosCompat.z(pos));
	}
}
