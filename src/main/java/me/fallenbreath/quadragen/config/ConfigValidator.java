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

import java.util.List;
import java.util.Map;

public class ConfigValidator
{
	private ConfigValidator()
	{
	}

	static void validate(QuadraGenConfig config)
	{
		if (config == null)
		{
			throw error("$", "expected an object");
		}
		if (config.schemaVersion == null)
		{
			throw error("$.schema_version", "missing required field");
		}
		if (config.schemaVersion != QuadraGenConfig.SCHEMA_VERSION)
		{
			throw error("$.schema_version", "expected " + QuadraGenConfig.SCHEMA_VERSION + ", got " + config.schemaVersion);
		}
		requireBoolean(config.enabled, "$.enabled");
		requireBoolean(config.enabledInSingleplayer, "$.enabled_in_singleplayer");
		validateDimension(config.overworld, "$.overworld");
		validateDimension(config.nether, "$.nether");
	}

	private static void validateDimension(DimensionConfig config, String path)
	{
		if (config == null)
		{
			throw error(path, "missing required field");
		}
		requireBoolean(config.enabled, path + ".enabled");
		if (config.advertisedWorldType == null)
		{
			throw error(path + ".advertised_world_type", "missing or invalid world type");
		}
		Map<String, QuadrantConfig> quadrants = config.quadrants;
		if (quadrants == null)
		{
			throw error(path + ".quadrants", "missing required field");
		}
		for (Quadrant quadrant : Quadrant.values())
		{
			String quadrantPath = path + ".quadrants." + quadrant.getConfigKey();
			QuadrantConfig quadrantConfig = quadrants.get(quadrant.getConfigKey());
			if (quadrantConfig == null)
			{
				throw error(quadrantPath, "missing required quadrant");
			}
			validateQuadrant(quadrantConfig, quadrantPath);
		}
	}

	private static void validateQuadrant(QuadrantConfig config, String path)
	{
		if (config.generator == null)
		{
			throw error(path + ".generator", "missing or invalid generator");
		}
		requireBoolean(config.clearGeneratedContent, path + ".clear_generated_content");
		if (config.generator == GeneratorKind.NOISE)
		{
			if (config.flat != null)
			{
				throw error(path + ".flat", "is only valid when generator is 'flat'");
			}
			return;
		}
		if (config.flat == null)
		{
			throw error(path + ".flat", "missing required flat settings");
		}
		validateFlat(config.flat, path + ".flat");
	}

	private static void validateFlat(FlatConfig config, String path)
	{
		requireString(config.biome, path + ".biome");
		if (config.layers == null)
		{
			throw error(path + ".layers", "missing required field");
		}
		List<FlatLayerConfig> layers = config.layers;
		for (int i = 0; i < layers.size(); i++)
		{
			FlatLayerConfig layer = layers.get(i);
			String layerPath = path + ".layers[" + i + "]";
			if (layer == null)
			{
				throw error(layerPath, "expected an object");
			}
			requireString(layer.block, layerPath + ".block");
			if (layer.count == null || layer.count < 1)
			{
				throw error(layerPath + ".count", "must be a positive integer");
			}
		}
	}

	private static void requireBoolean(Boolean value, String path)
	{
		if (value == null)
		{
			throw error(path, "missing required boolean");
		}
	}

	private static void requireString(String value, String path)
	{
		if (value == null || value.isEmpty())
		{
			throw error(path, "must be a non-empty string");
		}
	}

	private static ConfigValidationException error(String path, String message)
	{
		return new ConfigValidationException(path, message);
	}
}
