package dev.eriksonn.aeronautics.mixin.levitite;

import dev.eriksonn.aeronautics.content.blocks.levitite.LevititeShaderManager;
import com.mojang.blaze3d.platform.Window;
import com.zurrtum.create.client.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PonderUI.class)
public class PonderUIMixin {
    @Inject(method = "renderScene",at = @At("HEAD"))
    protected void renderScene(GuiGraphics graphics, int id, Window window, int index, float partialTicks,
                               float uiTicks, CallbackInfo ci) {
        LevititeShaderManager.disableShader();
    }
}
