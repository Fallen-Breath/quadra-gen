package me.fallenbreath.quadragen.mixins.spawning;

import me.fallenbreath.quadragen.compat.DummyClass;
import org.spongepowered.asm.mixin.Mixin;

/**
 * mc >= 1.16.5: main project  <--------
 * mc <= 1.15.2: subproject 1.15.2
 * <p>
 * Worldgen mob spawning is owned by a separate Overworld generator in 1.15.2.
 */
@Mixin(DummyClass.class)
public abstract class OverworldLevelSourceMixin
{
}
