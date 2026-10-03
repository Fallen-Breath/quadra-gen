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

package me.fallenbreath.quadragen.mixins.worldgen.biome;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fallenbreath.quadragen.runtime.BiomeLocateContext;
import me.fallenbreath.quadragen.runtime.BiomeLocateHelper;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.ServerLevelContextAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;
import java.util.function.Predicate;

//#if MC >= 26.3
//$$ import net.minecraft.world.level.levelgen.RandomState;
//#elseif MC >= 1.18.2
import net.minecraft.world.level.biome.Climate;
//#endif

//#if MC >= 1.19.4
import net.minecraft.world.level.LevelReader;
//#else
//$$ import java.util.Random;
//#endif

//#if MC >= 1.18.2
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
//#endif

/**
 * mc >  1.15.2: subproject 26.2 (main project)  <--------
 * mc <= 1.15.2: subproject 1.15.2
 * <p>
 * Scopes the vanilla ServerLevel delegation while BiomeSourceMixin adapts sampled biomes.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevel_BiomeLocateMixin
{
	//#if MC >= 1.19.4
	@WrapOperation(
			method = "findClosestBiome3d(Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;III)Lcom/mojang/datafixers/util/Pair;",
			at = @At(
					value = "INVOKE",
					target =
					//#if MC >= 26.3
					//$$ "Lnet/minecraft/world/level/biome/BiomeSource;findClosestBiome3d(Lnet/minecraft/core/BlockPos;IIILjava/util/function/Predicate;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/LevelReader;)Lcom/mojang/datafixers/util/Pair;"
					//#else
					"Lnet/minecraft/world/level/biome/BiomeSource;findClosestBiome3d(Lnet/minecraft/core/BlockPos;IIILjava/util/function/Predicate;Lnet/minecraft/world/level/biome/Climate$Sampler;Lnet/minecraft/world/level/LevelReader;)Lcom/mojang/datafixers/util/Pair;"
					//#endif
			)
	)
	private Pair<BlockPos, Holder<Biome>> scopeLocateBiome3d(
			BiomeSource source,
			BlockPos origin,
			int maxSearchRadius,
			int sampleResolutionHorizontal,
			int sampleResolutionVertical,
			Predicate<Holder<Biome>> biomeTest,
			//#if MC >= 26.3
			//$$ RandomState samplingContext,
			//#else
			Climate.Sampler samplingContext,
			//#endif
			LevelReader level,
			Operation<Pair<BlockPos, Holder<Biome>>> original
	)
	{
		LevelContext context = ((ServerLevelContextAccess)this).getLevelContext$quadragen();
		// FixedBiomeSource bypasses the search loop adapted by BiomeSourceMixin.
		if (context == null || source instanceof FixedBiomeSource || maxSearchRadius < 0 || sampleResolutionHorizontal <= 0 || sampleResolutionVertical <= 0)
		{
			return original.call(source, origin, maxSearchRadius, sampleResolutionHorizontal, sampleResolutionVertical, biomeTest, samplingContext, level);
		}
		Set<Holder<Biome>> candidates = BiomeLocateHelper.getCandidates(
				context,
				BiomeLocateHelper.getSearchQuadrants(origin.getX(), origin.getZ(), maxSearchRadius, sampleResolutionHorizontal),
				source.possibleBiomes()
		);
		if (BiomeLocateHelper.canReturnNotFound(candidates, biomeTest))
		{
			return null;
		}
		BiomeLocateContext previous = BiomeLocateContext.install(context, candidates);
		try
		{
			return original.call(source, origin, maxSearchRadius, sampleResolutionHorizontal, sampleResolutionVertical, biomeTest, samplingContext, level);
		}
		finally
		{
			BiomeLocateContext.restore(previous);
		}
	}
	//#else
	//$$ @WrapOperation(
	//$$ 		method =
			//#if MC >= 1.18.2
			//$$ "findNearestBiome(Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;II)Lcom/mojang/datafixers/util/Pair;",
			//#else
			//$$ "findNearestBiome(Lnet/minecraft/world/level/biome/Biome;Lnet/minecraft/core/BlockPos;II)Lnet/minecraft/core/BlockPos;",
			//#endif
	//$$ 		at = @At(
	//$$ 				value = "INVOKE",
	//$$ 				target =
					//#if MC >= 1.18.2
					//$$ "Lnet/minecraft/world/level/biome/BiomeSource;findBiomeHorizontal(IIIIILjava/util/function/Predicate;Ljava/util/Random;ZLnet/minecraft/world/level/biome/Climate$Sampler;)Lcom/mojang/datafixers/util/Pair;"
					//#else
					//$$ "Lnet/minecraft/world/level/biome/BiomeSource;findBiomeHorizontal(IIIIILjava/util/function/Predicate;Ljava/util/Random;Z)Lnet/minecraft/core/BlockPos;"
					//#endif
	//$$ 		)
	//$$ )
	//$$ private
			//#if MC >= 1.18.2
			//$$ Pair<BlockPos, Holder<Biome>> scopeLocateBiomeHorizontal(
			//#else
			//$$ BlockPos scopeLocateBiomeHorizontal(
			//#endif
	//$$ 		BiomeSource source,
	//$$ 		int x,
	//$$ 		int y,
	//$$ 		int z,
	//$$ 		int maxSearchRadius,
	//$$ 		int sampleResolution,
			//#if MC >= 1.18.2
			//$$ Predicate<Holder<Biome>> biomeTest,
			//#else
			//$$ Predicate<Biome> biomeTest,
			//#endif
	//$$ 		Random random,
	//$$ 		boolean findClosest,
			//#if MC >= 1.18.2
			//$$ Climate.Sampler sampler,
			//$$ Operation<Pair<BlockPos, Holder<Biome>>> original
			//#else
			//$$ Operation<BlockPos> original
			//#endif
	//$$ )
	//$$ {
	//$$ 	LevelContext context = ((ServerLevelContextAccess)this).getLevelContext$quadragen();
	//$$ 	// FixedBiomeSource bypasses the search loop adapted by BiomeSourceMixin.
	//$$ 	if (context == null || source instanceof FixedBiomeSource || maxSearchRadius < 0 || sampleResolution <= 0 || !findClosest)
	//$$ 	{
			//#if MC >= 1.18.2
			//$$ return original.call(source, x, y, z, maxSearchRadius, sampleResolution, biomeTest, random, findClosest, sampler);
			//#else
			//$$ return original.call(source, x, y, z, maxSearchRadius, sampleResolution, biomeTest, random, findClosest);
			//#endif
	//$$ 	}
		//#if MC >= 1.18.2
		//$$ Set<Holder<Biome>> candidates = BiomeLocateHelper.getCandidates(
		//#else
		//$$ Set<Biome> candidates = BiomeLocateHelper.getCandidates(
		//#endif
	//$$ 			context,
	//$$ 			BiomeLocateHelper.getSearchQuadrants(x, z, maxSearchRadius, sampleResolution),
	//$$ 			source.possibleBiomes()
	//$$ 	);
	//$$ 	if (BiomeLocateHelper.canReturnNotFound(candidates, biomeTest))
	//$$ 	{
	//$$ 		return null;
	//$$ 	}
	//$$ 	BiomeLocateContext previous = BiomeLocateContext.install(context, candidates);
	//$$ 	try
	//$$ 	{
			//#if MC >= 1.18.2
			//$$ return original.call(source, x, y, z, maxSearchRadius, sampleResolution, biomeTest, random, findClosest, sampler);
			//#else
			//$$ return original.call(source, x, y, z, maxSearchRadius, sampleResolution, biomeTest, random, findClosest);
			//#endif
	//$$ 	}
	//$$ 	finally
	//$$ 	{
	//$$ 		BiomeLocateContext.restore(previous);
	//$$ 	}
	//$$ }
	//#endif
}
