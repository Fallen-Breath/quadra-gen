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

package me.fallenbreath.quadragen.gameplay;

import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;

public final class PhantomSpawnPolicy
{
	private PhantomSpawnPolicy()
	{
	}

	public static boolean allowsPhantom(LevelContext context, ServerPlayer player)
	{
		QuadrantPlan plan = context.planForBlock(player.getBlockX(), player.getBlockZ());
		return !plan.isClearGeneratedContent() && !(plan.isFlat() && plan.getFlat().isEmpty());
	}

	public static int tick(LevelContext context, ServerLevel level, boolean spawnEnemies, int nextTick)
	{
		if (spawnEnemies && level.getGameRules().get(GameRules.SPAWN_PHANTOMS))
		{
			RandomSource random = level.getRandom();
			nextTick--;
			if (nextTick <= 0)
			{
				nextTick += (60 + random.nextInt(60)) * 20;
				if (level.getSkyDarken() >= 5 || !level.dimensionType().hasSkyLight())
				{
					for (ServerPlayer player : level.players())
					{
						if (!allowsPhantom(context, player) || player.isSpectator())
						{
							continue;
						}
						BlockPos playerPos = player.blockPosition();
						if (!level.dimensionType().hasSkyLight() || playerPos.getY() >= level.getSeaLevel() && level.canSeeSky(playerPos))
						{
							DifficultyInstance difficulty = level.getCurrentDifficultyAt(playerPos);
							if (difficulty.isHarderThan(random.nextFloat() * 3.0F))
							{
								ServerStatsCounter stats = player.getStats();
								int value = Mth.clamp(stats.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
								if (random.nextInt(value) >= 72000)
								{
									BlockPos spawnPos = playerPos.above(20 + random.nextInt(15)).east(-10 + random.nextInt(21)).south(-10 + random.nextInt(21));
									BlockState blockState = level.getBlockState(spawnPos);
									FluidState fluidState = level.getFluidState(spawnPos);
									if (NaturalSpawner.isValidEmptySpawnBlock(level, spawnPos, blockState, fluidState, EntityTypes.PHANTOM))
									{
										SpawnGroupData groupData = null;
										int groupSize = 1 + random.nextInt(difficulty.getDifficulty().getId() + 1);
										for (int i = 0; i < groupSize; i++)
										{
											Phantom phantom = EntityTypes.PHANTOM.create(level, EntitySpawnReason.NATURAL);
											if (phantom != null)
											{
												phantom.snapTo(spawnPos, 0.0F, 0.0F);
												groupData = phantom.finalizeSpawn(level, difficulty, EntitySpawnReason.NATURAL, groupData);
												level.addFreshEntityWithPassengers(phantom);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return nextTick;
	}
}
