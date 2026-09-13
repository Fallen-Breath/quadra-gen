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

public final class QuadraGenConfig
{
	public static final int SCHEMA_VERSION = 1;

	@SerializedName("schema_version")
	Integer schemaVersion;
	@SerializedName("enabled")
	Boolean enabled;
	@SerializedName("enabled_in_singleplayer")
	Boolean enabledInSingleplayer;
	@SerializedName("nether")
	DimensionConfig nether;
	@SerializedName("overworld")
	DimensionConfig overworld;

	public QuadraGenConfig(boolean enabled, boolean enabledInSingleplayer, DimensionConfig overworld, DimensionConfig nether)
	{
		this.schemaVersion = SCHEMA_VERSION;
		this.enabled = enabled;
		this.enabledInSingleplayer = enabledInSingleplayer;
		this.nether = nether;
		this.overworld = overworld;
	}

	public boolean isEnabled()
	{
		return this.enabled;
	}

	public boolean isEnabledInSingleplayer()
	{
		return this.enabledInSingleplayer;
	}

	public DimensionConfig getNether()
	{
		return this.nether;
	}

	public DimensionConfig getOverworld()
	{
		return this.overworld;
	}
}
