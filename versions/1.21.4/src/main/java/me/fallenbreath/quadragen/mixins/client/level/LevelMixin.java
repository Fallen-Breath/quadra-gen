/*
 * This file is part of the Quadra Gen project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Fallen_Breath and contributors
 */

package me.fallenbreath.quadragen.mixins.client.level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.runtime.ClientSyncQuery;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * mc >= 1.21.8: subproject 26.2 (main project)
 * mc in [1.21.5, 1.21.8): subproject 1.21.5
 * mc in [1.21.4, 1.21.5): subproject 1.21.4                    <--------
 * mc < 1.21.4: subproject 1.21.1
 * <p>
 * 1.21.4 performs the precipitation sea-level lookup directly in {@code isRainingAt}.
 */
@Mixin(Level.class)
public abstract class LevelMixin
{
	@ModifyExpressionValue(
			method = "isRainingAt(Lnet/minecraft/core/BlockPos;)Z",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getSeaLevel()I")
	)
	private int useQuadrantSeaLevel(int original, @Local(argsOnly = true) BlockPos pos)
	{
		Level level = (Level)(Object)this;
		return level instanceof ClientLevel ? ClientSyncQuery.getSeaLevelAt(pos, original) : original;
	}
}
