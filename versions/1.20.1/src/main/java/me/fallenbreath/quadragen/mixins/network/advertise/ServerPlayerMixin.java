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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 *           mc >  1.20.1: subproject 26.2 (main project)
 * 1.16.5 <= mc <= 1.20.1: subproject 1.20.1  <--------
 *           mc <  1.16.5: subproject 1.15.2
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin
{
	@ModifyExpressionValue(
			method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;isFlat()Z"
			)
	)
	private boolean advertiseWorldType_modifyLevelType1(boolean original, @Local(argsOnly = true) ServerLevel level)
	{
		return AdvertisedWorldTypeQuery.shouldAdvertiseFlat(level, (ServerPlayer)(Object)this, original);
	}

	@ModifyExpressionValue(
			method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;isFlat()Z"
			)
	)
	private boolean advertiseWorldType_modifyLevelType2(boolean original, @Local(argsOnly = true) ServerLevel level)
	{
		return AdvertisedWorldTypeQuery.shouldAdvertiseFlat(level, (ServerPlayer)(Object)this, original);
	}
}
