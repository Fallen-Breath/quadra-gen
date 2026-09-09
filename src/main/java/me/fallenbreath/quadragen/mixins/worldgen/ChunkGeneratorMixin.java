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
import net.minecraft.SharedConstants;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if 1.16.5 <= MC && MC < 1.19.4
//$$ import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//$$ import java.util.ArrayList;
//$$ import java.util.List;
//#endif

//#if 1.15.2 <= MC && MC < 1.18.2
//$$ import net.minecraft.world.level.ChunkPos;
//#endif

//#if MC >= 1.16.5
import net.minecraft.world.level.WorldGenLevel;
//#endif

//#if 1.18.2 <= MC && MC < 1.19.4
//$$ import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//$$ import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.core.Vec3i;
//$$ import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
//#endif

//#if 1.15.2 <= MC && MC < 1.18.2
//$$ import me.fallenbreath.quadragen.compat.ChunkPosCompat;
//$$ import net.minecraft.core.Registry;
//$$ import net.minecraft.server.level.WorldGenRegion;
//$$ import net.minecraft.world.level.biome.Biome;
//$$ import net.minecraft.world.level.chunk.ChunkBiomeContainer;
//$$ import net.minecraft.world.level.chunk.ProtoChunk;
//$$ import java.util.Arrays;
//#endif

//#if 1.16.5 <= MC && MC < 1.18.2
//$$ import net.minecraft.world.level.StructureFeatureManager;
//#endif

