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

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public final class ChunkPosCompat
{
	private ChunkPosCompat()
	{
	}

	public static int x(ChunkPos pos)
	{
		//#if MC >= 26.1
		return pos.x();
		//#else
		//$$ return pos.x;
		//#endif
	}

	public static int z(ChunkPos pos)
	{
		//#if MC >= 26.1
		return pos.z();
		//#else
		//$$ return pos.z;
		//#endif
	}

	public static ChunkPos fromBlockPos(BlockPos pos)
	{
		//#if MC >= 26.1
		return ChunkPos.containing(pos);
		//#else
		//$$ return new ChunkPos(pos);
		//#endif
	}

	public static ChunkPos of(int x, int z)
	{
		return new ChunkPos(x, z);
	}

	public static long pack(ChunkPos pos)
	{
		//#if MC >= 26.1
		return pos.pack();
		//#else
		//$$ return pos.toLong();
		//#endif
	}

	public static long pack(int x, int z)
	{
		//#if MC >= 26.1
		return ChunkPos.pack(x, z);
		//#else
		//$$ return ChunkPos.asLong(x, z);
		//#endif
	}

	public static long pack(BlockPos pos)
	{
		//#if MC >= 26.1
		return ChunkPos.pack(pos);
		//#elseif MC >= 1.17.1
		//$$ return ChunkPos.asLong(pos);
		//#else
		//$$ return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
		//#endif
	}

	public static ChunkPos unpack(long packed)
	{
		//#if MC >= 26.1
		return ChunkPos.unpack(packed);
		//#else
		//$$ return new ChunkPos(packed);
		//#endif
	}
}
