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

import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.ServerLevelContextAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

/**
 * mc >= 1.16.5: main project
 * mc <= 1.15.2: subproject 1.15.2  <--------
 * <p>
 * Mirrors {@link Slime#checkSlimeSpawnRules}: vanilla Flat worlds reject three quarters of slime spawn attempts.
 */
@Mixin(Slime.class)
public abstract class SlimeMixin
{
	@Inject(method = "checkSlimeSpawnRules", at = @At("HEAD"), cancellable = true)
	private static void applyFlatSpawnReduction(
			CallbackInfoReturnable<Boolean> cir,
			@Local(argsOnly = true) LevelAccessor level,
			@Local(argsOnly = true) BlockPos pos,
			@Local(argsOnly = true) Random random)
	{
		if (level instanceof ServerLevelContextAccess)
		{
			LevelContext context = ((ServerLevelContextAccess)level).getLevelContext$quadragen();
			if (context != null && context.getPlanAt(pos.getX(), pos.getZ()).isFlat() && random.nextInt(4) != 1)
			{
				cir.setReturnValue(false);
			}
		}
	}
}
