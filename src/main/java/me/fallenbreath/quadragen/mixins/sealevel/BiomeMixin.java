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

package me.fallenbreath.quadragen.mixins.sealevel;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import me.fallenbreath.quadragen.runtime.SeaLevelQuery;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * mc >  1.21.1: subproject 26.2 (main project)
 * mc <= 1.21.1: subproject 1.21.1
 */
@Mixin(Biome.class)
public abstract class BiomeMixin
{
	@Unique
	private final ThreadLocal<Integer> cachedSeaLevel$quadragen = new ThreadLocal<>();

	@ModifyExpressionValue(
			method = "shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Z",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getSeaLevel()I")
	)
	private int useSeaLevelAtFreezeCheck(int original, @Local(argsOnly = true) LevelReader level, @Local(argsOnly = true) BlockPos pos)
	{
		return SeaLevelQuery.getSeaLevelAt(level, pos, original);
	}

	@ModifyExpressionValue(
			method = "shouldSnow",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getSeaLevel()I")
	)
	private int useSeaLevelAtSnowCheck(int original, @Local(argsOnly = true) LevelReader level, @Local(argsOnly = true) BlockPos pos)
	{
		return SeaLevelQuery.getSeaLevelAt(level, pos, original);
	}

	@ModifyExpressionValue(
			method = "getTemperature(Lnet/minecraft/core/BlockPos;I)F",
			at = @At(value = "INVOKE", target = "Ljava/lang/ThreadLocal;get()Ljava/lang/Object;", remap = false)
	)
	private Object validateTemperatureCache(Object original, @Local(argsOnly = true) int seaLevel)
	{
		Integer previous = this.cachedSeaLevel$quadragen.get();
		if (previous == null || previous.intValue() != seaLevel)
		{
			// {@link net.minecraft.world.level.biome.Biome#getTemperature} keys its per-thread cache by position alone.
			((Long2FloatLinkedOpenHashMap)original).clear();
			this.cachedSeaLevel$quadragen.set(seaLevel);
		}
		return original;
	}
}
