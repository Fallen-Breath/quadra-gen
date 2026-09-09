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

import me.fallenbreath.quadragen.runtime.access.ServerLevelContextAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class CustomSpawnerPolicy
{
	private CustomSpawnerPolicy()
	{
	}

	public static boolean usesNoiseGeneratorAt(ServerLevel level, BlockPos pos)
	{
		LevelContext context = ((ServerLevelContextAccess)level).getLevelContext$quadragen();
		return context == null || context.getPlanAt(pos.getX(), pos.getZ()).isNoise();
	}
}
