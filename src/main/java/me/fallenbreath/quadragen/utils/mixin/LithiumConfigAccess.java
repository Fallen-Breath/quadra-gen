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

package me.fallenbreath.quadragen.utils.mixin;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public final class LithiumConfigAccess
{
	private static final String LITHIUM_PACKAGE = "net.caffeinemc.mods.lithium.";

	private LithiumConfigAccess()
	{
	}

	/** Checks a relative Lithium mixin class name without loading the mixin class. */
	public static boolean isLithiumMixinEnabled(String mixinClassName)
	{
		if (!FabricLoader.getInstance().isModLoaded("lithium"))
		{
			return false;
		}
		ClassLoader classLoader = LithiumConfigAccess.class.getClassLoader();
		String resource = (LITHIUM_PACKAGE + "mixin." + mixinClassName).replace('.', '/') + ".class";
		if (classLoader.getResource(resource) == null)
		{
			return false;
		}
		try
		{
			Object config = ConfigHolder.CONFIG;
			// Lithium's lookup includes parent rules; pass a class name, not just the rule package.
			Object option = config.getClass().getMethod("getEffectiveOptionForMixin", String.class).invoke(config, mixinClassName);
			return option != null && (Boolean)option.getClass().getMethod("isEnabled").invoke(option);
		}
		catch (ReflectiveOperationException | LinkageError e)
		{
			throw new IllegalStateException("Failed to check Lithium mixin " + mixinClassName, e);
		}
	}

	private static Object loadLithiumConfig()
	{
		try
		{
			Class<?> configClass = Class.forName(LITHIUM_PACKAGE + "common.config.LithiumConfig", true, LithiumConfigAccess.class.getClassLoader());
			// Use Lithium's loader so defaults, mod overrides and rule dependencies are respected.
			return configClass.getMethod("load", File.class).invoke(null, new File("./config/lithium.properties"));
		}
		catch (ReflectiveOperationException | LinkageError e)
		{
			throw new IllegalStateException("Failed to load Lithium config for Quadra Gen mixin compatibility", e);
		}
	}

	private static final class ConfigHolder
	{
		private static final Object CONFIG = loadLithiumConfig();
	}
}
