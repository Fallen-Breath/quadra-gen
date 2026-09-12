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

import me.fallenbreath.fanetlib.api.packet.FanetlibPackets;
import me.fallenbreath.fanetlib.api.packet.PacketCodec;
import me.fallenbreath.fanetlib.api.packet.PacketHandlerC2S;
import me.fallenbreath.fanetlib.api.packet.PacketHandlerS2C;
import me.fallenbreath.fanetlib.api.packet.PacketId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class QuadraGenNetwork
{
	public static final int GET_DIMENSION_STATE_REQUEST = 1;
	public static final int GET_DIMENSION_STATE_RESPONSE = 2;

	private static final PacketId<QuadraGenPacket> CHANNEL_ID = PacketId.of("quadragen", "network");
	private static final Map<Integer, BiConsumer<CompoundTag, PacketHandlerC2S.Context>> C2S_HANDLERS = new HashMap<>();
	private static final Map<Integer, BiConsumer<CompoundTag, PacketHandlerS2C.Context>> S2C_HANDLERS = new HashMap<>();

	private QuadraGenNetwork()
	{
	}

	public static void initPackets()
	{
		C2S_HANDLERS.put(GET_DIMENSION_STATE_REQUEST, QuadraGenNetwork::handleStateRequest);
		S2C_HANDLERS.put(GET_DIMENSION_STATE_RESPONSE, QuadraGenNetwork::handleStateResponse);
		FanetlibPackets.registerDual(
				CHANNEL_ID,
				PacketCodec.of(QuadraGenPacket::writeTo, QuadraGenPacket::new),
				QuadraGenNetwork::handleC2S,
				QuadraGenNetwork::handleS2C
		);
	}

	private static void handleC2S(QuadraGenPacket packet, PacketHandlerC2S.Context context)
	{
		BiConsumer<CompoundTag, PacketHandlerC2S.Context> handler = C2S_HANDLERS.get(packet.getPacketId());
		if (handler != null)
		{
			handler.accept(packet.getData(), context);
		}
	}

	private static void handleStateRequest(CompoundTag payload, PacketHandlerC2S.Context context)
	{
		ServerNetworkHandler.handleRequest(payload, context);
	}

	private static void handleS2C(QuadraGenPacket packet, PacketHandlerS2C.Context context)
	{
		BiConsumer<CompoundTag, PacketHandlerS2C.Context> handler = S2C_HANDLERS.get(packet.getPacketId());
		if (handler != null)
		{
			handler.accept(packet.getData(), context);
		}
	}

	private static void handleStateResponse(CompoundTag payload, PacketHandlerS2C.Context context)
	{
		ClientNetworkHandler.handleState(payload, context);
	}

	public static ClientboundCustomPayloadPacket createS2CPacket(QuadraGenPacket packet)
	{
		return FanetlibPackets.createS2C(CHANNEL_ID, packet);
	}

	public static ServerboundCustomPayloadPacket createC2SPacket(QuadraGenPacket packet)
	{
		return FanetlibPackets.createC2S(CHANNEL_ID, packet);
	}
}
