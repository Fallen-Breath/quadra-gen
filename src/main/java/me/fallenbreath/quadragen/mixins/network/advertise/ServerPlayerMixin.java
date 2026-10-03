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
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.runtime.AdvertisedWorldTypeQuery;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//#if MC >= 1.21.3
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
//#elseif MC >= 1.21.1
//$$ import net.minecraft.world.level.portal.DimensionTransition;
//#endif

/**
 *           mc >  1.20.1: subproject 26.2 (main project)  <--------
 * 1.16.5 <= mc <= 1.20.1: subproject 1.20.1
 *           mc <= 1.15.2: subproject 1.15.2
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin
{
	@ModifyReturnValue(method = "createCommonSpawnInfo", at = @At("RETURN"))
	private CommonPlayerSpawnInfo advertiseWorldType_modifyCommonSpawnInfo(CommonPlayerSpawnInfo original, ServerLevel level)
	{
		ServerPlayer self = (ServerPlayer)(Object)this;
		//#if MC < 1.21.1
		//$$ // Accepted compromise: portal AUTO uses departure X/Z to preserve vanilla packet timing.
		//#endif
		return AdvertisedWorldTypeQuery.withWorldType(level, self.getX(), self.getZ(), original);
	}

	//#if MC >= 1.21.1
	/** Uses the destination passed to vanilla teleport, including relative coordinates in newer versions. */
	@ModifyExpressionValue(
			//#if MC >= 1.21.3
			method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
			//#else
			//$$ method = "changeDimension(Lnet/minecraft/world/level/portal/DimensionTransition;)Lnet/minecraft/world/entity/Entity;",
			//#endif
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;createCommonSpawnInfo(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/network/protocol/game/CommonPlayerSpawnInfo;")
	)
	private CommonPlayerSpawnInfo advertiseWorldType_useTeleportDestination(CommonPlayerSpawnInfo original,
			//#if MC >= 1.21.3
			@Local(argsOnly = true) TeleportTransition transition
			//#else
			//$$ @Local(argsOnly = true) DimensionTransition transition
			//#endif
	)
	{
		//#if MC >= 1.21.3
		// Match {@link net.minecraft.world.entity.Entity#teleportSetPosition(PositionMoveRotation, java.util.Set)}.
		Vec3 destination = PositionMoveRotation.calculateAbsolute(PositionMoveRotation.of((ServerPlayer)(Object)this), PositionMoveRotation.of(transition), transition.relatives()).position();
		return AdvertisedWorldTypeQuery.withWorldType(transition.newLevel(), destination.x, destination.z, original);
		//#else
		//$$ return AdvertisedWorldTypeQuery.withWorldType(transition.newLevel(), transition.pos().x, transition.pos().z, original);
		//#endif
	}
	//#else
	//$$ @ModifyExpressionValue(
	//$$ 		method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V",
	//$$ 		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;createCommonSpawnInfo(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/network/protocol/game/CommonPlayerSpawnInfo;")
	//$$ )
	//$$ private CommonPlayerSpawnInfo advertiseWorldType_useCommandDestination(CommonPlayerSpawnInfo original,
	//$$ 		@Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true, ordinal = 0) double x, @Local(argsOnly = true, ordinal = 2) double z)
	//$$ {
	//$$ 	return AdvertisedWorldTypeQuery.withWorldType(level, x, z, original);
	//$$ }
	//#endif
}
