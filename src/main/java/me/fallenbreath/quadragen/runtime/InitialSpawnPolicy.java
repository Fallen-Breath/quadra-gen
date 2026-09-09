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

import me.fallenbreath.quadragen.compat.ChunkPosCompat;
import me.fallenbreath.quadragen.core.DimensionKind;
import me.fallenbreath.quadragen.core.Quadrant;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.access.ServerLevelContextAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class InitialSpawnPolicy
{
	//#if MC >= 1.18.2
	// Vanilla {@link net.minecraft.server.MinecraftServer#setInitialSpawn} searches a centered 11x11 chunk square.
	private static final int AXIS_CLEARANCE_CHUNKS = 5;
	//#elseif MC >= 1.16.5
	//$$ // Vanilla {@link net.minecraft.server.MinecraftServer#setInitialSpawn} searches up to 16 chunks from its anchor.
	//$$ private static final int AXIS_CLEARANCE_CHUNKS = 16;
	//#else
	//$$ // Vanilla {@link net.minecraft.server.level.ServerLevel#setInitialSpawn} searches up to 16 chunks from its anchor.
	//$$ private static final int AXIS_CLEARANCE_CHUNKS = 16;
	//#endif

	private InitialSpawnPolicy()
	{
	}

	public static ChunkPos selectAnchor(ServerLevel level, ChunkPos vanillaAnchor)
	{
		if (DimensionKind.from(level) != DimensionKind.OVERWORLD)
		{
			return vanillaAnchor;
		}
		LevelContext context = ((ServerLevelContextAccess)level).getLevelContext$quadragen();
		return context == null ? vanillaAnchor : InitialSpawnPolicy.selectAnchor(context, vanillaAnchor);
	}

	public static boolean allowsBonusChest(ServerLevel level, BlockPos spawnPos)
	{
		if (DimensionKind.from(level) != DimensionKind.OVERWORLD)
		{
			return true;
		}
		LevelContext context = ((ServerLevelContextAccess)level).getLevelContext$quadragen();
		return context == null || InitialSpawnPolicy.hasSafeGround(level, spawnPos);
	}

	private static ChunkPos selectAnchor(LevelContext context, ChunkPos vanillaAnchor)
	{
		ChunkPos noise = nearestOrdinaryNoise(context, vanillaAnchor);
		return noise != null ? noise : vanillaAnchor;
	}

	/**
	 * Mirrors the ground acceptance checks in
	 * {@link net.minecraft.server.level.PlayerRespawnLogic#getOverworldRespawnPos} and
	 * {@link net.minecraft.server.level.PlayerSpawnFinder#getLevelRespawnPos} for the final vanilla spawn position.
	 */
	private static boolean hasSafeGround(ServerLevel level, BlockPos spawnPos)
	{
		BlockPos groundPos = spawnPos.below();
		if (level.isOutsideBuildHeight(groundPos))
		{
			return false;
		}
		BlockState groundState = level.getBlockState(groundPos);
		return groundState.getFluidState().isEmpty() && Block.isFaceFull(groundState.getCollisionShape(level, groundPos), Direction.UP);
	}

	private static ChunkPos nearestOrdinaryNoise(LevelContext context, ChunkPos origin)
	{
		int originX = ChunkPosCompat.x(origin);
		int originZ = ChunkPosCompat.z(origin);
		ChunkPos best = null;
		long bestDistance = Long.MAX_VALUE;
		for (Quadrant quadrant : Quadrant.values())
		{
			QuadrantPlan plan = context.getPlan(quadrant);
			if (!plan.isOrdinaryNoise())
			{
				continue;
			}
			int x = project(originX, quadrant.getXSign());
			int z = project(originZ, quadrant.getZSign());
			long dx = (long)x - originX;
			long dz = (long)z - originZ;
			long distance = dx * dx + dz * dz;
			if (distance < bestDistance)
			{
				bestDistance = distance;
				best = ChunkPosCompat.of(x, z);
			}
		}
		return best;
	}

	private static int project(int coordinate, int sign)
	{
		return sign > 0 ? Math.max(coordinate, AXIS_CLEARANCE_CHUNKS) : Math.min(coordinate, -AXIS_CLEARANCE_CHUNKS - 1);
	}
}
