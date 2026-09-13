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

package me.fallenbreath.quadragen.mixins.client.weather;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.runtime.ClientSyncQuery;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * <p>
 * 1.21.3 performs the client weather precipitation lookup in
 * {@link WeatherEffectRenderer#getPrecipitationAt(net.minecraft.world.level.Level, BlockPos)}.
 */
@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherEffectRendererMixin
{
	@ModifyExpressionValue(
			method = "getPrecipitationAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getSeaLevel()I")
	)
	private int useQuadrantSeaLevel(int original, @Local(argsOnly = true) BlockPos pos)
	{
		return ClientSyncQuery.getSeaLevelAt(pos, original);
	}
}