//#if 1.15.2 <= MC && MC < 1.17.1
//$$ import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//$$ import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//$$ import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
//$$ import java.util.Random;
//#endif

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

	//#if 1.15.2 <= MC && MC < 1.18.2
	//$$ /**
	//$$  * Mirrors {@link net.minecraft.world.level.chunk.ChunkGenerator#createBiomes} with a constant biome container
	//$$  * for Flat quadrants.
	//$$  */
	//$$ @Inject(
	//$$ 		method =
	//$$ //#if MC >= 1.16.5
	//$$ 		"createBiomes(Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
	//$$ //#else
	//$$ //$$ 		"createBiomes(Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
	//$$ //#endif
	//$$ 		at = @At("HEAD"),
	//$$ 		cancellable = true
	//$$ )
	//$$ private void createBiomes(
	//$$ 		CallbackInfo ci,
	//$$ //#if MC >= 1.16.5
	//$$ 		@Local(argsOnly = true) Registry<Biome> biomeRegistry,
	//$$ //#endif
	//$$ 		@Local(argsOnly = true) ChunkAccess chunk)
	//$$ {
	//$$ 	LevelContext context = this.levelContext$quadragen;
	//$$ 	if (context != null)
	//$$ 	{
	//$$ 		QuadrantPlan plan = context.getPlanAt(chunk.getPos());
	//$$ 		if (plan.isFlat())
	//$$ 		{
	//$$ //#if MC >= 1.17.1
	//$$ 			int[] biomeIds = new int[16 * ((chunk.getHeight() + 3) / 4)];
	//$$ 			Arrays.fill(biomeIds, biomeRegistry.getId(plan.getFlat().getBiome()));
	//$$ 			((ProtoChunk)chunk).setBiomes(new ChunkBiomeContainer(biomeRegistry, chunk, biomeIds));
	//$$ //#else
	//$$ //$$ 			Biome[] biomes = new Biome[ChunkBiomeContainer.BIOMES_SIZE];
	//$$ //$$ 			Arrays.fill(biomes, plan.getFlat().getBiome());
	//$$ //$$ //#if MC >= 1.16.5
	//$$ //$$ 			((ProtoChunk)chunk).setBiomes(new ChunkBiomeContainer(biomeRegistry, biomes));
	//$$ //$$ //#else
	//$$ //$$ //$$ 			((ProtoChunk)chunk).setBiomes(new ChunkBiomeContainer(biomes));
	//$$ //$$ //#endif
	//$$ //#endif
	//$$ 			ci.cancel();
	//$$ 		}
	//$$ 	}
	//$$ }
	//#endif

	@Inject(method = "createStructures", at = @At("HEAD"), cancellable = true)
	private void createStructures(CallbackInfo ci, @Local(argsOnly = true) ChunkAccess centerChunk)
	{
		LevelContext context = this.levelContext$quadragen;
		if (context != null && !this.isUpgrading$quadragen(centerChunk))
		{
			QuadrantPlan plan = context.getPlanAt(centerChunk.getPos());
			if (plan.isFlat())
			{
				ci.cancel();
			}
		}
	}

	//#if MC >= 1.18.2
	@Inject(method = "applyBiomeDecoration", at = @At("HEAD"), cancellable = true)
	private void applyBiomeDecoration(
			CallbackInfo ci,
			@Local(argsOnly = true) WorldGenLevel level,
			@Local(argsOnly = true) ChunkAccess chunk)
	{
		LevelContext context = this.levelContext$quadragen;
		if (context == null || this.isUpgrading$quadragen(chunk))
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
	//#elseif MC >= 1.15.2
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
	//$$ 	LevelContext context = this.levelContext$quadragen;
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
	//#else
	//$$ // TODO: Port decoration routing against the target MC source.
	//$$ TODO_PORT_MC_VERSION;
	//#endif

	//#if 1.18.2 <= MC && MC < 1.19.4
	//$$ /**
	//$$  * Filters the precomputed positions consumed by
	//$$  * {@link net.minecraft.world.level.chunk.ChunkGenerator#getNearestGeneratedStructure} because this version does not
	//$$  * validate concentric-ring structure starts during locate.
	//$$  */
	//$$ @ModifyExpressionValue(
	//$$ 		method = "getNearestGeneratedStructure(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/placement/ConcentricRingsStructurePlacement;)Lnet/minecraft/core/BlockPos;",
	//$$ 		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;getRingPositionsFor(Lnet/minecraft/world/level/levelgen/structure/placement/ConcentricRingsStructurePlacement;)Ljava/util/List;")
	//$$ )
	//$$ private List<ChunkPos> filterConcentricRingCandidates(List<ChunkPos> original)
	//$$ {
	//$$ 	LevelContext context = this.levelContext$quadragen;
	//$$ 	if (context == null)
	//$$ 	{
	//$$ 		return original;
	//$$ 	}
	//$$ 	List<ChunkPos> filtered = new ArrayList<ChunkPos>();
	//$$ 	for (ChunkPos candidate : original)
	//$$ 	{
	//$$ 		if (context.getPlanAt(candidate).isNoise())
	//$$ 		{
	//$$ 			filtered.add(candidate);
	//$$ 		}
	//$$ 	}
	//$$ 	return filtered;
	//$$ }

	//$$ @WrapOperation(
	//$$ 		method = "findNearestMapFeature",
	//$$ 		at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;distSqr(Lnet/minecraft/core/Vec3i;)D", ordinal = 0)
	//$$ )
	//$$ private double ignoreMissingConcentricRingCandidate(BlockPos origin, Vec3i candidate, Operation<Double> original)
	//$$ {
	//$$ 	return candidate == null ? Double.MAX_VALUE : original.call(origin, candidate);
	//$$ }
	//#endif

	//#if 1.15.2 <= MC && MC < 1.18.2
	//$$ @Inject(method = "applyCarvers", at = @At("HEAD"), cancellable = true)
	//$$ private void applyCarvers(CallbackInfo ci, @Local(argsOnly = true) ChunkAccess chunk)
	//$$ {
	//$$ 	LevelContext context = this.levelContext$quadragen;
	//$$ 	if (context != null && !context.getPlanAt(chunk.getPos()).isOrdinaryNoise())
	//$$ 	{
	//$$ 		ci.cancel();
	//$$ 	}
	//$$ }

	//$$ //#if MC >= 1.17.1
	//$$ @ModifyExpressionValue(
	//$$ 		method = "applyCarvers",
	//$$ 		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/carver/ConfiguredWorldCarver;isStartChunk(Ljava/util/Random;)Z")
	//$$ )
	//$$ private boolean filterCarverSource(boolean isStartChunk, @Local(ordinal = 1) ChunkPos sourcePos)
	//$$ {
	//$$ 	if (!isStartChunk)
	//$$ 	{
	//$$ 		return false;
	//$$ 	}
	//$$ 	LevelContext context = this.levelContext$quadragen;
	//$$ 	return context == null || context.getPlanAt(sourcePos).isOrdinaryNoise();
	//$$ }
	//$$ //#else
	//$$ //$$ @WrapOperation(
	//$$ //$$ 		method = "applyCarvers",
	//$$ //$$ 		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/carver/ConfiguredWorldCarver;isStartChunk(Ljava/util/Random;II)Z")
	//$$ //$$ )
	//$$ //$$ private boolean filterCarverSource(
	//$$ //$$ 		ConfiguredWorldCarver<?> carver,
	//$$ //$$ 		Random random,
	//$$ //$$ 		int sourceX,
	//$$ //$$ 		int sourceZ,
	//$$ //$$ 		Operation<Boolean> original)
	//$$ //$$ {
	//$$ //$$ 	if (!original.call(carver, random, sourceX, sourceZ))
	//$$ //$$ 	{
	//$$ //$$ 		return false;
	//$$ //$$ 	}
	//$$ //$$ 	LevelContext context = this.levelContext$quadragen;
	//$$ //$$ 	return context == null || context.getPlanAt(new ChunkPos(sourceX, sourceZ)).isOrdinaryNoise();
	//$$ //$$ }
	//$$ //#endif
	//#endif

	//#if 1.16.5 <= MC && MC < 1.18.2
	//$$ /**
	//$$  * Filters the precomputed positions read by {@link net.minecraft.world.level.chunk.ChunkGenerator#findNearestMapFeature}
	//$$  * because its stronghold branch does not validate structure starts.
	//$$  */
	//$$ @ModifyExpressionValue(
	//$$ 		method = "findNearestMapFeature",
	//$$ 		at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;strongholdPositions:Ljava/util/List;")
	//$$ )
	//$$ private List<ChunkPos> filterStrongholdCandidates(List<ChunkPos> original)
	//$$ {
	//$$ 	LevelContext context = this.levelContext$quadragen;
	//$$ 	if (context == null)
	//$$ 	{
	//$$ 		return original;
	//$$ 	}
	//$$ 	List<ChunkPos> filtered = new ArrayList<ChunkPos>();
	//$$ 	for (ChunkPos candidate : original)
	//$$ 	{
	//$$ 		if (context.getPlanAt(candidate).isNoise())
	//$$ 		{
	//$$ 			filtered.add(candidate);
	//$$ 		}
	//$$ 	}
	//$$ 	return filtered;
	//$$ }
	//#endif

	@Unique
	private boolean isUpgrading$quadragen(ChunkAccess chunk)
	{
		//#if MC >= 1.18.2
		return chunk.isUpgrading();
		//#elseif MC >= 1.15.2
		//$$ return false;
		//#else
		//$$ // TODO: Port upgrading detection against the target MC source.
		//$$ return TODO_PORT_MC_VERSION;
		//#endif
	}
}
