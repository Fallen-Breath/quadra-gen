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

package me.fallenbreath.quadragen.mixins.spawning;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.ServerLevelContextAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * <p>
 * Makes the generator-type check in {@link Slime#checkSlimeSpawnRules} reflect the candidate quadrant.
 */
@Mixin(Slime.class)
public abstract class SlimeMixin
{
	@ModifyExpressionValue(
			method = "checkSlimeSpawnRules",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelData;getGeneratorType()Lnet/minecraft/world/level/LevelType;")
	)
	private static LevelType useGeneratorTypeAtCandidate(
			LevelType original,
			@Local(argsOnly = true) LevelAccessor level,
			@Local(argsOnly = true) BlockPos pos)
	{
		if (level instanceof ServerLevelContextAccess)
		{
			LevelContext context = ((ServerLevelContextAccess)level).getLevelContext$quadragen();
			if (context != null && context.getPlanAt(pos.getX(), pos.getZ()).isFlat())
			{
				return LevelType.FLAT;
			}
		}
		return original;
	}
}
