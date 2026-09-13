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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.fallenbreath.quadragen.QuadraGenMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigLoader
{
	private static final Gson GSON = new GsonBuilder().create();
	private static final String DEFAULT_CONFIG_RESOURCE = "/default_config.json";

	private ConfigLoader()
	{
	}

	public static QuadraGenConfig loadOrCreate()
	{
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve(QuadraGenMod.MOD_ID).resolve("config.json");
		try
		{
			if (Files.notExists(configPath))
			{
				Files.createDirectories(configPath.getParent());
				copyDefault(configPath);
				QuadraGenMod.LOGGER.info("Created default config at {}", configPath.toAbsolutePath());
			}
			try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8))
			{
				QuadraGenConfig config = GSON.fromJson(reader, QuadraGenConfig.class);
				//#if MC < 1.16.5
				normalizeLegacyNetherBiome(config);
				//#endif
				ConfigValidator.validate(config);
				return config;
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
			throw new ConfigValidationException("$", "invalid configuration in " + configPath.toAbsolutePath(), e);
		}
	}

	//#if MC < 1.16.5
	private static void normalizeLegacyNetherBiome(QuadraGenConfig config)
	{
		if (config == null || config.nether == null || config.nether.quadrants == null)
		{
			return;
		}
		for (QuadrantConfig quadrant : config.nether.quadrants.values())
		{
			if (quadrant != null && quadrant.flat != null && "minecraft:nether_wastes".equals(quadrant.flat.biome))
			{
				quadrant.flat.biome = "minecraft:nether";
			}
		}
	}
	//#endif

	private static void copyDefault(Path configPath) throws IOException
	{
		InputStream resource = ConfigLoader.class.getResourceAsStream(DEFAULT_CONFIG_RESOURCE);
		if (resource == null)
		{
			throw new IOException("missing bundled default config resource " + DEFAULT_CONFIG_RESOURCE);
		}
		try (InputStream input = resource)
		{
			Files.copy(input, configPath);
		}
	}
}
