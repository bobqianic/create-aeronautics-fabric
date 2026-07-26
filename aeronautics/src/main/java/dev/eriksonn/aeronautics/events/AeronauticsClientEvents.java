package dev.eriksonn.aeronautics.events;

import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.map.BalloonMap;
import dev.eriksonn.aeronautics.content.blocks.levitite.LevititeShaderManager;
import dev.eriksonn.aeronautics.util.AeroSoundDistUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class AeronauticsClientEvents {

    public static void clientLevelTick(final boolean post) {
        if (post) {
            final ClientLevel level = Minecraft.getInstance().level;

            AeroSoundDistUtil.tickGlobalBurnerSound();
            LevititeShaderManager.tick();

            if (level != null) {
                BalloonMap.tick(level);
            }
        }
    }

}
