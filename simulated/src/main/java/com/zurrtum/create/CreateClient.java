package com.zurrtum.create;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

public final class CreateClient {
    public static final ZapperRenderHandler ZAPPER_RENDER_HANDLER = new ZapperRenderHandler();
    private CreateClient() {
    }

    public static final class ZapperRenderHandler {
        public void shoot(InteractionHand hand, Vec3 position) {
        }
    }
}
