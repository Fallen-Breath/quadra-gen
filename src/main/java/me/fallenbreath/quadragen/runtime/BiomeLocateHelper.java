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

import me.fallenbreath.quadragen.core.Quadrant;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import net.minecraft.world.level.biome.Biome;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

//#if MC >= 1.18.2
import net.minecraft.core.Holder;
//#endif

public final class BiomeLocateHelper
{
	private BiomeLocateHelper()
	{
	}

	//#if MC >= 1.18.2
	public static boolean canReturnNotFound(Set<Holder<Biome>> candidates, Predicate<Holder<Biome>> biomeTest)
	//#else
	//$$ public static boolean canReturnNotFound(Set<Biome> candidates, Predicate<Biome> biomeTest)
	//#endif
	{
		return candidates.stream().noneMatch(biomeTest);
	}

	//#if MC >= 1.18.2
	public static Set<Holder<Biome>> getCandidates(LevelContext context, Set<Quadrant> searchQuadrants, Collection<Holder<Biome>> noiseBiomes)
	//#else
	//$$ public static Set<Biome> getCandidates(LevelContext context, Set<Quadrant> searchQuadrants, Collection<Biome> noiseBiomes)
	//#endif
	{
		//#if MC >= 1.18.2
		Set<Holder<Biome>> candidates = new LinkedHashSet<>();
		//#else
		//$$ Set<Biome> candidates = new LinkedHashSet<>();
		//#endif
		boolean includesNoise = false;
		for (Quadrant quadrant : searchQuadrants)
		{
			QuadrantPlan plan = context.getPlan(quadrant);
			if (plan.isFlat())
			{
				candidates.add(plan.getFlat().getBiome());
			}
			else
			{
				includesNoise = true;
			}
		}
		if (includesNoise)
		{
			candidates.addAll(noiseBiomes);
		}
		return candidates;
	}

	//#if MC >= 1.19.4
	/** Mirrors the horizontal sampling bounds of {@link net.minecraft.world.level.biome.BiomeSource#findClosestBiome3d}. */
	//#elseif MC >= 1.16.5
	//$$ /** Mirrors the nearest-first quart sampling bounds of {@link net.minecraft.world.level.biome.BiomeSource#findBiomeHorizontal}. */
	//#else
	//$$ /** Used only by versions with a server biome-locate entry point. */
	//#endif
	public static Set<Quadrant> getSearchQuadrants(int blockX, int blockZ, int searchRadius, int sampleResolution)
	{
		//#if MC >= 1.19.4
		long centerX = blockX;
		long centerZ = blockZ;
		long radius = (long)(searchRadius / sampleResolution) * sampleResolution;
		//#else
		//$$ long centerX = (long)(blockX >> 2) << 2;
		//$$ long centerZ = (long)(blockZ >> 2) << 2;
		//$$ long radius = (long)((searchRadius >> 2) / sampleResolution) * sampleResolution << 2;
		//#endif
		long minX = centerX - radius;
		long maxX = centerX + radius;
		long minZ = centerZ - radius;
		long maxZ = centerZ + radius;
		// Keep a conservative window if vanilla's block-coordinate arithmetic can wrap.
		if (minX < Integer.MIN_VALUE || maxX > Integer.MAX_VALUE || minZ < Integer.MIN_VALUE || maxZ > Integer.MAX_VALUE)
		{
			return EnumSet.allOf(Quadrant.class);
		}
		Set<Quadrant> quadrants = EnumSet.noneOf(Quadrant.class);
		for (Quadrant quadrant : Quadrant.values())
		{
			boolean intersectsX = quadrant.getXSign() > 0 ? maxX >= 0 : minX < 0;
			boolean intersectsZ = quadrant.getZSign() > 0 ? maxZ >= 0 : minZ < 0;
			if (intersectsX && intersectsZ)
			{
				quadrants.add(quadrant);
			}
		}
		return quadrants;
	}
}
