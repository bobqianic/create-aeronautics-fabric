package dev.simulated_team.simulated.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.content.kinetics.fan.EncasedFanRenderer;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HitboxesRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.List;

/**
 * Replays a 1.21 render-state submission directly into Sable's Sodium
 * sublevel buffer. This preserves renderer families that submit more than one
 * piece, such as chain conveyors, belts, depots, blaze burners, and compatible
 * third-party block entities.
 */
public final class SableCreateBlockEntityRenderer {
    private SableCreateBlockEntityRenderer() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void render(
            final BlockEntity blockEntity,
            final float partialTick,
            final PoseStack poseStack,
            final MultiBufferSource bufferSource,
            final int light,
            final int overlay
    ) {
        if (!(Sable.HELPER.getContaining(blockEntity)
                instanceof final ClientSubLevel subLevel)) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final BlockEntityRenderer renderer = minecraft
                .getBlockEntityRenderDispatcher()
                .getRenderer(blockEntity);
        if (renderer == null) {
            // Some Create block entities, including ordinary fluid pipes, only
            // have static block-model geometry and intentionally have no BER.
            return;
        }

        final Camera camera = minecraft.gameRenderer.getMainCamera();
        final Pose3dc renderPose = subLevel.renderPose(partialTick);
        final Vec3 localCameraPosition =
                renderPose.transformPositionInverse(camera.getPosition());
        final CameraRenderState cameraState = createCameraState(
                camera,
                renderPose,
                localCameraPosition
        );
        final ImmediateCollector collector =
                new ImmediateCollector(bufferSource, light, overlay);

        SableCreateRenderContext.run(() -> {
            final BlockEntityRenderState renderState =
                    (BlockEntityRenderState) renderer.createRenderState();
            renderer.extractRenderState(
                    blockEntity,
                    renderState,
                    partialTick,
                    localCameraPosition,
                    null
            );

            // Sable samples light around the transformed physical block. Keep
            // that result instead of the plot-space light extracted by vanilla.
            renderState.lightCoords = light;
            if (renderState
                    instanceof final EncasedFanRenderer.EncasedFanRenderState fanState) {
                fanState.lightBehind = light;
                fanState.lightInFront = light;
            }

            // An empty burner has no dynamic blaze geometry. Create's renderer
            // leaves a freshly allocated state empty in that case.
            if (renderState
                    instanceof final BlazeBurnerRenderer.BlazeBurnerRenderState burnerState
                    && burnerState.data == null) {
                return;
            }

            renderer.submit(renderState, poseStack, collector, cameraState);
        });
    }

    private static CameraRenderState createCameraState(
            final Camera camera,
            final Pose3dc renderPose,
            final Vec3 localCameraPosition
    ) {
        final CameraRenderState state = new CameraRenderState();
        state.pos = localCameraPosition;
        state.entityPos = localCameraPosition;
        state.blockPos = BlockPos.containing(localCameraPosition);
        state.initialized = camera.isInitialized();
        state.orientation = new Quaternionf(renderPose.orientation())
                .conjugate()
                .mul(camera.rotation());
        return state;
    }

    /**
     * Minecraft 1.21 renderers describe their output through a submission
     * collector. Sable is already inside an immediate world-buffer pass here,
     * so execute each submitted piece as it arrives.
     */
    private static final class ImmediateCollector implements SubmitNodeCollector {
        private final MultiBufferSource bufferSource;
        private final int fallbackLight;
        private final int fallbackOverlay;

        private ImmediateCollector(
                final MultiBufferSource bufferSource,
                final int fallbackLight,
                final int fallbackOverlay
        ) {
            this.bufferSource = bufferSource;
            this.fallbackLight = fallbackLight;
            this.fallbackOverlay = fallbackOverlay;
        }

        @Override
        public OrderedSubmitNodeCollector order(final int order) {
            return this;
        }

        @Override
        public void submitCustomGeometry(
                final PoseStack poseStack,
                final RenderType renderType,
                final CustomGeometryRenderer renderer
        ) {
            renderer.render(
                    poseStack.last(),
                    this.bufferSource.getBuffer(renderType)
            );
        }

