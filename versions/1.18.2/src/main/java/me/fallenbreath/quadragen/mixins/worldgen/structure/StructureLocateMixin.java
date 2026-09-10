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

package me.fallenbreath.quadragen.mixins.worldgen.structure;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/**
 * mc >= 1.19.4: subproject 26.2 (main project)
 * mc == 1.18.2: subproject 1.18.2                    <--------
 * 1.16.5 <= mc <= 1.17.1: subproject 1.17.1
 * mc <= 1.15.2: subproject 1.15.2
 */
@Mixin(ChunkGenerator.class)
public abstract class StructureLocateMixin
{
	/**
	 * Filters the precomputed positions consumed by
	 * {@link net.minecraft.world.level.chunk.ChunkGenerator#getNearestGeneratedStructure} because this version does not
	 * validate concentric-ring structure starts during locate.
	 */
	@ModifyExpressionValue(
			method = "getNearestGeneratedStructure(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/placement/ConcentricRingsStructurePlacement;)Lnet/minecraft/core/BlockPos;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;getRingPositionsFor(Lnet/minecraft/world/level/levelgen/structure/placement/ConcentricRingsStructurePlacement;)Ljava/util/List;")
	)
	private List<ChunkPos> filterConcentricRingCandidates(List<ChunkPos> original)
	{
		LevelContext context = ((GeneratorContextAccess)this).getLevelContext$quadragen();
		if (context == null)
		{
			return original;
		}
		List<ChunkPos> filtered = new ArrayList<ChunkPos>();
		for (ChunkPos candidate : original)
		{
			if (context.getPlanAt(candidate).isNoise())
			{
				filtered.add(candidate);
			}
		}
		return filtered;
	}

	@WrapOperation(
			method = "findNearestMapFeature",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;distSqr(Lnet/minecraft/core/Vec3i;)D", ordinal = 0)
	)
	private double ignoreMissingConcentricRingCandidate(BlockPos origin, Vec3i candidate, Operation<Double> original)
	{
		return candidate == null ? Double.MAX_VALUE : original.call(origin, candidate);
	}
}
