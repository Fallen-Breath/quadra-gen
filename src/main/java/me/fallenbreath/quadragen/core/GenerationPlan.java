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

package me.fallenbreath.quadragen.core;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class GenerationPlan
{
	private final Map<Quadrant, QuadrantPlan> quadrants;

	public GenerationPlan(Map<Quadrant, QuadrantPlan> quadrants)
	{
		this.quadrants = Collections.unmodifiableMap(new EnumMap<Quadrant, QuadrantPlan>(quadrants));
	}

	public QuadrantPlan get(Quadrant quadrant)
	{
		QuadrantPlan plan = this.quadrants.get(quadrant);
		if (plan == null)
		{
			throw new IllegalArgumentException("Missing quadrant plan " + quadrant);
		}
		return plan;
	}

	public Map<Quadrant, QuadrantPlan> getQuadrants()
	{
		return this.quadrants;
	}
}
