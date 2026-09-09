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

package me.fallenbreath.quadragen.mixins.worldgen;

import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.core.FlatLayerPlacement;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * mc >= 1.19.4: subproject 26.2 (main project)
 * 1.16.5 <= mc <= 1.18.2: subproject 1.18.2
 * mc <= 1.15.2: subproject 1.15.2                    <--------
 * <p>
 * These versions use dimension-owned generators, legacy biome storage, and the coordinate-based carver API.
 */
@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin implements GeneratorContextAccess
{
	@Unique
	private volatile LevelContext levelContext$quadragen;

	@Override
	public LevelContext getLevelContext$quadragen()
	{
		return this.levelContext$quadragen;
	}

	@Override
	public void setLevelContext$quadragen(LevelContext context)
	{
		this.levelContext$quadragen = context;
	}

	@Inject(method = "createStructures", at = @At("HEAD"), cancellable = true)
	private void createStructures(CallbackInfo ci, @Local(argsOnly = true) ChunkAccess centerChunk)
	{
		LevelContext context = this.levelContext$quadragen;
		if (context != null && context.getPlanAt(centerChunk.getPos()).isFlat())
		{
			ci.cancel();
		}
	}

	@Inject(
			method = "applyBiomeDecoration(Lnet/minecraft/server/level/WorldGenRegion;)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private void applyBiomeDecoration(CallbackInfo ci, @Local(argsOnly = true) WorldGenRegion level)
	{
		LevelContext context = this.levelContext$quadragen;
		if (context == null)
		{
			return;
		}
		ChunkAccess chunk = level.getChunk(level.getCenterX(), level.getCenterZ());
		QuadrantPlan plan = context.getPlanAt(chunk.getPos());
		if (!plan.isOrdinaryNoise())
		{
			if (plan.isFlat() && !plan.isClearGeneratedContent())
			{
				FlatLayerPlacement.placeDelayedLayers(level, chunk, plan.getFlat());
			}
			ci.cancel();
		}
	}

}
