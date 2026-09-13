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

package me.fallenbreath.quadragen.mixins.worldgen.biome;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import me.fallenbreath.quadragen.core.Quadrant;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.BiomeLocateContext;
import me.fallenbreath.quadragen.runtime.LevelContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * <p>
 * 26.3 creates a caching BiomeResolver from RandomState before sampling.
 */
@Mixin(BiomeSource.class)
public abstract class BiomeSourceMixin
{
	@ModifyExpressionValue(
			method = "findClosestBiome3d(Lnet/minecraft/core/BlockPos;IIILjava/util/function/Predicate;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/LevelReader;)Lcom/mojang/datafixers/util/Pair;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/BiomeSource;possibleBiomes()Ljava/util/Set;")
	)
	private Set<Holder<Biome>> extendLocateCandidates(Set<Holder<Biome>> original)
	{
		BiomeLocateContext locateContext = BiomeLocateContext.current();
		if (locateContext == null)
		{
			return original;
		}
		LevelContext context = locateContext.getLevelContext();
		Set<Holder<Biome>> candidates = new LinkedHashSet<Holder<Biome>>(original);
		for (Quadrant quadrant : Quadrant.values())
		{
			QuadrantPlan plan = context.getPlan(quadrant);
			if (plan.isFlat())
			{
				candidates.add(plan.getFlat().getBiome());
			}
		}
		return candidates;
	}

	@WrapOperation(
			method = "findClosestBiome3d(Lnet/minecraft/core/BlockPos;IIILjava/util/function/Predicate;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/LevelReader;)Lcom/mojang/datafixers/util/Pair;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/BiomeResolver;getNoiseBiome(III)Lnet/minecraft/core/Holder;")
	)
	private Holder<Biome> useQuadrantBiome(
			BiomeResolver resolver,
			int quartX,
			int quartY,
			int quartZ,
			Operation<Holder<Biome>> original)
	{
		Holder<Biome> biome = original.call(resolver, quartX, quartY, quartZ);
		BiomeLocateContext locateContext = BiomeLocateContext.current();
		if (locateContext == null)
		{
			return biome;
		}
		LevelContext context = locateContext.getLevelContext();
		QuadrantPlan plan = context.getPlanAt(quartX << 2, quartZ << 2);
		return plan.isFlat() ? plan.getFlat().getBiome() : biome;
	}
}
