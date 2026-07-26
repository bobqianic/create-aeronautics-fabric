package dev.simulated_team.simulated.mixin.handle;

import dev.simulated_team.simulated.content.blocks.handle.PlayerHoldingHandleRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends HumanoidRenderState> {
    @Shadow
    @Final
    public ModelPart body;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("RETURN"))
    private void simulated$afterSetupAnim(final T renderState, final CallbackInfo callbackInfo) {
        if (!(renderState instanceof final AvatarRenderState avatarRenderState))
            return;
        if (Minecraft.getInstance().level == null)
            return;
        if (!(Minecraft.getInstance().level.getEntity(avatarRenderState.id) instanceof final Player player))
            return;

        PlayerHoldingHandleRenderer.afterSetupAnim(player, (HumanoidModel<?>) (Object) this);
    }
}
