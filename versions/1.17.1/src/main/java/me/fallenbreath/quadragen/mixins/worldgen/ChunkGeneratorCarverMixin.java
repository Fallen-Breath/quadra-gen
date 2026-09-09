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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC < 1.17.1
//$$ import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
//$$ import java.util.Random;
//#endif

/**
 * mc >= 1.18.2: subproject 26.2 (main project)
 * mc <= 1.17.1: subproject 1.17.1                    <--------
 */
@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorCarverMixin
{
	@Inject(method = "applyCarvers", at = @At("HEAD"), cancellable = true)
	private void applyCarvers(CallbackInfo ci, @Local(argsOnly = true) ChunkAccess chunk)
	{
		LevelContext context = ((GeneratorContextAccess)this).getLevelContext$quadragen();
		if (context != null && !context.getPlanAt(chunk.getPos()).isOrdinaryNoise())
		{
			ci.cancel();
		}
	}

	//#if MC >= 1.17.1
	@ModifyExpressionValue(
			method = "applyCarvers",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/carver/ConfiguredWorldCarver;isStartChunk(Ljava/util/Random;)Z")
	)
	private boolean filterCarverSource(boolean isStartChunk, @Local(ordinal = 1) ChunkPos sourcePos)
	{
		if (!isStartChunk)
		{
			return false;
		}
		LevelContext context = ((GeneratorContextAccess)this).getLevelContext$quadragen();
		return context == null || context.getPlanAt(sourcePos).isOrdinaryNoise();
	}
	//#else
	//$$ @WrapOperation(
	//$$ 		method = "applyCarvers",
	//$$ 		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/carver/ConfiguredWorldCarver;isStartChunk(Ljava/util/Random;II)Z")
	//$$ )
	//$$ private boolean filterCarverSource(
	//$$ 		ConfiguredWorldCarver<?> carver,
	//$$ 		Random random,
	//$$ 		int sourceX,
	//$$ 		int sourceZ,
	//$$ 		Operation<Boolean> original)
	//$$ {
	//$$ 	if (!original.call(carver, random, sourceX, sourceZ))
	//$$ 	{
	//$$ 		return false;
	//$$ 	}
	//$$ 	LevelContext context = ((GeneratorContextAccess)this).getLevelContext$quadragen();
	//$$ 	return context == null || context.getPlanAt(new ChunkPos(sourceX, sourceZ)).isOrdinaryNoise();
	//$$ }
	//#endif
}
