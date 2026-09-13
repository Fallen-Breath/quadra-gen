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

import me.fallenbreath.quadragen.core.Quadrant;
import me.fallenbreath.quadragen.network.ClientSyncState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

/**
 * Position-aware queries for the client-side vanilla assumptions represented by
 * {@link net.minecraft.client.multiplayer.ClientLevel.ClientLevelData}.
 */
public final class ClientSyncQuery
{
	private ClientSyncQuery()
	{
	}

	/**
	 * Mirrors {@link net.minecraft.client.multiplayer.ClientLevel.ClientLevelData#getHorizonHeight(net.minecraft.world.level.LevelHeightAccessor)}
	 * while selecting the generator type from the queried position.
	 */
	public static double getHorizonHeight(ClientLevel level, double original, double x, double z)
	{
		ClientSyncState.State state = ClientSyncState.getState();
		if (!state.isActive())
		{
			return original;
		}
		return isFlat(state, x, z) ? level.getMinY() : 63.0D;
	}

	/**
	 * Mirrors {@link net.minecraft.client.multiplayer.ClientLevel.ClientLevelData#voidDarknessOnsetRange()} for one position.
	 */
	public static float getVoidDarknessOnsetRange(float original, double x, double z)
	{
		ClientSyncState.State state = ClientSyncState.getState();
		if (!state.isActive())
		{
			return original;
		}

		// Vanilla {@link net.minecraft.client.multiplayer.ClientLevel.ClientLevelData#voidDarknessOnsetRange}
		// returns 1.0F for Flat and 32.0F for Noise.
		// The active Quadra dimension is Noise-based, so preserve its vanilla result instead of copying 32.0F here.
		return isFlat(state, x, z) ? 1.0F : original;
	}

	//#if MC < 1.16.5
	/** Selects the vanilla Flat/Noise clear-color scale for one position; vanilla source: {@link net.minecraft.world.level.dimension.Dimension#getClearColorScale()}. */
	//#elseif MC < 1.21.8
	/** Selects the vanilla Flat/Noise clear-color scale for one position; vanilla source: {@link net.minecraft.client.multiplayer.ClientLevel.ClientLevelData#getClearColorScale()}. */
	//#else
	/** Selects the vanilla Flat/Noise clear-color scale for one position; corresponding vanilla source: {@link net.minecraft.client.multiplayer.ClientLevel.ClientLevelData#voidDarknessOnsetRange()}. */
	//#endif
	public static float getClearColorScale(float original, double x, double z)
	{
		ClientSyncState.State state = ClientSyncState.getState();
		if (!state.isActive())
		{
			return original;
		}
		return isFlat(state, x, z) ? 1.0F : original;
	}

	/**
	 * Selects the Flat sea level for a client precipitation query. Noise and inactive states retain the vanilla value.
	 */
	public static int getSeaLevelAt(BlockPos pos, int original)
	{
		ClientSyncState.State state = ClientSyncState.getState();
		if (!state.isActive())
		{
			return original;
		}

		Quadrant quadrant = Quadrant.fromCoordinates(pos.getX(), pos.getZ());
		if (!isFlat(state, quadrant))
		{
			return original;
		}
		return state.getFlatSeaLevel(quadrant);
	}

	private static boolean isFlat(ClientSyncState.State state, double x, double z)
	{
		return isFlat(state, Quadrant.fromCoordinates(Mth.floor(x), Mth.floor(z)));
	}

	private static boolean isFlat(ClientSyncState.State state, Quadrant quadrant)
	{
		return (state.getFlatQuadrants() & (1 << quadrant.ordinal())) != 0;
	}
}
