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

package me.fallenbreath.quadragen.worldgen;

import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomState;

import java.util.concurrent.CompletableFuture;

public final class GenerationHooks
{
	private GenerationHooks()
	{
	}

	public static QuadrantPlan plan(LevelContext context, ChunkAccess chunk)
	{
		return context.planForChunk(chunk.getPos());
	}

	public static CompletableFuture<ChunkAccess> createBiomes(LevelContext context, RandomState randomState, ChunkAccess chunk)
	{
		QuadrantPlan plan = plan(context, chunk);
		BiomeWriter.fillFlatBiome(chunk, randomState, plan);
		return CompletableFuture.completedFuture(chunk);
	}

	public static CompletableFuture<ChunkAccess> fill(LevelContext context, ChunkAccess chunk)
	{
		QuadrantPlan plan = plan(context, chunk);
		if (plan.isFlat() && !plan.isClearGeneratedContent())
		{
			FlatLayerPlacer.placeDirectLayers(chunk, plan.getFlat());
		}
		return CompletableFuture.completedFuture(chunk);
	}

	public static void decorate(LevelContext context, WorldGenLevel level, ChunkAccess chunk)
	{
		QuadrantPlan plan = plan(context, chunk);
		if (plan.isFlat() && !plan.isClearGeneratedContent())
		{
			FlatLayerPlacer.placeDelayedLayers(level, chunk, plan.getFlat());
		}
	}
}
