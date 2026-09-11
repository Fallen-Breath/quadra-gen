/*
 * This file is part of the Quadra Gen project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Fallen_Breath and contributors
 *
 * Quadra Gen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License v3.0
 * as published by the Free Software Foundation, either version 3 of the License, or
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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import me.fallenbreath.quadragen.runtime.BiomeLocateContext;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.ServerLevelContextAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

/**
 * mc >= 26.3: subproject 26.3                    <--------
 * mc <= 26.2: subproject 26.2
 * <p>
 * 26.3 passes RandomState to BiomeSource locate instead of the Climate sampler used before it.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin
{
	@WrapOperation(
			method = "findClosestBiome3d(Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;III)Lcom/mojang/datafixers/util/Pair;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/biome/BiomeSource;findClosestBiome3d(Lnet/minecraft/core/BlockPos;IIILjava/util/function/Predicate;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/LevelReader;)Lcom/mojang/datafixers/util/Pair;"
			)
	)
	private Pair<BlockPos, Holder<Biome>> scopeLocateBiome(
			BiomeSource source,
			BlockPos origin,
			int maxSearchRadius,
			int sampleResolutionHorizontal,
			int sampleResolutionVertical,
			Predicate<Holder<Biome>> biomeTest,
			RandomState randomState,
			LevelReader level,
			Operation<Pair<BlockPos, Holder<Biome>>> original
	)
	{
		LevelContext context = ((ServerLevelContextAccess)this).getLevelContext$quadragen();
		if (context == null)
		{
			return original.call(source, origin, maxSearchRadius, sampleResolutionHorizontal, sampleResolutionVertical, biomeTest, randomState, level);
		}
		BiomeLocateContext.install(context);
		try
		{
			return original.call(source, origin, maxSearchRadius, sampleResolutionHorizontal, sampleResolutionVertical, biomeTest, randomState, level);
		}
		finally
		{
			BiomeLocateContext.clear();
		}
	}
}
