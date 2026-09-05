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

package me.fallenbreath.quadragen.config;

import me.fallenbreath.quadragen.core.GeneratorKind;
import me.fallenbreath.quadragen.core.Quadrant;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ConfigDefaults
{
	private ConfigDefaults()
	{
	}

	public static QuadraGenConfig create()
	{
		Map<Quadrant, QuadrantConfig> quadrants = new EnumMap<Quadrant, QuadrantConfig>(Quadrant.class);
		quadrants.put(Quadrant.X_POSITIVE_Z_POSITIVE, noise(false));
		quadrants.put(Quadrant.X_NEGATIVE_Z_POSITIVE, noise(true));
		quadrants.put(Quadrant.X_NEGATIVE_Z_NEGATIVE, flat("minecraft:the_void", Collections.<FlatLayerConfig>emptyList()));
		quadrants.put(Quadrant.X_POSITIVE_Z_NEGATIVE, flat("minecraft:plains", Collections.singletonList(new FlatLayerConfig("minecraft:white_stained_glass", 1))));
		return new QuadraGenConfig(true, quadrants);
	}

	private static QuadrantConfig noise(boolean clear)
	{
		return new QuadrantConfig(GeneratorKind.NOISE, clear, null);
	}

	private static QuadrantConfig flat(String biome, List<FlatLayerConfig> layers)
	{
		return new QuadrantConfig(GeneratorKind.FLAT, false, new FlatConfig(biome, layers));
	}
}
