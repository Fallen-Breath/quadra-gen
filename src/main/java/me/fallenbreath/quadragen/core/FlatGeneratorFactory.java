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

package me.fallenbreath.quadragen.core;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

//#if MC >= 1.18.2
import net.minecraft.core.Holder;
//#elseif MC >= 1.17.1
//$$ import net.minecraft.world.level.levelgen.StructureSettings;
//#endif

public final class FlatGeneratorFactory
{
	private FlatGeneratorFactory()
	{
	}

	@SuppressWarnings("deprecation")
	public static FlatLevelSource create(
			RegistryAccess registryAccess,
			//#if MC >= 1.18.2
			Holder<Biome> biome,
			//#elseif MC >= 1.17.1
			//$$ Biome biome,
			//#else
			//$$ TODO_PORT_MC_VERSION biome,
			//#endif
			List<BlockState> layers)
	{
		//#if MC >= 1.19.4
		FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(Optional.empty(), biome, Collections.emptyList());
		//#elseif MC >= 1.18.2
		//$$ FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(Optional.empty(), registryAccess.registryOrThrow(Registry.BIOME_REGISTRY));
		//$$ settings.setBiome(biome);
		//#elseif MC >= 1.17.1
		//$$ FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(
		//$$ 		new StructureSettings(Optional.empty(), Collections.emptyMap()),
		//$$ 		registryAccess.registryOrThrow(Registry.BIOME_REGISTRY)
		//$$ );
		//$$ settings.setBiome(() -> biome);
		//#else
		//$$ // TODO: Port Flat settings construction against the target MC source.
		//$$ FlatLevelGeneratorSettings settings = TODO_PORT_MC_VERSION;
		//#endif
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
		//#if MC >= 1.19.4
		FlatLevelSource generator = new FlatLevelSource(settings);
		// Initialize {@link net.minecraft.world.level.levelgen.ChunkGenerator#getBiomeGenerationSettings} so the layer split
		// performed by {@link net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings#adjustGenerationSettings} is ready before
		// {@link net.minecraft.world.level.levelgen.FlatLevelSource#fillFromNoise} reads the settings.
		generator.getBiomeGenerationSettings(biome);
		return generator;
		//#elseif MC >= 1.18.2
		//$$ // {@link net.minecraft.world.level.levelgen.FlatLevelSource#FlatLevelSource} invokes
		//$$ // {@link net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings#getBiomeFromSettings}, which splits delayed layers.
		//$$ return new FlatLevelSource(registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY), settings);
		//#elseif MC >= 1.17.1
		//$$ // {@link net.minecraft.world.level.levelgen.FlatLevelSource#FlatLevelSource} invokes
		//$$ // {@link net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings#getBiomeFromSettings}, which splits delayed layers.
		//$$ return new FlatLevelSource(settings);
		//#else
		//$$ // TODO: Port Flat generator construction against the target MC source.
		//$$ return TODO_PORT_MC_VERSION;
		//#endif
	}
}
