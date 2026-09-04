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

import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.ProtoChunkContextAccess;
import me.fallenbreath.quadragen.worldgen.CarverPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(ConfiguredWorldCarver.class)
public abstract class ConfiguredWorldCarverMixin
{
	@Inject(method = "carve", at = @At("HEAD"), cancellable = true)
	private void quadragen$filterCarverSource(
			CarvingContext carvingContext,
			ChunkAccess chunk,
			Function<BlockPos, Holder<Biome>> biomeGetter,
			RandomSource random,
			Aquifer aquifer,
			ChunkPos sourceChunkPos,
			CarvingMask mask,
			CallbackInfoReturnable<Boolean> cir)
	{
		if (chunk instanceof ProtoChunkContextAccess)
		{
			LevelContext context = ((ProtoChunkContextAccess)chunk).quadragen$getLevelContext();
			if (context != null && !CarverPolicy.shouldCarveSource(context, sourceChunkPos))
			{
				cir.setReturnValue(false);
			}
		}
	}
}
