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

package me.fallenbreath.quadragen.mixins.worldgen.mob;

import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.17.1
import me.fallenbreath.quadragen.compat.ChunkPosCompat;
//#endif

/**
 * mc >= 1.16.5: subproject 26.2 (main project)       <--------
 * mc <= 1.15.2: subproject 1.15.2
 * <p>
 * Worldgen mob spawning is implemented by NoiseBasedChunkGenerator in this interval.
 */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class WorldgenMobGenerationMixin
{
	@Inject(method = "spawnOriginalMobs", at = @At("HEAD"), cancellable = true)
	private void spawnOriginalMobs(WorldGenRegion region, CallbackInfo ci)
	{
		LevelContext context = ((GeneratorContextAccess)this).getLevelContext$quadragen();
		if (context == null)
		{
			return;
		}
		ChunkAccess chunk = region.getChunk(
				//#if MC >= 1.17.1
				ChunkPosCompat.x(region.getCenter()), ChunkPosCompat.z(region.getCenter())
				//#else
				//$$ region.getCenterX(), region.getCenterZ()
				//#endif
		);
		if (
				//#if MC >= 1.18.2
				!chunk.isUpgrading() &&
				//#endif
				!context.getPlanAt(chunk.getPos()).isOrdinaryNoise()
		)
		{
			ci.cancel();
		}
	}
}
