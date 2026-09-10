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

package me.fallenbreath.quadragen.mixins.worldgen.spawnpoint;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.fallenbreath.quadragen.runtime.InitialSpawnPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * mc >= 1.16.5: subproject 26.2 (main project)
 * mc <= 1.15.2: subproject 1.15.2                    <--------
 * <p>
 * Initial-spawn selection is owned by ServerLevel in this interval.
 */
@Mixin(ServerLevel.class)
public abstract class InitialSpawnMixin
{
	@ModifyVariable(method = "setInitialSpawn", at = @At("STORE"), ordinal = 0)
	private ChunkPos selectInitialSpawnAnchor(ChunkPos vanillaAnchor)
	{
		return InitialSpawnPolicy.selectAnchor((ServerLevel)(Object)this, vanillaAnchor);
	}

	@ModifyExpressionValue(
			method = "setInitialSpawn",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelSettings;hasStartingBonusItems()Z")
	)
	private boolean disableUnsafeBonusChest(boolean spawnBonusChest)
	{
		if (!spawnBonusChest)
		{
			return false;
		}
		ServerLevel level = (ServerLevel)(Object)this;
		return InitialSpawnPolicy.allowsBonusChest(
				level,
				new BlockPos(level.getLevelData().getXSpawn(), level.getLevelData().getYSpawn(), level.getLevelData().getZSpawn())
		);
	}
}
