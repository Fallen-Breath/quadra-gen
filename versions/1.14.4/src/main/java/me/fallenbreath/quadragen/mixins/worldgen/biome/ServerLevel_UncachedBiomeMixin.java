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
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.ServerLevelContextAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * mc >= 1.15.2: subproject 26.2 (main project)
 * mc <  1.15.2: subproject 1.14.4
 * <p>
 * 1.14.4 has no uncached quart query; {@link Level#getBiome(BlockPos)} samples BiomeSource directly when its chunk is absent.
 */
@Mixin(Level.class)
public abstract class ServerLevel_UncachedBiomeMixin
{
	@ModifyExpressionValue(
			method = "getBiome",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/BiomeSource;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome;")
	)
	private Biome useQuadrantBiome(Biome original, @Local(argsOnly = true) BlockPos pos)
	{
		if ((Object)this instanceof ServerLevel)
		{
			LevelContext context = ((ServerLevelContextAccess)this).getLevelContext$quadragen();
			if (context != null)
			{
				QuadrantPlan plan = context.getPlanAt(pos.getX(), pos.getZ());
				if (plan.isFlat())
				{
					return plan.getFlat().getBiome();
				}
			}
		}
		return original;
	}
}
