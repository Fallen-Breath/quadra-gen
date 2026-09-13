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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * mc >= 1.21.8: subproject 26.2 (main project)       <--------
 * mc in [1.21.5, 1.21.8): subproject 1.21.5
 * mc in [1.21.1, 1.21.5): subproject 1.21.4
 * mc < 1.21.1: subproject 1.20.6
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin
{
	@ModifyExpressionValue(
			//#if MC >= 26.1.2
			method = "computeFogColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IFLorg/joml/Vector4f;)V",
			//#elseif MC >= 1.21.11
			//$$ method = "computeFogColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IF)Lorg/joml/Vector4f;",
			//#elseif MC >= 1.21.8
			//$$ method = "computeFogColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IFZ)Lorg/joml/Vector4f;",
			//#else
			//$$ method = "computeFogColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IF)Lorg/joml/Vector4f;",
			//#endif
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;voidDarknessOnsetRange()F"
			)
	)
	private float useQuadrantVoidDarknessRange(
			float original,
			@Local(argsOnly = true) Camera camera
	)
	{
		return ClientSyncQuery.getVoidDarknessOnsetRange(original, camera.position().x, camera.position().z);
	}
}
