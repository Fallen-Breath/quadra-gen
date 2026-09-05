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
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import me.fallenbreath.quadragen.worldgen.FlatLayerPlacer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
		if (context != null && !centerChunk.isUpgrading())
		{
			QuadrantPlan plan = context.getPlanAt(centerChunk.getPos());
			if (plan.isFlat())
			{
				ci.cancel();
			}
		}
	}

	@Inject(method = "applyBiomeDecoration", at = @At("HEAD"), cancellable = true)
	private void applyBiomeDecoration(
			CallbackInfo ci,
			@Local(argsOnly = true) WorldGenLevel level,
			@Local(argsOnly = true) ChunkAccess chunk)
	{
		LevelContext context = this.levelContext$quadragen;
		if (context == null || chunk.isUpgrading())
		{
			return;
		}
		QuadrantPlan plan = context.getPlanAt(chunk.getPos());
		if (!plan.isOrdinaryNoise())
		{
			if (plan.isFlat() && !plan.isClearGeneratedContent())
			{
				FlatLayerPlacer.placeDelayedLayers(level, chunk, plan.getFlat());
			}
			ci.cancel();
		}
	}
}
