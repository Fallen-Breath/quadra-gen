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

import me.fallenbreath.quadragen.config.ConfigValidationException;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class RegistryCompat
{
	private RegistryCompat()
	{
	}

	public static BlockState resolveBlock(String value, String path)
	{
		Identifier identifier = IdentifierCompat.parse(value, path);
		Block block = BuiltInRegistries.BLOCK.getOptional(identifier).orElseThrow(
				() -> new ConfigValidationException(path, "unknown block '" + value + "'")
		);
		return block.defaultBlockState();
	}

	public static Holder<Biome> resolveBiome(RegistryAccess access, String value, String path)
	{
		Identifier identifier = IdentifierCompat.parse(value, path);
		Registry<Biome> registry = access.lookupOrThrow(Registries.BIOME);
		return registry.get(identifier).map(holder -> (Holder<Biome>)holder).orElseThrow(
				() -> new ConfigValidationException(path, "unknown biome '" + value + "'")
		);
	}
}
