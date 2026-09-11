/*
 * This file is part of the Quadra Gen project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Fallen_Breath and contributors
 *
 * Quadra Gen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License v3.0
 * as published by the Free Software Foundation, either version 3 of the License, or
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
import me.fallenbreath.quadragen.core.Quadrant;
import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.BiomeLocateContext;
import me.fallenbreath.quadragen.runtime.LevelContext;
import net.minecraft.world.level.biome.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//#if MC >= 1.17.1
import net.minecraft.core.QuartPos;
//#endif

//#if MC >= 1.18.2
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
//#else
//$$ import net.minecraft.world.level.biome.Biome;
//#endif

//#if MC >= 1.19.4
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.LinkedHashSet;
import java.util.Set;
//#endif

/**
 * mc >= 1.19.4: subproject 26.2 (main project)       <--------
 * 1.18.2 <= mc <= 1.19.4: subproject 26.2
 * mc <= 1.17.1: subproject 1.17.1
 * <p>
 * Adjusts only the biome values consumed by vanilla locate searches.
 */
@Mixin(BiomeSource.class)
public abstract class BiomeSourceMixin
{
	//#if MC >= 1.19.4
	@ModifyExpressionValue(
			method = "findClosestBiome3d(Lnet/minecraft/core/BlockPos;IIILjava/util/function/Predicate;Lnet/minecraft/world/level/biome/Climate$Sampler;Lnet/minecraft/world/level/LevelReader;)Lcom/mojang/datafixers/util/Pair;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/BiomeSource;possibleBiomes()Ljava/util/Set;")
	)
	private Set<Holder<Biome>> extendLocateCandidates(Set<Holder<Biome>> original)
	{
		BiomeLocateContext locateContext = BiomeLocateContext.current();
		if (locateContext == null)
		{
			return original;
		}
		LevelContext context = locateContext.getLevelContext();
		Set<Holder<Biome>> candidates = new LinkedHashSet<Holder<Biome>>(original);
		for (Quadrant quadrant : Quadrant.values())
		{
			QuadrantPlan plan = context.getPlan(quadrant);
			if (plan.isFlat())
			{
				candidates.add(plan.getFlat().getBiome());
			}
		}
		return candidates;
	}
	//#endif

	@WrapOperation(
			method =
			//#if MC >= 1.19.4
			"findClosestBiome3d(Lnet/minecraft/core/BlockPos;IIILjava/util/function/Predicate;Lnet/minecraft/world/level/biome/Climate$Sampler;Lnet/minecraft/world/level/LevelReader;)Lcom/mojang/datafixers/util/Pair;",
			//#elseif MC >= 1.18.2
			//$$ "findBiomeHorizontal(IIIIILjava/util/function/Predicate;Ljava/util/Random;ZLnet/minecraft/world/level/biome/Climate$Sampler;)Lcom/mojang/datafixers/util/Pair;",
			//#else
			//$$ "findBiomeHorizontal(IIIIILjava/util/function/Predicate;Ljava/util/Random;Z)Lnet/minecraft/core/BlockPos;",
			//#endif
			at = @At(
					value = "INVOKE",
					target =
					//#if MC >= 1.18.2
					"Lnet/minecraft/world/level/biome/BiomeSource;getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;"
					//#else
					//$$ "Lnet/minecraft/world/level/biome/BiomeSource;getNoiseBiome(III)Lnet/minecraft/world/level/biome/Biome;"
					//#endif
			)
	)
	private
			//#if MC >= 1.18.2
			Holder<Biome> useQuadrantBiome(
			//#else
			//$$ Biome useQuadrantBiome(
			//#endif
			BiomeSource source,
			int quartX,
			int quartY,
			int quartZ,
			//#if MC >= 1.18.2
			Climate.Sampler sampler,
			//#endif
			Operation<
					//#if MC >= 1.18.2
					Holder<Biome>
					//#else
					//$$ Biome
					//#endif
					> original)
	{
		//#if MC >= 1.18.2
		Holder<Biome> biome = original.call(source, quartX, quartY, quartZ, sampler);
		//#else
		//$$ Biome biome = original.call(source, quartX, quartY, quartZ);
		//#endif
		BiomeLocateContext locateContext = BiomeLocateContext.current();
		if (locateContext == null)
		{
			return biome;
		}
		LevelContext context = locateContext.getLevelContext();
		QuadrantPlan plan = context.getPlanAt(this.toBlockCoordinate(quartX), this.toBlockCoordinate(quartZ));
		return plan.isFlat() ? plan.getFlat().getBiome() : biome;
	}

	private int toBlockCoordinate(int quartCoordinate)
	{
		//#if MC >= 1.17.1
		return QuartPos.toBlock(quartCoordinate);
		//#else
		//$$ return quartCoordinate << 2;
		//#endif
	}
}
