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

package me.fallenbreath.quadragen.compat;

import net.minecraft.nbt.CompoundTag;

public final class NbtCompat
{
	private NbtCompat()
	{
	}

	public static boolean getBoolean(CompoundTag tag, String key, boolean defaultValue)
	{
		//#if MC >= 1.21.5
		return tag.getBooleanOr(key, defaultValue);
		//#else
		//$$ return tag.getBoolean(key);
		//#endif
	}

	public static int getInt(CompoundTag tag, String key, int defaultValue)
	{
		//#if MC >= 1.21.5
		return tag.getIntOr(key, defaultValue);
		//#else
		//$$ return tag.contains(key) ? tag.getInt(key) : defaultValue;
		//#endif
	}

	public static int[] getIntArray(CompoundTag tag, String key)
	{
		//#if MC >= 1.21.5
		return tag.getIntArray(key).orElse(new int[0]);
		//#else
		//$$ return tag.contains(key) ? tag.getIntArray(key) : new int[0];
		//#endif
	}
}
