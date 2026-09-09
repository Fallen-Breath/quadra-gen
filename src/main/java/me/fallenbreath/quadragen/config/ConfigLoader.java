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

import com.google.gson.*;
import me.fallenbreath.quadragen.QuadraGen;
import me.fallenbreath.quadragen.core.GeneratorKind;
import me.fallenbreath.quadragen.core.Quadrant;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class ConfigLoader
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Set<String> ROOT_FIELDS = setOf("schema_version", "enabled", "enabled_in_singleplayer", "overworld", "nether");
	private static final Set<String> DIMENSION_FIELDS = setOf("enabled", "quadrants");
	private static final Set<String> QUADRANT_FIELDS = setOf("generator", "clear_generated_content", "flat");
	private static final Set<String> FLAT_FIELDS = setOf("biome", "layers");
	private static final Set<String> LAYER_FIELDS = setOf("block", "count");

	private ConfigLoader()
	{
	}

	public static QuadraGenConfig loadOrCreate()
	{
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve(QuadraGen.MOD_ID).resolve("config.json");
		try
		{
			if (Files.notExists(configPath))
			{
				Files.createDirectories(configPath.getParent());
				writeDefault(configPath);
				QuadraGen.LOGGER.info("Created default config at {}", configPath.toAbsolutePath());
			}
			try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8))
			{
				//#if MC >= 1.18.2
				JsonElement root = JsonParser.parseReader(reader);
				//#elseif MC >= 1.15.2
				//$$ JsonElement root = new JsonParser().parse(reader);
				//#else
				//$$ // TODO: Port JSON parsing against the target Gson version.
				//$$ JsonElement root = TODO_PORT_MC_VERSION;
				//#endif
				return parseRoot(requireObject(root, "$"));
			}
		}
		catch (IOException e)
		{
			throw new ConfigValidationException("$", "failed to read or create " + configPath.toAbsolutePath(), e);
		}
		catch (ConfigValidationException e)
		{
			throw e;
		}
		catch (RuntimeException e)
		{
			throw new ConfigValidationException("$", "invalid JSON in " + configPath.toAbsolutePath(), e);
		}
	}

	private static void writeDefault(Path path) throws IOException
	{
		try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8))
		{
			GSON.toJson(toJson(ConfigDefaults.create()), writer);
			writer.write(System.lineSeparator());
		}
	}

	private static QuadraGenConfig parseRoot(JsonObject root)
	{
		checkFields(root, ROOT_FIELDS, "$");
		int schemaVersion = requireInt(root, "schema_version", "$", 1);
		if (schemaVersion != QuadraGenConfig.SCHEMA_VERSION)
		{
			throw error("$.schema_version", "expected " + QuadraGenConfig.SCHEMA_VERSION + ", got " + schemaVersion);
		}
		boolean enabled = requireBoolean(root, "enabled", "$");
		boolean enabledInSingleplayer = requireBoolean(root, "enabled_in_singleplayer", "$");
		DimensionConfig overworld = parseDimension(requireObject(require(root, "overworld", "$"), "$.overworld"), "$.overworld");
		DimensionConfig nether = parseDimension(requireObject(require(root, "nether", "$"), "$.nether"), "$.nether");
		return new QuadraGenConfig(enabled, enabledInSingleplayer, overworld, nether);
	}

	private static DimensionConfig parseDimension(JsonObject object, String path)
	{
		checkFields(object, DIMENSION_FIELDS, path);
		boolean enabled = requireBoolean(object, "enabled", path);
		JsonObject quadrantsObject = requireObject(require(object, "quadrants", path), path + ".quadrants");
		Set<String> quadrantKeys = new HashSet<String>();
		for (Quadrant quadrant : Quadrant.values())
		{
			quadrantKeys.add(quadrant.getConfigKey());
		}
		checkFields(quadrantsObject, quadrantKeys, path + ".quadrants");

		Map<Quadrant, QuadrantConfig> quadrants = new EnumMap<Quadrant, QuadrantConfig>(Quadrant.class);
		for (Quadrant quadrant : Quadrant.values())
		{
			String quadrantPath = path + ".quadrants." + quadrant.getConfigKey();
			JsonElement value = quadrantsObject.get(quadrant.getConfigKey());
			if (value == null)
			{
				throw error(quadrantPath, "missing required quadrant");
			}
			quadrants.put(quadrant, parseQuadrant(requireObject(value, quadrantPath), quadrantPath));
		}
		return new DimensionConfig(enabled, quadrants);
	}

	private static QuadrantConfig parseQuadrant(JsonObject object, String path)
	{
		checkFields(object, QUADRANT_FIELDS, path);
		String generatorName = requireString(object, "generator", path);
		GeneratorKind generator = GeneratorKind.fromConfigValue(generatorName);
		if (generator == null)
		{
			throw error(path + ".generator", "expected 'noise' or 'flat', got '" + generatorName + "'");
		}
		boolean clear = requireBoolean(object, "clear_generated_content", path);
		JsonElement flatElement = object.get("flat");
		if (generator == GeneratorKind.NOISE)
		{
			if (flatElement != null)
			{
				throw error(path + ".flat", "is only valid when generator is 'flat'");
			}
			return new QuadrantConfig(generator, clear, null);
		}
		if (flatElement == null)
		{
			throw error(path + ".flat", "missing required flat settings");
		}
		return new QuadrantConfig(generator, clear, parseFlat(requireObject(flatElement, path + ".flat"), path + ".flat"));
	}

	private static FlatConfig parseFlat(JsonObject object, String path)
	{
		checkFields(object, FLAT_FIELDS, path);
		String biome = requireString(object, "biome", path);
		JsonElement layersElement = require(object, "layers", path);
		if (!layersElement.isJsonArray())
		{
			throw error(path + ".layers", "expected an array");
		}
		JsonArray layersArray = layersElement.getAsJsonArray();
		java.util.List<FlatLayerConfig> layers = new java.util.ArrayList<FlatLayerConfig>();
		for (int i = 0; i < layersArray.size(); i++)
		{
			String layerPath = path + ".layers[" + i + "]";
			JsonObject layer = requireObject(layersArray.get(i), layerPath);
			checkFields(layer, LAYER_FIELDS, layerPath);
			String block = requireString(layer, "block", layerPath);
			int count = requireInt(layer, "count", layerPath, 1);
			layers.add(new FlatLayerConfig(block, count));
		}
		return new FlatConfig(biome, layers);
	}

	private static JsonObject toJson(QuadraGenConfig config)
	{
		JsonObject root = new JsonObject();
		root.addProperty("schema_version", QuadraGenConfig.SCHEMA_VERSION);
		root.addProperty("enabled", config.isEnabled());
		root.addProperty("enabled_in_singleplayer", config.isEnabledInSingleplayer());
		root.add("overworld", toJson(config.getOverworld()));
		root.add("nether", toJson(config.getNether()));
		return root;
	}

	private static JsonObject toJson(DimensionConfig config)
	{
		JsonObject dimension = new JsonObject();
		dimension.addProperty("enabled", config.isEnabled());
		JsonObject quadrants = new JsonObject();
		for (Quadrant quadrant : Quadrant.values())
		{
			QuadrantConfig quadrantConfig = config.getQuadrant(quadrant);
			JsonObject object = new JsonObject();
			object.addProperty("generator", quadrantConfig.getGenerator().getConfigValue());
			object.addProperty("clear_generated_content", quadrantConfig.isClearGeneratedContent());
			if (quadrantConfig.getFlat() != null)
			{
				JsonObject flat = new JsonObject();
				flat.addProperty("biome", quadrantConfig.getFlat().getBiome());
				JsonArray layers = new JsonArray();
				for (FlatLayerConfig layerConfig : quadrantConfig.getFlat().getLayers())
				{
					JsonObject layer = new JsonObject();
					layer.addProperty("block", layerConfig.getBlock());
					layer.addProperty("count", layerConfig.getCount());
					layers.add(layer);
				}
				flat.add("layers", layers);
				object.add("flat", flat);
			}
			quadrants.add(quadrant.getConfigKey(), object);
		}
		dimension.add("quadrants", quadrants);
		return dimension;
	}

	private static JsonElement require(JsonObject object, String name, String path)
	{
		JsonElement value = object.get(name);
		if (value == null)
		{
			throw error(path + "." + name, "missing required field");
		}
		return value;
	}

	private static JsonObject requireObject(JsonElement element, String path)
	{
		if (!element.isJsonObject())
		{
			throw error(path, "expected an object");
		}
		return element.getAsJsonObject();
	}

	private static String requireString(JsonObject object, String name, String path)
	{
		JsonElement value = require(object, name, path);
		if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
		{
			throw error(path + "." + name, "expected a string");
		}
		String result = value.getAsString();
		if (result.isEmpty())
		{
			throw error(path + "." + name, "must not be empty");
		}
		return result;
	}

	private static boolean requireBoolean(JsonObject object, String name, String path)
	{
		JsonElement value = require(object, name, path);
		if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean())
		{
			throw error(path + "." + name, "expected a boolean");
		}
		return value.getAsBoolean();
	}

	private static int requireInt(JsonObject object, String name, String path, int minimum)
	{
		JsonElement value = require(object, name, path);
		if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber())
		{
			throw error(path + "." + name, "expected an integer");
		}
		try
		{
			java.math.BigDecimal decimal = value.getAsBigDecimal();
			int result = decimal.intValueExact();
			if (result < minimum)
			{
				throw error(path + "." + name, "must be at least " + minimum);
			}
			return result;
		}
		catch (ArithmeticException e)
		{
			throw error(path + "." + name, "expected a 32-bit integer");
		}
	}

	private static void checkFields(JsonObject object, Set<String> allowed, String path)
	{
		for (Map.Entry<String, JsonElement> entry : object.entrySet())
		{
			String key = entry.getKey();
			if (!allowed.contains(key))
			{
				throw error(path + "." + key, "unknown field");
			}
		}
	}

	private static ConfigValidationException error(String path, String message)
	{
		return new ConfigValidationException(path, message);
	}

	private static Set<String> setOf(String... values)
	{
		return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(values)));
	}
}
