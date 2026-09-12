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

import me.fallenbreath.fanetlib.api.event.FanetlibClientEvents;
import me.fallenbreath.fanetlib.api.packet.PacketHandlerS2C;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;

public final class ClientNetworkHandler
{
	private ClientNetworkHandler()
	{
	}

	public static void initEvents()
	{
		FanetlibClientEvents.registerGameJoinListener((client, networkHandler) -> requestState(networkHandler));
		FanetlibClientEvents.registerPlayerRespawnListener((client, networkHandler) -> requestState(networkHandler));
		FanetlibClientEvents.registerDisconnectListener(client -> ClientSyncState.reset());
	}

	public static void handleState(CompoundTag data, PacketHandlerS2C.Context context)
	{
		Minecraft client = context.getClient();
		client.execute(() -> ClientSyncState.apply(data));
	}

	private static void requestState(ClientPacketListener networkHandler)
	{
		ClientSyncState.reset();
		networkHandler.send(QuadraGenNetwork.createC2SPacket(new QuadraGenPacket(QuadraGenNetwork.GET_DIMENSION_STATE_REQUEST, new CompoundTag())));
	}
}
