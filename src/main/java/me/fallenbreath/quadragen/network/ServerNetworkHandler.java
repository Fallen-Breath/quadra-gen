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

package me.fallenbreath.quadragen.network;

import me.fallenbreath.fanetlib.api.packet.PacketHandlerC2S;
import me.fallenbreath.quadragen.core.Quadrant;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.ServerLevelContextAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class ServerNetworkHandler
{
	private ServerNetworkHandler()
	{
	}

	public static void handleRequest(CompoundTag ignored, PacketHandlerC2S.Context context)
	{
		context.runSynced(() -> {
			ServerPlayer player = context.getPlayer();
			ServerLevel level = player.level();
			player.connection.send(QuadraGenNetwork.createS2CPacket(responseFor(level)));
		});
	}

	private static QuadraGenPacket responseFor(ServerLevel level)
	{
		CompoundTag data = new CompoundTag();
		LevelContext levelContext = ((ServerLevelContextAccess)level).getLevelContext$quadragen();
		if (levelContext == null)
		{
			data.putBoolean("active", false);
			return new QuadraGenPacket(QuadraGenNetwork.GET_DIMENSION_STATE_RESPONSE, data);
		}

		int flatQuadrants = 0;
		int[] flatSeaLevels = new int[Quadrant.values().length];
		for (Quadrant quadrant : Quadrant.values())
		{
			QuadrantPlan plan = levelContext.getPlan(quadrant);
			if (plan.isFlat())
			{
				flatQuadrants |= 1 << quadrant.ordinal();
				flatSeaLevels[quadrant.ordinal()] = plan.getFlat().getFlatGenerator().getSeaLevel();
			}
		}
		data.putBoolean("active", true);
		data.putInt("flat_quadrants", flatQuadrants);
		data.putIntArray("flat_sea_levels", flatSeaLevels);
		return new QuadraGenPacket(QuadraGenNetwork.GET_DIMENSION_STATE_RESPONSE, data);
	}
}
