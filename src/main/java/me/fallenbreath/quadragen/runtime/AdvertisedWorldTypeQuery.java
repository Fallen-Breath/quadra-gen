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

//#if MC >= 1.20.2
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
//#endif

//#if MC < 1.16
//$$ import net.minecraft.world.level.LevelType;
//#endif

public final class AdvertisedWorldTypeQuery
{
	private AdvertisedWorldTypeQuery()
	{
	}

	private static boolean shouldAdvertiseFlat(LevelContext context, double x, double z)
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
		return context.getPlanAt((int)Math.floor(x), (int)Math.floor(z)).isFlat();
	}

	public static boolean shouldAdvertiseFlat(ServerLevel level, ServerPlayer player, boolean original)
	{
		//#if MC >= 1.15.2
		return shouldAdvertiseFlat(level, player.getX(), player.getZ(), original);
		//#else
		//$$ return shouldAdvertiseFlat(level, player.x, player.z, original);
		//#endif
	}

	public static boolean shouldAdvertiseFlat(ServerLevel level, double x, double z, boolean original)
	{
		LevelContext context = ((ServerLevelContextAccess)level).getLevelContext$quadragen();
		if (context == null)
		{
			return original;
		}
		return shouldAdvertiseFlat(context, x, z);
	}

	//#if MC >= 1.20.2
	public static CommonPlayerSpawnInfo withWorldType(ServerLevel level, double x, double z, CommonPlayerSpawnInfo original)
	{
		boolean flat = shouldAdvertiseFlat(level, x, z, original.isFlat());
		if (flat == original.isFlat())
		{
			return original;
		}
		return new CommonPlayerSpawnInfo(
				original.dimensionType(), original.dimension(), original.seed(), original.gameType(),
				original.previousGameType(), original.isDebug(), flat, original.lastDeathLocation(), original.portalCooldown()
				//#if MC >= 1.21.3
				, original.seaLevel()
				//#endif
		);
	}
	//#endif

	//#if MC < 1.16
	//$$ public static LevelType shouldAdvertiseFlat(ServerLevel level, ServerPlayer player, LevelType original)
	//$$ {
	//#if MC >= 1.15.2
	//$$ 	return shouldAdvertiseFlat(level, player.getX(), player.getZ(), original);
	//#else
	//$$ 	return shouldAdvertiseFlat(level, player.x, player.z, original);
	//#endif
	//$$ }
	//$$
	//$$ public static LevelType shouldAdvertiseFlat(ServerLevel level, double x, double z, LevelType original)
	//$$ {
	//$$ 	LevelContext context = ((ServerLevelContextAccess)level).getLevelContext$quadragen();
	//$$ 	if (context == null)
	//$$ 	{
	//$$ 		return original;
	//$$ 	}
	//$$ 	return shouldAdvertiseFlat(context, x, z) ? LevelType.FLAT : LevelType.NORMAL;
	//$$ }
	//#endif
}
