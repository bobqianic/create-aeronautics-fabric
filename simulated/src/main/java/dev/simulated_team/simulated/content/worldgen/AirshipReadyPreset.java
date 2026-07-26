package dev.simulated_team.simulated.content.worldgen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;

public class AirshipReadyPreset extends SimulatedWorldPreset {
	public AirshipReadyPreset(final ResourceLocation id, @Nullable final Component description) {
		super(id, description);
	}

	@Override
	public void modifyGameRules(final GameRules gameRules) {
		gameRules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
		gameRules.getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(false, null);
		gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
		gameRules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
	}
}
