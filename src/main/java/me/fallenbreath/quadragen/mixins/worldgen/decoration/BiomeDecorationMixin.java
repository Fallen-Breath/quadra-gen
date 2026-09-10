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

package me.fallenbreath.quadragen.mixins.worldgen.decoration;

import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.core.FlatLayerPlacement;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.18.2
import net.minecraft.SharedConstants;
import net.minecraft.world.level.WorldGenLevel;
//#else
//$$ import net.minecraft.server.level.WorldGenRegion;
//#endif

//#if 1.17.1 <= MC && MC < 1.18.2
//$$ import me.fallenbreath.quadragen.compat.ChunkPosCompat;
//$$ import net.minecraft.SharedConstants;
//#endif

@Mixin(ChunkGenerator.class)
public abstract class BiomeDecorationMixin
{
	//#if MC >= 1.18.2
	@Inject(method = "applyBiomeDecoration", at = @At("HEAD"), cancellable = true)
	private void applyBiomeDecoration(
			CallbackInfo ci,
			@Local(argsOnly = true) WorldGenLevel level,
			@Local(argsOnly = true) ChunkAccess chunk)
	{
		LevelContext context = ((GeneratorContextAccess)this).getLevelContext$quadragen();
		if (context == null || chunk.isUpgrading())
		{
			return;
		}
		QuadrantPlan plan = context.getPlanAt(chunk.getPos());
		if (!plan.isOrdinaryNoise())
		{
			if (!SharedConstants.DEBUG_DISABLE_FEATURES && plan.isFlat() && !plan.isClearGeneratedContent())
			{
				FlatLayerPlacement.placeDelayedLayers(level, chunk, plan.getFlat());
			}
			ci.cancel();
		}
	}
	//#else
	//$$ @Inject(
	//$$ 		method =
	//$$ //#if MC >= 1.16.5
	//$$ 		"applyBiomeDecoration(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureFeatureManager;)V",
	//$$ //#else
	//$$ //$$ 		"applyBiomeDecoration(Lnet/minecraft/server/level/WorldGenRegion;)V",
	//$$ //#endif
	//$$ 		at = @At("HEAD"),
	//$$ 		cancellable = true
	//$$ )
	//$$ private void applyBiomeDecoration(CallbackInfo ci, @Local(argsOnly = true) WorldGenRegion level)
	//$$ {
	//$$ 	LevelContext context = ((GeneratorContextAccess)this).getLevelContext$quadragen();
	//$$ 	if (context == null)
	//$$ 	{
	//$$ 		return;
	//$$ 	}
	//$$ //#if MC >= 1.17.1
	//$$ 	ChunkAccess chunk = level.getChunk(ChunkPosCompat.x(level.getCenter()), ChunkPosCompat.z(level.getCenter()));
	//$$ //#else
	//$$ //$$ 	ChunkAccess chunk = level.getChunk(level.getCenterX(), level.getCenterZ());
	//$$ //#endif
	//$$ 	QuadrantPlan plan = context.getPlanAt(chunk.getPos());
	//$$ 	if (!plan.isOrdinaryNoise())
	//$$ 	{
	//$$ 		if (
	//$$ //#if MC >= 1.17.1
	//$$ 				!SharedConstants.DEBUG_DISABLE_FEATURES &&
	//$$ //#endif
	//$$ 				plan.isFlat() && !plan.isClearGeneratedContent()
	//$$ 		)
	//$$ 		{
	//$$ 			FlatLayerPlacement.placeDelayedLayers(level, chunk, plan.getFlat());
	//$$ 		}
	//$$ 		ci.cancel();
	//$$ 	}
	//$$ }
	//#endif
}
