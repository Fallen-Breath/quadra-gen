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

package me.fallenbreath.quadragen.mixins.spawn;

import me.fallenbreath.quadragen.gameplay.PhantomSpawnPolicy;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.ServerLevelContextAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerMixin
{
	@Shadow
	private int nextTick;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void quadragen$filterPlayers(ServerLevel level, boolean spawnEnemies, CallbackInfo ci)
	{
		LevelContext context = ((ServerLevelContextAccess)level).quadragen$getLevelContext();
		if (context != null)
		{
			this.nextTick = PhantomSpawnPolicy.tick(context, level, spawnEnemies, this.nextTick);
			ci.cancel();
		}
	}
}
