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

import me.fallenbreath.quadragen.QuadraGen;
import me.fallenbreath.quadragen.compat.DimensionCompat;
import me.fallenbreath.quadragen.compat.RegistryCompat;
import me.fallenbreath.quadragen.config.ConfigValidationException;
import me.fallenbreath.quadragen.config.FlatConfig;
import me.fallenbreath.quadragen.config.FlatLayerConfig;
import me.fallenbreath.quadragen.config.QuadraGenConfig;
import me.fallenbreath.quadragen.config.QuadrantConfig;
import me.fallenbreath.quadragen.core.FlatGenerationPlan;
import me.fallenbreath.quadragen.core.GenerationPlan;
import me.fallenbreath.quadragen.core.Quadrant;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.core.QuadrantRouter;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import me.fallenbreath.quadragen.runtime.access.ServerLevelContextAccess;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class LevelBootstrap
{
	private LevelBootstrap()
	{
	}

	public static void install(ServerLevel level, ChunkGenerator generator)
	{
		QuadraGenConfig config = QuadraGen.getConfig();
		if (!config.isEnabled() || !DimensionCompat.isOverworld(level))
		{
			return;
		}
		if (!(generator instanceof NoiseBasedChunkGenerator))
		{
			QuadraGen.LOGGER.warn("Quadra Gen is enabled, but the Overworld generator is {}; this world is left untouched", generator.getClass().getName());
			return;
		}
		Map<Quadrant, QuadrantPlan> resolved = new EnumMap<Quadrant, QuadrantPlan>(Quadrant.class);
		for (Quadrant quadrant : Quadrant.values())
		{
			QuadrantConfig raw = config.getQuadrant(quadrant);
			if (raw.getFlat() == null)
			{
				resolved.put(quadrant, new QuadrantPlan(raw.getGenerator(), raw.isClearGeneratedContent(), null));
			}
			else
			{
				resolved.put(quadrant, new QuadrantPlan(raw.getGenerator(), raw.isClearGeneratedContent(), resolveFlat(level, quadrant, raw.getFlat())));
			}
		}

		LevelContext context = new LevelContext(new GenerationPlan(resolved), new QuadrantRouter(), (NoiseBasedChunkGenerator)generator);
		((ServerLevelContextAccess)level).quadragen$setLevelContext(context);
		((GeneratorContextAccess)generator).quadragen$setLevelContext(context);
		QuadraGen.LOGGER.info("Quadra Gen world-generation routing installed for {}", level.dimension().identifier());
	}

	private static FlatGenerationPlan resolveFlat(ServerLevel level, Quadrant quadrant, FlatConfig raw)
	{
		String basePath = "$.quadrants." + quadrant.getConfigKey() + ".flat";
		Holder<Biome> biome = RegistryCompat.resolveBiome(level.registryAccess(), raw.getBiome(), basePath + ".biome");
		List<BlockState> layers = new ArrayList<BlockState>();
		long layerCount = 0L;
		for (int i = 0; i < raw.getLayers().size(); i++)
		{
			FlatLayerConfig layer = raw.getLayers().get(i);
			layerCount += layer.getCount();
			if (layerCount > Integer.MAX_VALUE || level.getMinY() + layerCount - 1L > level.getMaxY())
			{
				throw new ConfigValidationException(basePath + ".layers[" + i + "].count", "flat layers exceed the world's maximum build Y " + level.getMaxY());
			}
			BlockState state = RegistryCompat.resolveBlock(layer.getBlock(), basePath + ".layers[" + i + "].block");
			for (int j = 0; j < layer.getCount(); j++)
			{
				layers.add(state);
			}
		}
		return new FlatGenerationPlan(level.getMinY(), biome, layers);
	}
}
