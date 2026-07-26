package dev.simulated_team.simulated.index;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.foundation.render.RenderTypes;
import dev.simulated_team.simulated.Simulated;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;

public final class SimRenderTypes {

    // Split translucent laser composition into two commutative operations.
    // This keeps intersecting colors stable when camera-based quad order flips.
    private static final BlendFunction LASER_ATTENUATION_BLEND = new BlendFunction(
            SourceFactor.ZERO, DestFactor.ONE_MINUS_SRC_ALPHA,
            SourceFactor.ZERO, DestFactor.ONE);
    private static final BlendFunction LASER_EMISSION_BLEND = new BlendFunction(
            SourceFactor.SRC_ALPHA, DestFactor.ONE,
            SourceFactor.ZERO, DestFactor.ONE);

    // Native 1.21 core-shader port of the original Veil laser. The vertex format,
    // blend, cull, depth-write and primitive mode intentionally match upstream.
    private static final RenderPipeline LASER_PIPELINE = nativeLaserPipeline("laser", true);

    // Fabric draws this variant after translucent terrain. It still depth-tests
    // against the world, but must not put the near-camera wall into the depth
    // buffer before particles and clouds are rendered.
    private static final RenderPipeline LASER_LATE_ATTENUATION_PIPELINE =
            mixedLaserPipeline("laser_late_attenuation", LASER_ATTENUATION_BLEND);
    private static final RenderPipeline LASER_LATE_PIPELINE =
            mixedLaserPipeline("laser_late", LASER_EMISSION_BLEND);

    // A distinct copy of the proven late native pipeline for Iris. It is drawn
    // only after Iris finalizes its world composite, so Iris never substitutes a
    // shader-pack program for the laser core shader.
    private static final RenderPipeline LASER_IRIS_COMPOSITE_ATTENUATION_PIPELINE =
            mixedLaserPipeline("laser_iris_composite_attenuation", LASER_ATTENUATION_BLEND);
    private static final RenderPipeline LASER_IRIS_COMPOSITE_PIPELINE =
            mixedLaserPipeline("laser_iris_composite", LASER_EMISSION_BLEND);

    // A deliberately faint additive pass around the post-composite Iris beam.
    // Keeping it separate prevents the glow blend from changing the proven core.
    private static final RenderPipeline LASER_IRIS_GLOW_PIPELINE =
            irisLaserGlowPipeline("laser_iris_glow");

    private static final RenderPipeline BLOCK_TRANSLUCENT_PIPELINE = terrainPipeline(
            "block_translucent", DefaultVertexFormat.BLOCK, true, true);

    private static final RenderPipeline LOCK_PIPELINE = RenderPipeline.builder()
            .withLocation(Simulated.path("pipeline/lock"))
            .withVertexShader("core/rendertype_text_see_through")
            .withFragmentShader("core/rendertype_text_see_through")
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withSampler("Sampler0")
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withDepthWrite(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS)
            .build();

