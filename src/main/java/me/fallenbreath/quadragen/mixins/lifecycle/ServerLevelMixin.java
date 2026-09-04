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
import me.fallenbreath.quadragen.worldgen.BiomeQuery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ServerLevelContextAccess
{
	@Unique
	private volatile LevelContext quadragen$levelContext;

	@Override
	public LevelContext quadragen$getLevelContext()
	{
		return this.quadragen$levelContext;
	}

	@Override
	public void quadragen$setLevelContext(LevelContext context)
	{
		this.quadragen$levelContext = context;
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void quadragen$installContext(
			MinecraftServer server,
			Executor executor,
			LevelStorageSource.LevelStorageAccess levelStorage,
			ServerLevelData levelData,
			ResourceKey<Level> dimension,
			LevelStem levelStem,
			boolean isDebug,
			long biomeZoomSeed,
			List<CustomSpawner> customSpawners,
			boolean tickTime,
			CallbackInfo ci)
	{
		LevelBootstrap.install((ServerLevel)(Object)this, levelStem.generator());
	}

	@Inject(
			method = "findClosestBiome3d",
			at = @At("HEAD"),
			cancellable = true
	)
	private void quadragen$findClosestBiome3d(
			Predicate<Holder<Biome>> biomeTest,
			BlockPos origin,
			int maxSearchRadius,
			int sampleResolutionHorizontal,
			int sampleResolutionVertical,
			CallbackInfoReturnable<Pair<BlockPos, Holder<Biome>>> cir)
	{
		LevelContext context = this.quadragen$levelContext;
		if (context != null)
		{
			cir.setReturnValue(BiomeQuery.findClosestBiome3d(
					context,
					(ServerLevel)(Object)this,
					biomeTest,
					origin,
					maxSearchRadius,
					sampleResolutionHorizontal,
					sampleResolutionVertical
			));
		}
	}
}
