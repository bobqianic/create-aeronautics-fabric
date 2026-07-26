package dev.simulated_team.simulated.mixin_interface;

import net.minecraft.client.renderer.texture.SpriteContents;

public interface SpriteContentsExtension {
    SpriteContents.Ticker simulated$getTicker();
    void simulated$setTicker(SpriteContents.Ticker ticker);
}
