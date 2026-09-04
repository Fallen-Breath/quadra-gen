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

import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import me.fallenbreath.quadragen.worldgen.GenerationHooks;
import me.fallenbreath.quadragen.worldgen.StructurePolicy;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin implements GeneratorContextAccess
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

	@Inject(method = "createStructures", at = @At("HEAD"), cancellable = true)
	private void quadragen$createStructures(
			RegistryAccess registryAccess,
			ChunkGeneratorStructureState state,
			StructureManager structureManager,
			ChunkAccess centerChunk,
			StructureTemplateManager structureTemplateManager,
			ResourceKey<Level> level,
			CallbackInfo ci)
	{
		LevelContext context = this.quadragen$levelContext;
		if (context != null && !centerChunk.isUpgrading())
		{
			QuadrantPlan plan = GenerationHooks.plan(context, centerChunk);
			if (!StructurePolicy.shouldCreateStarts(plan))
			{
				ci.cancel();
			}
		}
	}

	@Inject(method = "applyBiomeDecoration", at = @At("HEAD"), cancellable = true)
	private void quadragen$applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager, CallbackInfo ci)
	{
		LevelContext context = this.quadragen$levelContext;
		if (context == null || chunk.isUpgrading())
		{
			return;
		}
		QuadrantPlan plan = GenerationHooks.plan(context, chunk);
		if (!plan.isOrdinaryNoise())
		{
			GenerationHooks.decorate(context, level, chunk);
			ci.cancel();
		}
	}
}
