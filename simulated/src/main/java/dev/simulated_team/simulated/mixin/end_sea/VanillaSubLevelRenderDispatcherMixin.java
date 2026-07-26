package dev.simulated_team.simulated.mixin.end_sea;

import com.mojang.blaze3d.shaders.Uniform;
import dev.ryanhcode.sable.sublevel.render.dispatcher.VanillaSubLevelRenderDispatcher;
import dev.simulated_team.simulated.content.end_sea.EndSeaPhysics;
import dev.simulated_team.simulated.content.end_sea.EndSeaPhysicsData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VanillaSubLevelRenderDispatcher.class)
public class VanillaSubLevelRenderDispatcherMixin {

    @Inject(method = "setupDynamicEffects", at = @At("TAIL"))
    private static void setupDynamicEffects(final ShaderInstance shader, final boolean onSubLevel, final boolean upload, final CallbackInfo ci) {
        final Uniform cameraY = shader.getUniform("EndSeaCameraY");

        if (cameraY != null) {
            final Minecraft minecraft = Minecraft.getInstance();
            final EndSeaPhysics physics = EndSeaPhysicsData.of(minecraft.level);

            if (onSubLevel && physics != null) {
                final float y = (float) (minecraft.gameRenderer.getMainCamera().getPosition().y - physics.startY());

                cameraY.set(y);
                if (upload) {
                    cameraY.upload();
                }
            } else {
                cameraY.set(0f);
                if (upload) {
                    cameraY.upload();
                }
            }
        }
    }

}
