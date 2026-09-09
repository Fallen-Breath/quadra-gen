package me.fallenbreath.quadragen.mixins.worldgen;

import me.fallenbreath.quadragen.compat.DummyClass;
import org.spongepowered.asm.mixin.Mixin;

/**
 * mc >= 1.16.5: main project                         <--------
 * mc <= 1.15.2: subproject 1.15.2
 * <p>
 * The legacy implementation filters its precomputed stronghold positions.
 */
@Mixin(DummyClass.class)
public abstract class StrongholdFeatureMixin
{
}
