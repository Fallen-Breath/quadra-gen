package me.fallenbreath.quadragen.mixins.spawning;

import me.fallenbreath.quadragen.compat.DummyClass;
import org.spongepowered.asm.mixin.Mixin;

/**
 * mc >= 1.16.5: main project  <--------
 * mc <= 1.15.2: subproject 1.15.2
 * <p>
 * Generator-owned custom spawners no longer require quadrant compensation after 1.15.2.
 */
@Mixin(DummyClass.class)
public abstract class PatrolSpawnerMixin
{
}
