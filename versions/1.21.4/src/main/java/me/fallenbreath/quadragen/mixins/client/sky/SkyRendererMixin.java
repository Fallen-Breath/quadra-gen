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
 */

package me.fallenbreath.quadragen.mixins.client.sky;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.runtime.ClientSyncQuery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * mc >= 1.21.5: subproject 1.21.5
 * mc <= 1.21.4: subproject 1.21.4                    <--------
 * <p>
 * 1.21.4 keeps the dark-disc decision in {@link LevelRenderer}, rather than in {@code SkyRenderer}.
 */
@Mixin(LevelRenderer.class)
public abstract class SkyRendererMixin
{
	@ModifyExpressionValue(
			method = "shouldRenderDarkDisc(F)Z",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;getHorizonHeight(Lnet/minecraft/world/level/LevelHeightAccessor;)D"
			)
	)
	private double useQuadrantHorizonHeight(double original, @Local(argsOnly = true) float deltaPartialTick)
	{
		var eyePosition = Minecraft.getInstance().player.getEyePosition(deltaPartialTick);
		return ClientSyncQuery.getHorizonHeight(Minecraft.getInstance().level, original, eyePosition.x, eyePosition.z);
	}
}
