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

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public final class RegistryCompat
{
	private RegistryCompat()
	{
	}

	public static Optional<Block> findBlock(String value)
	{
		Identifier identifier = IdentifierCompat.tryParse(value);
		return identifier == null ? Optional.empty() : BuiltInRegistries.BLOCK.getOptional(identifier);
	}

	public static Optional<Holder<Biome>> findBiome(RegistryAccess access, String value)
	{
		Identifier identifier = IdentifierCompat.tryParse(value);
		if (identifier == null)
		{
			return Optional.empty();
		}
		Registry<Biome> registry =
				//#if MC >= 1.21.3
				access.lookupOrThrow(Registries.BIOME);
				//#elseif MC >= 1.20.4
				//$$ access.registryOrThrow(Registries.BIOME);
				//#else
				//$$ // TODO: Port this lookup against the target MC source.
				//$$ TODO_PORT_MC_VERSION;
				//#endif
		//#if MC >= 1.21.3
		return registry.get(identifier).map(holder -> holder);
		//#elseif MC >= 1.20.6
		//$$ return registry.getHolder(identifier).map(holder -> holder);
		//#elseif MC >= 1.20.4
		//$$ return registry.getHolder(ResourceKey.create(registry.key(), identifier)).map(holder -> holder);
		//#else
		//$$ // TODO: Port this lookup against the target MC source.
		//$$ return TODO_PORT_MC_VERSION;
		//#endif
	}
}
