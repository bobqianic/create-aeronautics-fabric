package dev.ryanhcode.sable.mixin.sublevel_render;

import dev.ryanhcode.sable.mixinterface.BlockEntityRenderDispatcherExtension;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin implements BlockEntityRenderDispatcherExtension {

    @Unique
    private Vec3 sable$cameraPos;

    @ModifyArg(method = "tryExtractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;shouldRender(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/phys/Vec3;)Z"), index = 1)
    public Vec3 sable$moveCameraPosForCheck(final Vec3 pCameraPos) {
        return this.sable$cameraPos != null ? this.sable$cameraPos : pCameraPos;
    }

    @Override
    public void sable$setCameraPosition(@Nullable final Vec3 pos) {
        this.sable$cameraPos = pos;
    }
}
