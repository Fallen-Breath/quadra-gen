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

package me.fallenbreath.quadragen.mixins.network.advertise;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.runtime.AdvertisedWorldTypeQuery;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 *           mc >  1.20.1: subproject 26.2 (main project)
 * 1.16.5 <= mc <= 1.20.1: subproject 1.20.1
 *           mc <= 1.15.2: subproject 1.15.2  <--------
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin
{
	/** Matches {@link net.minecraft.server.level.ServerPlayer#changeDimension(DimensionType)}, including its Math.min lower bound. */
	@Unique
	private int clampLegacyPortalCoordinate(double coordinate, double borderMin, double borderMax)
	{
		final double coordinateLimit = 2.9999872E7;
		final double borderInset = 16.0;
		double min = Math.min(-coordinateLimit, borderMin + borderInset);
		double max = Math.min(coordinateLimit, borderMax - borderInset);
		return Mth.floor(Mth.clamp(coordinate, min, max));
	}

	@ModifyExpressionValue(
			method = "changeDimension(Lnet/minecraft/world/level/dimension/DimensionType;)Lnet/minecraft/world/entity/Entity;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/storage/LevelData;getGeneratorType()Lnet/minecraft/world/level/LevelType;"
			)
	)
	private LevelType advertiseWorldType_modifyLevelType1(LevelType original, @Local(ordinal = 1) ServerLevel level)
	{
		ServerPlayer self = (ServerPlayer)(Object)this;
		if (level.getDimension().getType() == DimensionType.THE_END)
		{
			// Match the End destination in {@link net.minecraft.server.level.ServerPlayer#changeDimension(DimensionType)}.
			//#if MC >= 1.15.2
			double x = self.getX();
			double z = self.getZ();
			//#else
			//$$ double x = self.x;
			//$$ double z = self.z;
			//#endif
			// The player's dimension field already points to the destination; its level still identifies the source.
			if (self.getLevel().getDimension().getType() == DimensionType.OVERWORLD)
			{
				BlockPos spawn = level.getDimensionSpecificSpawn();
				x = spawn.getX();
				z = spawn.getZ();
			}
			WorldBorder border = level.getWorldBorder();
			x = this.clampLegacyPortalCoordinate(x, border.getMinX(), border.getMaxX());
			z = this.clampLegacyPortalCoordinate(z, border.getMinZ(), border.getMaxZ());
			return AdvertisedWorldTypeQuery.shouldAdvertiseFlat(level, x, z, original);
		}
		// Accepted compromise: Nether portal AUTO uses departure X/Z to preserve vanilla packet timing.
		return AdvertisedWorldTypeQuery.shouldAdvertiseFlat(level, self, original);
	}

	@ModifyExpressionValue(
			method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/storage/LevelData;getGeneratorType()Lnet/minecraft/world/level/LevelType;"
			)
	)
	private LevelType advertiseWorldType_modifyLevelType2(LevelType original,
			@Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true, ordinal = 0) double x, @Local(argsOnly = true, ordinal = 2) double z)
	{
		return AdvertisedWorldTypeQuery.shouldAdvertiseFlat(level, x, z, original);
	}
}
