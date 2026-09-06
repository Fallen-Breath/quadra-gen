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
 */

package me.fallenbreath.quadragen.compat;

import me.fallenbreath.quadragen.core.FlatGenerationPlan;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class FlatGeneratorCompat
{
	private FlatGeneratorCompat()
	{
	}

	@SuppressWarnings("deprecation")
	public static FlatLevelSource create(Holder<Biome> biome, List<BlockState> layers)
	{
		FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(Optional.empty(), biome, Collections.emptyList());
		List<FlatLayerInfo> layerInfos = settings.getLayersInfo();
		for (int index = 0; index < layers.size(); )
		{
			BlockState state = layers.get(index);
			int end = index + 1;
			while (end < layers.size() && layers.get(end).equals(state))
			{
				end++;
			}
			layerInfos.add(new FlatLayerInfo(end - index, state.getBlock()));
			index = end;
		}
		settings.updateLayers();
		FlatLevelSource generator = new FlatLevelSource(settings);
		// Initialize vanilla's layer split before fillFromNoise reads the settings.
		generator.getBiomeGenerationSettings(biome);
		return generator;
	}

	public static void placeDelayedLayers(WorldGenLevel level, ChunkAccess chunk, FlatGenerationPlan plan)
	{
		BlockPos origin = new BlockPos(chunk.getPos().getMinBlockX(), level.getMinY() + 1, chunk.getPos().getMinBlockZ());
		RandomSource random = RandomSource.create();
		List<BlockState> layers = plan.getLayers();
		for (int index = 0; index < layers.size(); index++)
		{
			if (plan.isDelayedLayer(index))
			{
				Feature.FILL_LAYER.place(new LayerConfiguration(index, layers.get(index)), level, plan.getFlatGenerator(), random, origin);
			}
		}
	}
}
