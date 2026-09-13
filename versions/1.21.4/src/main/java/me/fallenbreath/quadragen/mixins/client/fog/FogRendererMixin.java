/*
 * This file is part of the Quadra Gen project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Fallen_Breath and contributors
 */

package me.fallenbreath.quadragen.mixins.client.fog;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.runtime.ClientSyncQuery;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * mc >= 1.21.5: subproject 1.21.5
 * mc <= 1.21.4: subproject 1.21.4                    <--------
 * <p>
 * 1.21.4 names the vanilla void-fog value {@code getClearColorScale}.
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin
{
	@ModifyExpressionValue(
			method = "computeFogColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IF)Lorg/joml/Vector4f;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;getClearColorScale()F"
			)
	)
	private float useQuadrantClearColorScale(float original, @Local(argsOnly = true) Camera camera)
	{
		return ClientSyncQuery.getClearColorScale(original, camera.getPosition().x, camera.getPosition().z);
	}
}
