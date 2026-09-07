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
import me.fallenbreath.quadragen.runtime.SeaLevelQuery;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Level.class)
public abstract class LevelMixin
{
	@ModifyExpressionValue(
			method = "getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getSeaLevel()I")
	)
	private int useSeaLevelOutsideWorldBorder(int original, @Local(argsOnly = true, ordinal = 0) int blockX, @Local(argsOnly = true, ordinal = 1) int blockZ)
	{
		return SeaLevelQuery.getSeaLevelAt((Level)(Object)this, blockX, blockZ, original);
	}

	@ModifyExpressionValue(
			//#if MC >= 1.21.8
			method = "precipitationAt",
			//#elseif MC >= 1.21.5
			//$$ method = "isRainingAt",
			//#else
			//$$ method = "precipitationAt",
			//#endif
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getSeaLevel()I")
	)
	private int useSeaLevelAtPrecipitation(int original, @Local(argsOnly = true) BlockPos pos)
	{
		return SeaLevelQuery.getSeaLevelAt((Level)(Object)this, pos, original);
	}
}
