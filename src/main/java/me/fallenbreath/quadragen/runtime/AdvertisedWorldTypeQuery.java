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

import me.fallenbreath.quadragen.config.AdvertisedWorldType;
import me.fallenbreath.quadragen.runtime.access.ServerLevelContextAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

//#if MC < 1.16
//$$ import net.minecraft.world.level.LevelType;
//#endif

public final class AdvertisedWorldTypeQuery
{
	private AdvertisedWorldTypeQuery()
	{
	}

	private static boolean shouldAdvertiseFlat(ServerPlayer player, LevelContext context)
	{
		AdvertisedWorldType type = context.getAdvertisedWorldType();
		if (type == AdvertisedWorldType.FLAT)
		{
			return true;
		}
		if (type == AdvertisedWorldType.NOISE)
		{
			return false;
		}
		return context.getPlanAt(
				//#if MC >= 1.15.2
				(int)Math.floor(player.getX()),
				(int)Math.floor(player.getZ())
				//#else
				//$$ (int)Math.floor(player.x),
				//$$ (int)Math.floor(player.z)
				//#endif
		).isFlat();
	}

	public static boolean shouldAdvertiseFlat(ServerLevel level, ServerPlayer player, boolean original)
	{
		LevelContext context = ((ServerLevelContextAccess)level).getLevelContext$quadragen();
		if (context == null)
		{
			return original;
		}
		return shouldAdvertiseFlat(player, context);
	}

	//#if MC < 1.16
	//$$ public static LevelType shouldAdvertiseFlat(ServerLevel level, ServerPlayer player, LevelType original)
	//$$ {
	//$$ 	LevelContext context = ((ServerLevelContextAccess)level).getLevelContext$quadragen();
	//$$ 	if (context == null)
	//$$ 	{
	//$$ 		return original;
	//$$ 	}
	//$$ 	return shouldAdvertiseFlat(player, context) ? LevelType.FLAT : LevelType.NORMAL;
	//$$ }
	//#endif
}
