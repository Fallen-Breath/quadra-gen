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

import net.minecraft.world.level.biome.Biome;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

//#if MC >= 1.18.2
import net.minecraft.core.Holder;
//#endif

public final class BiomeLocateContext
{
	private static final ThreadLocal<BiomeLocateContext> CURRENT = new ThreadLocal<BiomeLocateContext>();

	//#if MC >= 1.18.2
	private final Set<Holder<Biome>> candidates;
	//#else
	//$$ private final Set<Biome> candidates;
	//#endif
	private final LevelContext levelContext;

	//#if MC >= 1.18.2
	private BiomeLocateContext(LevelContext levelContext, Set<Holder<Biome>> candidates)
	//#else
	//$$ private BiomeLocateContext(LevelContext levelContext, Set<Biome> candidates)
	//#endif
	{
		this.levelContext = levelContext;
		this.candidates = Collections.unmodifiableSet(new LinkedHashSet<>(candidates));
	}

	public static BiomeLocateContext current()
	{
		return CURRENT.get();
	}

	//#if MC >= 1.18.2
	public static BiomeLocateContext install(LevelContext levelContext, Set<Holder<Biome>> candidates)
	//#else
	//$$ public static BiomeLocateContext install(LevelContext levelContext, Set<Biome> candidates)
	//#endif
	{
		BiomeLocateContext previous = CURRENT.get();
		CURRENT.set(new BiomeLocateContext(levelContext, candidates));
		return previous;
	}

	public static void restore(BiomeLocateContext previous)
	{
		if (previous == null)
		{
			CURRENT.remove();
		}
		else
		{
			CURRENT.set(previous);
		}
	}

	//#if MC >= 1.18.2
	public Set<Holder<Biome>> getCandidates()
	//#else
	//$$ public Set<Biome> getCandidates()
	//#endif
	{
		return this.candidates;
	}

	public LevelContext getLevelContext()
	{
		return this.levelContext;
	}
}
