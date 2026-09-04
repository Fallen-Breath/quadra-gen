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

public final class QuadrantPlan
{
	private final GeneratorKind generatorKind;
	private final boolean clearGeneratedContent;
	private final FlatGenerationPlan flat;

	public QuadrantPlan(GeneratorKind generatorKind, boolean clearGeneratedContent, FlatGenerationPlan flat)
	{
		this.generatorKind = generatorKind;
		this.clearGeneratedContent = clearGeneratedContent;
		this.flat = flat;
	}

	public GeneratorKind getGeneratorKind()
	{
		return this.generatorKind;
	}

	public boolean isNoise()
	{
		return this.generatorKind == GeneratorKind.NOISE;
	}

	public boolean isFlat()
	{
		return this.generatorKind == GeneratorKind.FLAT;
	}

	public boolean isClearGeneratedContent()
	{
		return this.clearGeneratedContent;
	}

	public FlatGenerationPlan getFlat()
	{
		if (this.flat == null)
		{
			throw new IllegalStateException("Not a flat quadrant");
		}
		return this.flat;
	}

	public boolean isOrdinaryNoise()
	{
		return this.isNoise() && !this.clearGeneratedContent;
	}

	public boolean shouldCreateStructureStarts()
	{
		return this.isNoise();
	}

	public boolean shouldRunSurface()
	{
		return this.isOrdinaryNoise();
	}

	public boolean shouldRunCarvers()
	{
		return this.isOrdinaryNoise();
	}

	public boolean shouldRunWorldgenMobs()
	{
		return this.isOrdinaryNoise();
	}

	public boolean isPhysicallyEmpty()
	{
		return this.clearGeneratedContent || this.isFlat() && this.getFlat().isEmpty();
	}

	public boolean hasSafeFlatSurface()
	{
		return this.isFlat() && !this.clearGeneratedContent && this.getFlat().hasSafeSurface();
	}
}
