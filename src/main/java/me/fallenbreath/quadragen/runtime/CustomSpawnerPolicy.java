package me.fallenbreath.quadragen.runtime;

import me.fallenbreath.quadragen.runtime.access.ServerLevelContextAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class CustomSpawnerPolicy
{
	private CustomSpawnerPolicy()
	{
	}

	public static boolean usesNoiseGeneratorAt(ServerLevel level, BlockPos pos)
	{
		LevelContext context = ((ServerLevelContextAccess)level).getLevelContext$quadragen();
		return context == null || context.getPlanAt(pos.getX(), pos.getZ()).isNoise();
	}
}
