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

package me.fallenbreath.quadragen.gameplay;

import me.fallenbreath.quadragen.QuadraGen;
import me.fallenbreath.quadragen.core.Quadrant;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import net.minecraft.world.level.ChunkPos;

public final class InitialSpawnPolicy
{
	private static final int AXIS_CLEARANCE_CHUNKS = 5;

	private InitialSpawnPolicy()
	{
	}

	public static ChunkPos selectAnchor(LevelContext context, ChunkPos vanillaAnchor)
	{
		ChunkPos noise = nearestEligible(context, vanillaAnchor, true);
		if (noise != null)
		{
			return noise;
		}
		ChunkPos flat = nearestEligible(context, vanillaAnchor, false);
		if (flat != null)
		{
			return flat;
		}
		QuadraGen.LOGGER.warn("No ordinary Noise or non-empty safe Flat quadrant is available for initial spawn; keeping vanilla anchor {}", vanillaAnchor);
		return vanillaAnchor;
	}

	public static boolean hasSpawnCandidate(LevelContext context)
	{
		for (Quadrant quadrant : Quadrant.values())
		{
			QuadrantPlan plan = context.getGenerationPlan().get(quadrant);
			if (plan.isOrdinaryNoise() || plan.hasSafeFlatSurface())
			{
				return true;
			}
		}
		return false;
	}

	private static ChunkPos nearestEligible(LevelContext context, ChunkPos origin, boolean requireNoise)
	{
		ChunkPos best = null;
		long bestDistance = Long.MAX_VALUE;
		for (Quadrant quadrant : Quadrant.values())
		{
			QuadrantPlan plan = context.getGenerationPlan().get(quadrant);
			boolean eligible = requireNoise ? plan.isOrdinaryNoise() : plan.hasSafeFlatSurface();
			if (!eligible)
			{
				continue;
			}
			int x = project(origin.x(), quadrant.getXSign());
			int z = project(origin.z(), quadrant.getZSign());
			long dx = (long)x - origin.x();
			long dz = (long)z - origin.z();
			long distance = dx * dx + dz * dz;
			if (distance < bestDistance)
			{
				bestDistance = distance;
				best = new ChunkPos(x, z);
			}
		}
		return best;
	}

	private static int project(int coordinate, int sign)
	{
		return sign > 0 ? Math.max(coordinate, AXIS_CLEARANCE_CHUNKS) : Math.min(coordinate, -AXIS_CLEARANCE_CHUNKS - 1);
	}
}