        @Override
        public void submitItem(
                final PoseStack poseStack,
                final ItemDisplayContext displayContext,
                final int light,
                final int overlay,
                final int outlineColor,
                final int[] tintLayers,
                final List<BakedQuad> quads,
                final RenderType renderType,
                final FoilType foilType
        ) {
            ItemRenderer.renderItem(
                    displayContext,
                    poseStack,
                    this.bufferSource,
                    light,
                    overlay,
                    tintLayers,
                    quads,
                    renderType,
                    foilType
            );
        }

        @Override
        public <S> void submitModel(
                final Model<? super S> model,
                final S renderState,
                final PoseStack poseStack,
                final RenderType renderType,
                final int light,
                final int overlay,
                final int color,
                final TextureAtlasSprite sprite,
                final int outlineColor,
                final CrumblingOverlay crumblingOverlay
        ) {
            model.setupAnim(renderState);
            VertexConsumer consumer = this.bufferSource.getBuffer(renderType);
            if (sprite != null) {
                consumer = sprite.wrap(consumer);
            }
            model.renderToBuffer(poseStack, consumer, light, overlay, color);
        }

        @Override
        public void submitModelPart(
                final ModelPart modelPart,
                final PoseStack poseStack,
                final RenderType renderType,
                final int light,
                final int overlay,
                final TextureAtlasSprite sprite,
                final boolean renderFront,
                final boolean renderBack,
                final int outlineColor,
                final CrumblingOverlay crumblingOverlay,
                final int color
        ) {
            VertexConsumer consumer = this.bufferSource.getBuffer(renderType);
            if (sprite != null) {
                consumer = sprite.wrap(consumer);
            }
            modelPart.render(poseStack, consumer, light, overlay, color);
        }

        @Override
        public void submitBlock(
                final PoseStack poseStack,
                final BlockState blockState,
                final int light,
                final int overlay,
                final int outlineColor
        ) {
            Minecraft.getInstance()
                    .getBlockRenderer()
                    .renderSingleBlock(
                            blockState,
                            poseStack,
                            this.bufferSource,
                            light,
                            overlay
                    );
        }

        @Override
        public void submitMovingBlock(
                final PoseStack poseStack,
                final MovingBlockRenderState renderState
        ) {
            final int light = renderState.blockPos != null
                    ? LevelRenderer.getLightColor(renderState, renderState.blockPos)
                    : this.fallbackLight;
            Minecraft.getInstance()
                    .getBlockRenderer()
                    .renderSingleBlock(
                            renderState.blockState,
                            poseStack,
                            this.bufferSource,
                            light,
                            this.fallbackOverlay
                    );
        }

        @Override
        public void submitBlockModel(
                final PoseStack poseStack,
                final RenderType renderType,
                final BlockStateModel model,
                final float red,
                final float green,
                final float blue,
                final int light,
                final int overlay,
                final int outlineColor
        ) {
            ModelBlockRenderer.renderModel(
                    poseStack.last(),
                    this.bufferSource.getBuffer(renderType),
                    model,
                    red,
                    green,
                    blue,
                    light,
                    overlay
            );
        }

        @Override
        public void submitHitbox(
                final PoseStack poseStack,
                final EntityRenderState entityState,
                final HitboxesRenderState hitboxes
        ) {
        }

        @Override
        public void submitShadow(
                final PoseStack poseStack,
                final float opacity,
                final List<EntityRenderState.ShadowPiece> pieces
        ) {
        }

        @Override
        public void submitNameTag(
                final PoseStack poseStack,
                final Vec3 offset,
                final int yOffset,
                final Component text,
                final boolean discrete,
                final int light,
                final double distanceToCameraSq,
                final CameraRenderState cameraState
        ) {
        }

        @Override
        public void submitText(
                final PoseStack poseStack,
                final float x,
                final float y,
                final FormattedCharSequence text,
                final boolean dropShadow,
                final Font.DisplayMode displayMode,
                final int backgroundColor,
                final int light,
                final int color,
                final int outlineColor
        ) {
        }

        @Override
        public void submitFlame(
                final PoseStack poseStack,
                final EntityRenderState entityState,
                final Quaternionf cameraOrientation
        ) {
        }

        @Override
        public void submitLeash(
                final PoseStack poseStack,
                final EntityRenderState.LeashState leashState
        ) {
        }

        @Override
        public void submitParticleGroup(
                final ParticleGroupRenderer particleGroup
        ) {
        }
    }
}
