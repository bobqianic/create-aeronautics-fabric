package dev.simulated_team.simulated.compat.create;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class SafeBlockEntityRenderer<T extends net.minecraft.world.level.block.entity.BlockEntity>
        extends SmartBlockEntityRenderer<T> {
    public SafeBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }
}