    // Reuse the vanilla shader-compatible lightning pipeline, but draw to the main target.
    // RenderType.lightning() writes to the weather target, which has already been composited
    // by the time shader-pack-safe staff overlays are drawn.
    private static final RenderType STAFF_OVERLAY = create(
            "simulated_staff_overlay", RenderPipelines.LIGHTNING, compositeState(null, false, false));
    private static final RenderType STAFF_SELECTION_EDGE = create(
            "simulated_staff_selection_edge", RenderPipelines.LIGHTNING,
            compositeState(null, false, false));
    private static final RenderType STAFF_SELECTION_FACE = create(
            "simulated_staff_selection_face", RenderPipelines.ITEM_ENTITY_TRANSLUCENT_CULL,
            RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            AllSpecialTextures.CHECKERED.getLocation(), false))
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .createCompositeState(false));
    private static final RenderType LASER = createSorted(
            "simulated_laser", LASER_PIPELINE, compositeState(null, false, false));
    private static final RenderType LASER_LATE_ATTENUATION = create(
            "simulated_laser_late_attenuation", LASER_LATE_ATTENUATION_PIPELINE,
            compositeState(null, false, false));
    private static final RenderType LASER_LATE = create(
            "simulated_laser_late", LASER_LATE_PIPELINE, compositeState(null, false, false));
    private static final RenderType LASER_IRIS_COMPOSITE_ATTENUATION = create(
            "simulated_laser_iris_composite_attenuation", LASER_IRIS_COMPOSITE_ATTENUATION_PIPELINE,
            compositeState(null, false, false));
    private static final RenderType LASER_IRIS_COMPOSITE = create(
            "simulated_laser_iris_composite", LASER_IRIS_COMPOSITE_PIPELINE,
            compositeState(null, false, false));
    private static final RenderType LASER_IRIS_GLOW = create(
            "simulated_laser_iris_glow", LASER_IRIS_GLOW_PIPELINE,
            compositeState(null, false, false));
    private static final RenderType LENS = RenderType.cutout();

    private static final RenderType LOCK = create(
            "simulated_lock", LOCK_PIPELINE,
            RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            Simulated.path("textures/gui/lock.png"), false))
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .createCompositeState(false));

    // Iris cannot classify mod-defined terrain pipelines. A vanilla entity
    // pipeline keeps the standalone rope texture visible in shader passes.
    private static final RenderType ROPE = RenderType.entityCutoutNoCull(
            Simulated.path("textures/block/rope_particle.png"));

    private static final RenderType BLOCK_TRANSLUCENT = create(
            "simulated_block_translucent", BLOCK_TRANSLUCENT_PIPELINE,
            RenderType.CompositeState.builder()
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .createCompositeState(false));

    /**
     * Shader-pack-safe layer for textured placement previews.
     *
     * <p>The moving-block shader discards vertex alpha in some packs, while the
     * entity translucent shader can treat a world-space ghost as a refractive
     * entity. The translucent particle pipeline accepts the attributes emitted
     * by baked block models, preserves vertex color/alpha, and can still sample
     * the block atlas.</p>
     */
    private static final RenderType GHOST_BLOCK = create(
            "simulated_ghost_block", RenderPipelines.TRANSLUCENT_PARTICLE,
            RenderType.CompositeState.builder()
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .createCompositeState(false));

    private SimRenderTypes() {
    }

    private static RenderPipeline nativeLaserPipeline(final String name, final boolean depthWrite) {
        return RenderPipelines.register(RenderPipeline.builder()
                .withLocation(Simulated.path("pipeline/" + name))
                .withVertexShader(Simulated.path("core/laser"))
                .withFragmentShader(Simulated.path("core/laser"))
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withUniform("Fog", UniformType.UNIFORM_BUFFER)
                .withBlend(BlendFunction.TRANSLUCENT)
                .withCull(false)
                .withDepthWrite(depthWrite)
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .build());
    }

    private static RenderPipeline irisLaserGlowPipeline(final String name) {
        return RenderPipelines.register(RenderPipeline.builder()
                .withLocation(Simulated.path("pipeline/" + name))
                .withVertexShader(Simulated.path("core/laser"))
                .withFragmentShader(Simulated.path("core/laser"))
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withUniform("Fog", UniformType.UNIFORM_BUFFER)
                .withBlend(BlendFunction.LIGHTNING)
                .withCull(false)
                .withDepthWrite(false)
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .build());
    }

    private static RenderPipeline mixedLaserPipeline(final String name, final BlendFunction blend) {
        return RenderPipelines.register(RenderPipeline.builder()
                .withLocation(Simulated.path("pipeline/" + name))
                .withVertexShader(Simulated.path("core/laser"))
                .withFragmentShader(Simulated.path("core/laser"))
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withUniform("Fog", UniformType.UNIFORM_BUFFER)
                .withBlend(blend)
                .withCull(false)
                .withDepthWrite(false)
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .build());
    }

    private static RenderPipeline terrainPipeline(final String name, final VertexFormat format,
                                                  final boolean translucent, final boolean sortOnUpload) {
        final RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(Simulated.path("pipeline/" + name))
                .withVertexShader("core/terrain")
                .withFragmentShader("core/terrain")
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withUniform("Fog", UniformType.UNIFORM_BUFFER)
                .withUniform("Lighting", UniformType.UNIFORM_BUFFER)
                .withSampler("Sampler0")
                .withSampler("Sampler2")
                .withShaderDefine("ALPHA_CUTOUT", 0.1f)
                .withVertexFormat(format, VertexFormat.Mode.QUADS);
        if (translucent) {
            builder.withBlend(BlendFunction.TRANSLUCENT).withDepthWrite(false);
        }
        return builder.build();
    }

    private static RenderType.CompositeState compositeState(final ResourceLocation texture,
                                                            final boolean mipmap, final boolean lightmap) {
        final RenderType.CompositeState.CompositeStateBuilder builder = RenderType.CompositeState.builder();
        if (texture != null) {
            builder.setTextureState(new RenderStateShard.TextureStateShard(texture, mipmap));
        }
        if (lightmap) {
            builder.setLightmapState(RenderStateShard.LIGHTMAP);
        }
        return builder.createCompositeState(false);
    }

    private static RenderType create(final String name, final RenderPipeline pipeline,
                                     final RenderType.CompositeState state) {
        return RenderType.create(name, RenderType.TRANSIENT_BUFFER_SIZE, pipeline, state);
    }

    private static RenderType createSorted(final String name, final RenderPipeline pipeline,
                                           final RenderType.CompositeState state) {
        // The original Veil render type sorts its translucent quads on upload.
        return RenderType.create(name, RenderType.TRANSIENT_BUFFER_SIZE,
                false, true, pipeline, state);
    }

    public static RenderType staffOverlay() {
        return STAFF_OVERLAY;
    }

    public static RenderType staffSelectionEdge() {
        return STAFF_SELECTION_EDGE;
    }

    public static RenderType staffSelectionFace() {
        return STAFF_SELECTION_FACE;
    }

    public static RenderType laser() {
        return LASER;
    }

    public static RenderType lateLaser() {
        return LASER_LATE;
    }

    public static RenderType lateLaserAttenuation() {
        return LASER_LATE_ATTENUATION;
    }

    public static RenderType irisCompositeLaser() {
        return LASER_IRIS_COMPOSITE;
    }

    public static RenderType irisCompositeLaserAttenuation() {
        return LASER_IRIS_COMPOSITE_ATTENUATION;
    }

    public static RenderType irisCompositeLaserGlow() {
        return LASER_IRIS_GLOW;
    }

    public static RenderType lens() {
        return LENS;
    }

    public static RenderType lock() {
        return LOCK;
    }

    public static RenderType rope() {
        return ROPE;
    }

    public static RenderType blockTranslucent() {
        return BLOCK_TRANSLUCENT;
    }

    public static RenderType ghostBlock() {
        return GHOST_BLOCK;
    }

    public static RenderType itemGlowingSolid(boolean shadersActive) {
        return shadersActive ? Sheets.solidBlockSheet() : RenderTypes.itemGlowingSolid();
    }

    public static RenderType itemGlowingTranslucent(boolean shadersActive) {
        return shadersActive ? Sheets.translucentItemSheet() : RenderTypes.itemGlowingTranslucent();
    }

    public static RenderType spring(final ResourceLocation texture) {
        // Use a vanilla pipeline so Iris can map the render type into shader packs.
        // The old Veil spring shader is not available in the 1.21.10 Fabric port.
        // SpringRenderer emits a second, inward-facing copy of the mesh itself;
        // disabling culling would draw both copies on top of each other and z-fight.
        return RenderType.entityCutout(texture);
    }
}
