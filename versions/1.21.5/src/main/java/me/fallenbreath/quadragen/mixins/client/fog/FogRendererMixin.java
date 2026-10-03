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

package me.fallenbreath.quadragen.mixins.client.fog;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.runtime.ClientSyncQuery;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * mc >  1.21.5: subproject 26.2 (main project)
 * mc <= 1.21.5: subproject 1.21.5  <--------
 * <p>
 * Void fog uses the clear-color scale in this interval; versions before 1.16.5 obtain it from Dimension.
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin
{
	@ModifyExpressionValue(
			//#if MC >= 1.21.3
			method = "computeFogColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IF)Lorg/joml/Vector4f;",
			//#elseif MC >= 1.15.2
			//$$ method = "setupColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IF)V",
			//#else
			//$$ method = "setupClearColor(Lnet/minecraft/client/Camera;F)V",
			//#endif
			at = @At(
					value = "INVOKE",
					//#if MC >= 1.18.2
					target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;getClearColorScale()F"
					//#elseif MC >= 1.16.5
					//$$ target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;getClearColorScale()D"
					//#else
					//$$ target = "Lnet/minecraft/world/level/dimension/Dimension;getClearColorScale()D"
					//#endif
			)
	)
	//#if MC >= 1.18.2
	private static float useQuadrantClearColorScale(float original, @Local(argsOnly = true) Camera camera)
	//#elseif MC >= 1.15.2
	//$$ private static double useQuadrantClearColorScale(double original, @Local(argsOnly = true) Camera camera)
	//#else
	//$$ private double useQuadrantClearColorScale(double original, @Local(argsOnly = true) Camera camera)
	//#endif
	{
		return ClientSyncQuery.getClearColorScale(
				//#if MC >= 1.16.5 && MC < 1.18.2
				//$$ (float) original,
				//#else
				original,
				//#endif
				camera.getPosition().x, camera.getPosition().z
		);
	}
}
