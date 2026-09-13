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

package me.fallenbreath.quadragen.mixins.network.advertise;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.runtime.AdvertisedWorldTypeQuery;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.LevelType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 *           mc >  1.20.1: subproject 26.2 (main project)
 * 1.16.5 <= mc <= 1.20.1: subproject 1.20.1
 *           mc <= 1.15.2: subproject 1.15.2  <--------
 */
@Mixin(PlayerList.class)
public abstract class PlayerListMixin
{
	@ModifyExpressionValue(
			method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/storage/LevelData;getGeneratorType()Lnet/minecraft/world/level/LevelType;"
			)
	)
	private LevelType advertiseWorldType_modifyFlatFlag(LevelType original, @Local(argsOnly = true) ServerPlayer player)
	{
		return AdvertisedWorldTypeQuery.shouldAdvertiseFlat(player.getLevel(), player, original);
	}

	@ModifyExpressionValue(
			method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/dimension/DimensionType;Z)Lnet/minecraft/server/level/ServerPlayer;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/storage/LevelData;getGeneratorType()Lnet/minecraft/world/level/LevelType;"
			)
	)
	private LevelType advertiseWorldType_modifyRespawnFlatFlag(LevelType original, @Local(ordinal = 1) ServerPlayer player)
	{
		return AdvertisedWorldTypeQuery.shouldAdvertiseFlat(player.getLevel(), player, original);
	}
}
