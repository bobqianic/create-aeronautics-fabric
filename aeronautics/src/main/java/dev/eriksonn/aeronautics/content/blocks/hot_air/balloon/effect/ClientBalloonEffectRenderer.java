package dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.effect;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.content.blocks.hot_air.BlockEntityLiftingGasProvider;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.map.BalloonMap;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.ClientBalloon;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.post.PostProcessingManager;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.client.render.shader.uniform.ShaderUniformAccess;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class ClientBalloonEffectRenderer {

    private static final ResourceLocation FBO_ID = Aeronautics.path("soft_light");
    private static final ResourceLocation POST_SHADER_ID = Aeronautics.path("soft_light");

    private static final ResourceLocation SIDE_TEXTURE = Aeronautics.path("textures/special/heat_overlay.png");
    private static final ResourceLocation TOP_TEXTURE = Aeronautics.path("textures/special/lava_still.png");

    private static final ResourceLocation SHADER_ID = Aeronautics.path("hot_air_overlay");

    @Nullable
    private static AdvancedFbo overlayFbo;

    public static void onRenderLevelStage(final VeilRenderLevelStageEvent.Stage stage,
                                          final Matrix4fc frustumMatrix,
                                          final Matrix4fc projectionMatrix,
                                          final int renderTick) {
        freeFbo();
    }

    private static void freeFbo() {
        if (overlayFbo != null) {
            overlayFbo.free();
        }

        overlayFbo = null;
    }
}
