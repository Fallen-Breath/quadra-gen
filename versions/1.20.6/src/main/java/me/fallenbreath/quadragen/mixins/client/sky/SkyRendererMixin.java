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

package me.fallenbreath.quadragen.mixins.client.sky;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.quadragen.runtime.ClientSyncQuery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 */
@Mixin(LevelRenderer.class)
public abstract class SkyRendererMixin
{
	@ModifyExpressionValue(
			//#if MC >= 1.20.6
			method = "renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
			//#elseif MC >= 1.19.4
			//$$ method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
			//#elseif MC >= 1.18.2
			//$$ method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/math/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
			//#elseif MC >= 1.17.1
			//$$ method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/math/Matrix4f;FLjava/lang/Runnable;)V",
			//#elseif MC >= 1.16.5
			//$$ method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;F)V",
			//#elseif MC >= 1.15.2
			//$$ method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;F)V",
			//#else
			//$$ method = "renderSky(F)V",
			//#endif
			at = @At(
					value = "INVOKE",
					//#if MC >= 1.17.1
					target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;getHorizonHeight(Lnet/minecraft/world/level/LevelHeightAccessor;)D"
					//#elseif MC >= 1.16.5
					//$$ target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;getHorizonHeight()D"
					//#elseif MC >= 1.15.2
					//$$ target = "Lnet/minecraft/client/multiplayer/ClientLevel;getHorizonHeight()D"
					//#else
					//$$ target = "Lnet/minecraft/client/multiplayer/MultiPlayerLevel;getHorizonHeight()D"
					//#endif
			)
	)
	private double useQuadrantHorizonHeight(double original, @Local(argsOnly = true) float deltaPartialTick)
	{
		Vec3 eyePosition = Minecraft.getInstance().player.getEyePosition(deltaPartialTick);
		return ClientSyncQuery.getHorizonHeight(Minecraft.getInstance().level, original, eyePosition.x, eyePosition.z);
	}
}
