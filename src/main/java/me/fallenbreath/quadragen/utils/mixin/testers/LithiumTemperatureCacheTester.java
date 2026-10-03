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

package me.fallenbreath.quadragen.utils.mixin.testers;

import me.fallenbreath.conditionalmixin.api.mixin.ConditionTester;
import me.fallenbreath.quadragen.utils.mixin.LithiumConfigAccess;

public class LithiumTemperatureCacheTester implements ConditionTester
{
	@Override
	public boolean isSatisfied(String mixinClassName)
	{
		// Lithium removes the cache and computes temperature using the supplied sea level directly.
		return LithiumConfigAccess.isLithiumMixinEnabled("world.temperature_cache.BiomeMixin");
	}
}
