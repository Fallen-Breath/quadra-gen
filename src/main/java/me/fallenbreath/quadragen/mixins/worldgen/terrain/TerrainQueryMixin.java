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

package me.fallenbreath.quadragen.mixins.worldgen.terrain;

import me.fallenbreath.quadragen.core.QuadrantPlan;
import me.fallenbreath.quadragen.runtime.LevelContext;
import me.fallenbreath.quadragen.runtime.access.GeneratorContextAccess;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC >= 1.17.1
import net.minecraft.world.level.LevelHeightAccessor;
//#endif

//#if MC >= 1.17.1
import net.minecraft.world.level.NoiseColumn;
//#elseif MC >= 1.16.5
//$$ import net.minecraft.world.level.BlockGetter;
//#endif

//#if MC >= 1.18.2
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
//#endif

//#if MC >= 1.19.4
import net.minecraft.world.level.levelgen.RandomState;
//#endif

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class TerrainQueryMixin
{
	@Inject(method = "getBaseHeight", at = @At("HEAD"), cancellable = true)
	private void getBaseHeight(
			int x,
			int z,
			Heightmap.Types type,
			//#if MC >= 1.17.1
			LevelHeightAccessor heightAccessor,
			//#endif
			//#if MC >= 1.19.4
			RandomState randomState,
			//#endif
			CallbackInfoReturnable<Integer> cir)
	{
		LevelContext context = this.getContext$quadragen();
		if (context != null
				//#if MC >= 1.17.1
				&& !this.isUpgradingHeightAccessor$quadragen(heightAccessor)
				//#endif
		)
		{
			QuadrantPlan plan = context.getPlanAt(x, z);
			if (plan.isFlat())
			{
				cir.setReturnValue(plan.getFlat().getBaseHeightFromConfiguredLayers(
						type
						//#if MC >= 1.17.1
						, heightAccessor
						//#endif
				));
			}
		}
	}

	//#if MC >= 1.16.5
	@Inject(method = "getBaseColumn", at = @At("HEAD"), cancellable = true)
	private void getBaseColumn(
			int x,
			int z,
			//#if MC >= 1.17.1
			LevelHeightAccessor heightAccessor,
			//#endif
			//#if MC >= 1.19.4
			RandomState randomState,
			//#endif
			CallbackInfoReturnable<
					//#if MC >= 1.17.1
					NoiseColumn
					//#else
					//$$ BlockGetter
					//#endif
			> cir)
	{
		LevelContext context = this.getContext$quadragen();
		if (context != null
				//#if MC >= 1.17.1
				&& !this.isUpgradingHeightAccessor$quadragen(heightAccessor)
				//#endif
		)
		{
			QuadrantPlan plan = context.getPlanAt(x, z);
			if (plan.isFlat())
			{
				cir.setReturnValue(plan.getFlat().getBaseColumnFromConfiguredLayers(
						//#if MC >= 1.17.1
						heightAccessor
						//#endif
				));
			}
		}
	}
	//#endif

	//#if MC >= 1.17.1
	@Unique
	private boolean isUpgradingHeightAccessor$quadragen(LevelHeightAccessor heightAccessor)
	{
		//#if MC >= 1.18.2
		return heightAccessor == BelowZeroRetrogen.UPGRADE_HEIGHT_ACCESSOR || heightAccessor instanceof ChunkAccess && ((ChunkAccess)heightAccessor).isUpgrading();
		//#else
		//$$ return false;
		//#endif
	}
	//#endif

	@Unique
	private LevelContext getContext$quadragen()
	{
		return ((GeneratorContextAccess)this).getLevelContext$quadragen();
	}
}
