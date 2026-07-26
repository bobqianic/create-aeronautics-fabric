package dev.eriksonn.aeronautics.index.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.eriksonn.aeronautics.Aeronautics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class AeroRenderTypes {
    public static final ResourceLocation LEVITITE_SHADER = Aeronautics.path("levitite/levitite");
    private static final ResourceLocation FIRE_PALETTE =
            Aeronautics.path("textures/effects/fire_palette.png");

    private static final RenderPipeline BURNER_FLAME_PIPELINE =
            burnerFlamePipeline("burner_flame");

    // Iris composites its shader-pack targets after normal block-entity
    // rendering. Drawing a custom core shader into those targets can make the
    // otherwise opaque flame inherit the pack's transparency. This copy is
    // used only after Iris has completed that composite.
    private static final RenderPipeline BURNER_FLAME_IRIS_COMPOSITE_PIPELINE =
            burnerFlamePipeline("burner_flame_iris_composite");

    private static final RenderType BURNER_FLAME = RenderType.create(
            "aeronautics_burner_flame",
            RenderType.TRANSIENT_BUFFER_SIZE,
            BURNER_FLAME_PIPELINE,
            burnerFlameState()
    );

    private static final RenderType BURNER_FLAME_IRIS_COMPOSITE = RenderType.create(
            "aeronautics_burner_flame_iris_composite",
            RenderType.TRANSIENT_BUFFER_SIZE,
            BURNER_FLAME_IRIS_COMPOSITE_PIPELINE,
            burnerFlameState()
    );

    private AeroRenderTypes() {
    }

    private static RenderPipeline burnerFlamePipeline(final String name) {
        return RenderPipelines.register(RenderPipeline.builder()
                .withLocation(Aeronautics.path("pipeline/" + name))
                .withVertexShader(Aeronautics.path("core/burner_flame"))
                .withFragmentShader(Aeronautics.path("core/burner_flame"))
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withUniform("Fog", UniformType.UNIFORM_BUFFER)
                .withSampler("Sampler0")
                .withCull(false)
                .withVertexFormat(
                        DefaultVertexFormat.POSITION_TEX_COLOR,
                        VertexFormat.Mode.QUADS
                )
                .build());
    }

    private static RenderType.CompositeState burnerFlameState() {
        return RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(
                        FIRE_PALETTE,
                        false
                ))
                .createCompositeState(false);
    }

    public static void init() {
        // Force pipeline registration before Minecraft's first shader reload.
    }

    public static RenderType burnerFlame() {
        return BURNER_FLAME;
    }

    public static RenderType irisCompositeBurnerFlame() {
        return BURNER_FLAME_IRIS_COMPOSITE;
    }

    public static RenderType levitite() {
        return RenderType.translucentMovingBlock();
    }

    public static RenderType levititeGhosts() {
        return RenderType.translucentMovingBlock();
    }
}
