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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.17.1
import me.fallenbreath.quadragen.compat.ChunkPosCompat;
//#endif

//#if MC >= 1.16.5
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
//#else
//$$ import net.minecraft.world.level.levelgen.OverworldLevelSource;
//#endif

/**
 * Before 1.16.5, only OverworldLevelSource overrides worldgen mob spawning; Nether inherits the no-op base method.
 */
//#if MC >= 1.16.5
@Mixin(NoiseBasedChunkGenerator.class)
//#else
//$$ @Mixin(OverworldLevelSource.class)
//#endif
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
