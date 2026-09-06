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

package me.fallenbreath.quadragen.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.FlatLevelSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FlatGenerationPlan
{
	private final int baseY;
	private final Holder<Biome> biome;
	private final List<BlockState> layers;
	private final FlatLevelSource generator;
	private final boolean[] delayedLayers;
	private final boolean empty;
	private final boolean safeSurface;

	public FlatGenerationPlan(int baseY, Holder<Biome> biome, List<BlockState> layers, FlatLevelSource generator)
	{
		this.baseY = baseY;
		this.biome = biome;
		this.layers = Collections.unmodifiableList(new ArrayList<BlockState>(layers));
		this.generator = generator;
		this.delayedLayers = new boolean[this.layers.size()];
		boolean hasBlock = false;
		for (int index = 0; index < this.layers.size(); index++)
		{
			BlockState state = this.layers.get(index);
			this.delayedLayers[index] = !Heightmap.Types.MOTION_BLOCKING.isOpaque().test(state);
			if (!state.isAir())
			{
				hasBlock = true;
			}
		}
		this.empty = !hasBlock;
		this.safeSurface = hasSafeSurface(this.layers);
	}

	public Holder<Biome> getBiome()
	{
		return this.biome;
	}

	public int getBaseY()
	{
		return this.baseY;
	}

	public List<BlockState> getLayers()
	{
		return this.layers;
	}

	public FlatLevelSource getFlatGenerator()
	{
		return this.generator;
	}

	public boolean isDelayedLayer(int index)
	{
		return this.delayedLayers[index];
	}

	public boolean isEmpty()
	{
		return this.empty;
	}

	public boolean hasSafeSurface()
	{
		return this.safeSurface;
	}

	private static boolean hasSafeSurface(List<BlockState> layers)
	{
		int motionBlocking = -1;
		int worldSurface = -1;
		int oceanFloor = -1;
		for (int index = 0; index < layers.size(); index++)
		{
			BlockState state = layers.get(index);
			if (!state.isAir())
			{
				worldSurface = index;
			}
			if (Heightmap.Types.MOTION_BLOCKING.isOpaque().test(state))
			{
				motionBlocking = index;
			}
			if (Heightmap.Types.OCEAN_FLOOR.isOpaque().test(state))
			{
				oceanFloor = index;
			}
		}
		if (motionBlocking < 0 || worldSurface <= motionBlocking && worldSurface > oceanFloor)
		{
			return false;
		}
		for (int index = Math.min(motionBlocking + 1, layers.size() - 1); index >= 0; index--)
		{
			BlockState state = layers.get(index);
			if (!state.getFluidState().isEmpty())
			{
				return false;
			}
			if (Block.isFaceFull(state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO), Direction.UP))
			{
				return true;
			}
		}
		return false;
	}
}
