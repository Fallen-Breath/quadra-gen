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

package me.fallenbreath.quadragen.mixins.lifecycle;

import com.mojang.datafixers.util.Pair;
import me.fallenbreath.quadragen.runtime.LevelBootstrap;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.ServerLevelContextAccess;
import me.fallenbreath.quadragen.runtime.BiomeQuery;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

//#if MC >= 1.18.2
import net.minecraft.core.Holder;
//#endif

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ServerLevelContextAccess
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

	@Inject(method = "<init>", at = @At("RETURN"))
	private void installContext(CallbackInfo ci)
	{
		LevelBootstrap.install((ServerLevel)(Object)this);
	}

	//#if MC >= 1.18.2
	@Inject(
			//#if MC >= 1.19.4
			method = "findClosestBiome3d",
			//#else
			//$$ method = "findNearestBiome",
			//#endif
			at = @At("HEAD"),
			cancellable = true
	)
	private void findClosestBiome3d(
			Predicate<Holder<Biome>> biomeTest,
			BlockPos origin,
			int maxSearchRadius,
			int sampleResolutionHorizontal,
			//#if MC >= 1.19.4
			int sampleResolutionVertical,
			//#endif
			CallbackInfoReturnable<Pair<BlockPos, Holder<Biome>>> cir)
	{
		LevelContext context = this.levelContext$quadragen;
		if (context != null)
		{
			//#if MC >= 1.19.4
			cir.setReturnValue(BiomeQuery.findClosestBiome3d(
					context,
					(ServerLevel)(Object)this,
					biomeTest,
					origin,
					maxSearchRadius,
					sampleResolutionHorizontal,
					sampleResolutionVertical
			));
			//#else
			//$$ cir.setReturnValue(BiomeQuery.findNearestBiome(context, biomeTest, origin, maxSearchRadius, sampleResolutionHorizontal));
			//#endif
		}
	}
	//#elseif MC >= 1.16.5
	//$$ @Inject(method = "findNearestBiome", at = @At("HEAD"), cancellable = true)
	//$$ private void findNearestBiome(
	//$$ 		Biome biome,
	//$$ 		BlockPos origin,
	//$$ 		int maxSearchRadius,
	//$$ 		int sampleResolution,
	//$$ 		CallbackInfoReturnable<BlockPos> cir)
	//$$ {
	//$$ 	LevelContext context = this.levelContext$quadragen;
	//$$ 	if (context != null)
	//$$ 	{
	//$$ 		cir.setReturnValue(BiomeQuery.findNearestBiome(context, biome, origin, maxSearchRadius, sampleResolution));
	//$$ 	}
	//$$ }
	//#else
	//$$ // TODO: Port biome lookup against the target MC source.
	//$$ TODO_PORT_MC_VERSION;
	//#endif
}
