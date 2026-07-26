package dev.simulated_team.simulated.mixin_interface;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.end.EndDragonFight;

public interface PrimaryLevelDataExtension {
	ResourceLocation getPreset();
	void setPreset(ResourceLocation resourceLocation);
	void setEndDragonFight(EndDragonFight.Data endDragonFight);
}
