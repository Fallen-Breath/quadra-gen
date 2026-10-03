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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//#if MC < 1.16.5
//$$ import net.minecraft.world.level.LevelType;
//#endif

/**
 * mc >  1.20.1: subproject 26.2 (main project)
 * mc <= 1.20.1: subproject 1.20.1  <--------
 */
@Mixin(PlayerList.class)
public abstract class PlayerListMixin
{
	@ModifyExpressionValue(
			method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;)V",
			at = @At(
					value = "INVOKE",
					//#if MC >= 1.16.5
					target = "Lnet/minecraft/server/level/ServerLevel;isFlat()Z"
					//#else
					//$$ target = "Lnet/minecraft/world/level/storage/LevelData;getGeneratorType()Lnet/minecraft/world/level/LevelType;"
					//#endif
			)
	)
	//#if MC >= 1.16.5
	private boolean advertiseWorldType_modifyFlatFlag(boolean original, @Local(argsOnly = true) ServerPlayer player)
	//#else
	//$$ private LevelType advertiseWorldType_modifyFlatFlag(LevelType original, @Local(argsOnly = true) ServerPlayer player)
	//#endif
	{
		return AdvertisedWorldTypeQuery.shouldAdvertiseFlat(player.serverLevel(), player, original);
	}

	@ModifyExpressionValue(
			//#if MC >= 1.16.5
			method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;",
			//#else
			//$$ method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/dimension/DimensionType;Z)Lnet/minecraft/server/level/ServerPlayer;",
			//#endif
			at = @At(
					value = "INVOKE",
					//#if MC >= 1.16.5
					target = "Lnet/minecraft/server/level/ServerLevel;isFlat()Z"
					//#else
					//$$ target = "Lnet/minecraft/world/level/storage/LevelData;getGeneratorType()Lnet/minecraft/world/level/LevelType;"
					//#endif
			)
	)
	//#if MC >= 1.16.5
	private boolean advertiseWorldType_modifyRespawnFlatFlag(boolean original, @Local(ordinal = 1) ServerPlayer player)
	//#else
	//$$ private LevelType advertiseWorldType_modifyRespawnFlatFlag(LevelType original, @Local(ordinal = 1) ServerPlayer player)
	//#endif
	{
		return AdvertisedWorldTypeQuery.shouldAdvertiseFlat(player.serverLevel(), player, original);
	}
}
