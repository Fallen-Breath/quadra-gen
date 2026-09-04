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

import me.fallenbreath.quadragen.core.GenerationPlan;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.core.QuadrantRouter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

public final class LevelContext
{
	private final GenerationPlan generationPlan;
	private final QuadrantRouter router;
	private final NoiseBasedChunkGenerator noiseGenerator;

	public LevelContext(GenerationPlan generationPlan, QuadrantRouter router, NoiseBasedChunkGenerator noiseGenerator)
	{
		this.generationPlan = generationPlan;
		this.router = router;
		this.noiseGenerator = noiseGenerator;
	}

	public GenerationPlan getGenerationPlan()
	{
		return this.generationPlan;
	}

	public QuadrantRouter getRouter()
	{
		return this.router;
	}

	public NoiseBasedChunkGenerator getNoiseGenerator()
	{
		return this.noiseGenerator;
	}

	public QuadrantPlan planForBlock(int blockX, int blockZ)
	{
		return this.generationPlan.get(this.router.forBlock(blockX, blockZ));
	}

	public QuadrantPlan planForChunk(int chunkX, int chunkZ)
	{
		return this.generationPlan.get(this.router.forChunk(chunkX, chunkZ));
	}

	public QuadrantPlan planForChunk(ChunkPos pos)
	{
		return this.planForChunk(pos.x(), pos.z());
	}
}
