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

package me.fallenbreath.quadragen.config;

import me.fallenbreath.quadragen.compat.IdentifierCompat;
import me.fallenbreath.quadragen.compat.RegistryCompat;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

//#if MC >= 1.18.2
import net.minecraft.core.Holder;
//#endif

public final class ConfigValueResolver
{
	private ConfigValueResolver()
	{
	}

	public static BlockState resolveBlock(String value, String path)
	{
		ConfigValueResolver.validateIdentifier(value, path);
		Block block = RegistryCompat.findBlock(value).orElse(null);
		if (block == null)
		{
			throw new ConfigValidationException(path, "unknown block '" + value + "'");
		}
		return block.defaultBlockState();
	}

	public static
			//#if MC >= 1.18.2
			Holder<Biome>
			//#elseif MC >= 1.17.1
			//$$ Biome
			//#else
			//$$ TODO_PORT_MC_VERSION
			//#endif
			resolveBiome(RegistryAccess access, String value, String path)
	{
		ConfigValueResolver.validateIdentifier(value, path);
		//#if MC >= 1.18.2
		Holder<Biome> biome = RegistryCompat.findBiome(access, value).orElse(null);
		//#elseif MC >= 1.17.1
		//$$ Biome biome = RegistryCompat.findBiome(access, value).orElse(null);
		//#else
		//$$ TODO_PORT_MC_VERSION biome = TODO_PORT_MC_VERSION;
		//#endif
		if (biome == null)
		{
			throw new ConfigValidationException(path, "unknown biome '" + value + "'");
		}
		return biome;
	}

	private static void validateIdentifier(String value, String path)
	{
		if (!IdentifierCompat.isValid(value))
		{
			throw new ConfigValidationException(path, "invalid identifier '" + value + "'");
		}
	}
}
