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
import me.fallenbreath.quadragen.runtime.CustomSpawnerPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * mc >= 1.16.5: main project
 * mc <= 1.15.2: subproject 1.15.2  <--------
 * <p>
 * The original Flat generator omits PatrolSpawner, so the shared Noise generator must reject resolved Flat targets.
 */
@Mixin(PatrolSpawner.class)
public abstract class PatrolSpawnerMixin
{
	@ModifyExpressionValue(
			method = "tick",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;hasChunksAt(IIIIII)Z")
	)
	private boolean rejectFlatTarget(
			boolean chunksLoaded,
			@Local(argsOnly = true) ServerLevel level,
			@Local BlockPos.MutableBlockPos target)
	{
		return chunksLoaded && CustomSpawnerPolicy.usesNoiseGeneratorAt(level, target);
	}
}
