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

package me.fallenbreath.quadragen.mixins.lifecycle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.runtime.InitialSpawnPolicy;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin
{
	@ModifyVariable(method = "setInitialSpawn", at = @At("LOAD"), argsOnly = true, ordinal = 0)
	private static boolean disableUnsafeBonusChest(
			boolean spawnBonusChest,
			@Local(argsOnly = true) ServerLevel level,
			@Local(argsOnly = true) ServerLevelData levelData)
	{
		if (!spawnBonusChest)
		{
			return false;
		}
		//#if MC >= 1.21.10
		return InitialSpawnPolicy.allowsBonusChest(level, levelData.getRespawnData().pos());
		//#else
		//$$ return InitialSpawnPolicy.allowsBonusChest(level, levelData.getSpawnPos());
		//#endif
	}

	//#if MC >= 26.1
	@ModifyExpressionValue(
			method = "setInitialSpawn",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;containing(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/ChunkPos;")
	)
	//#else
	//$$ @ModifyVariable(method = "setInitialSpawn", at = @At("STORE"), ordinal = 0)
	//#endif
	private static ChunkPos selectInitialSpawnAnchor(
			ChunkPos vanillaAnchor,
			@Local(argsOnly = true) ServerLevel level)
	{
		return InitialSpawnPolicy.selectAnchor(level, vanillaAnchor);
	}
}
