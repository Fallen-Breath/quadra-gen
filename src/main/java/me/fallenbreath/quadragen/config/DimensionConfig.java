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

import com.google.gson.annotations.SerializedName;
import me.fallenbreath.quadragen.core.Quadrant;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DimensionConfig
{
	@SerializedName("enabled")
	Boolean enabled;
	@SerializedName("advertised_world_type")
	AdvertisedWorldType advertisedWorldType;
	@SerializedName("quadrants")
	Map<String, QuadrantConfig> quadrants;

	public DimensionConfig(boolean enabled, AdvertisedWorldType advertisedWorldType, Map<Quadrant, QuadrantConfig> quadrants)
	{
		this.enabled = enabled;
		this.advertisedWorldType = advertisedWorldType;
		Map<String, QuadrantConfig> values = new LinkedHashMap<String, QuadrantConfig>();
		for (Map.Entry<Quadrant, QuadrantConfig> entry : quadrants.entrySet())
		{
			values.put(entry.getKey().getConfigKey(), entry.getValue());
		}
		this.quadrants = Collections.unmodifiableMap(values);
	}

	public AdvertisedWorldType getAdvertisedWorldType()
	{
		return this.advertisedWorldType;
	}

	public QuadrantConfig getQuadrant(Quadrant quadrant)
	{
		QuadrantConfig config = this.quadrants.get(quadrant.getConfigKey());
		if (config == null)
		{
			throw new IllegalArgumentException("Missing quadrant " + quadrant.getConfigKey());
		}
		return config;
	}

	public boolean isEnabled()
	{
		return this.enabled;
	}
}
