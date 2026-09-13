/*
 * This file is part of the Quadra Gen project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Fallen_Breath and contributors
 */

package me.fallenbreath.quadragen.mixins.client.level;

import me.fallenbreath.quadragen.compat.DummyClass;
import org.spongepowered.asm.mixin.Mixin;

/**
 * mc >= 1.21.5: subproject 1.21.5
 * mc <= 1.21.4: subproject 1.21.4                    <--------
 * <p>
 * 1.21.4 has no {@code ClientLevel#getPrecipitationAt(BlockPos)}; precipitation uses {@code Level#isRainingAt}.
 */
@Mixin(DummyClass.class)
public abstract class ClientLevelMixin
{
}
