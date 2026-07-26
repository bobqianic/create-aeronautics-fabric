package dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.eriksonn.aeronautics.index.AeroPartialModels;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.compat.create.RenderBridge;
import dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer;
import dev.simulated_team.simulated.content.physics_staff.OptionalShaderMods;
import dev.simulated_team.simulated.util.SimColors;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import dev.eriksonn.aeronautics.index.client.AeroRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class HotAirBurnerRenderer extends SmartBlockEntityRenderer<HotAirBurnerBlockEntity> {
    private static final float FLAME_ANIMATION_PERIOD = 10.0F / 3.0F;
    private static final float FLAME_SIZE = 2.0F;
    private static final float FLAME_QUAD_Y = 4.0F / 16.0F;

    public HotAirBurnerRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final HotAirBurnerBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        final float signalStrength = Math.max(0, be.getSignalStrength() / 15F);
        final SuperByteBuffer indicator = CachedBuffers.partial(AeroPartialModels.HOT_AIR_BURNER_INDICATOR, be.getBlockState());
        final VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());
        indicator.light(light)
                .color(SimColors.redstone(signalStrength))
                .renderInto(ms.last(), vb);

        if (signalStrength <= 0.0) {
            return;
        }

        ms.pushPose();
        // The shader trims the bottom edge of the quad. Starting one model
        // pixel below the burner plate keeps the visible flame rooted on it.
        ms.translate(-0.5, FLAME_QUAD_Y, 0.5);

        final BlockPos pos = be.getBlockPos();
        final Vec3 center = pos.getCenter();
        final Minecraft minecraft = Minecraft.getInstance();
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        if (be.getLevel() instanceof PonderLevel && minecraft.getCameraEntity() != null) {
            camera = minecraft.getCameraEntity().getPosition(partialTicks);
        }

        final SubLevel subLevel = Sable.HELPER.getContaining(be);
        if (subLevel != null) {
            camera = subLevel.logicalPose().transformPositionInverse(camera);
        }

        final float angle = (float) Math.atan2(
                camera.z() - center.z(),
                camera.x() - center.x()
        );
        final HotAirBurnerBlock.Variant variant =
                be.getBlockState().getValue(HotAirBurnerBlock.VARIANT);
        final float palette =
                variant == HotAirBurnerBlock.Variant.FIRE ? 0.25F : 0.75F;
        final float flameRenderTime =
                (float) Mth.lerp(partialTicks, be.lastRenderTime, be.renderTime)
                        + be.getTimeOffset();
        final float animationPhase = positiveModulo(
                flameRenderTime,
                FLAME_ANIMATION_PERIOD
        ) / FLAME_ANIMATION_PERIOD;

        ms.translate(1.0F, 0.0F, 0.0F);
        ms.mulPose(Axis.YP.rotation((float) (-angle + Math.PI * 0.5F)));
        ms.translate(-1.0F, 0.0F, 0.0F);
        renderFlameOrEnqueue(
                ms.last().pose(),
                buffer,
                animationPhase,
                be.getFlameIntensity(partialTicks),
                palette
        );
        ms.popPose();

        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
    }

    private static float positiveModulo(final float value, final float modulus) {
        final float result = value % modulus;
        return result < 0.0F ? result + modulus : result;
    }

    private static void renderFlameOrEnqueue(
            final Matrix4f pose,
            final MultiBufferSource buffer,
            final float animationPhase,
            final float intensity,
            final float palette
    ) {
        if (OptionalShaderMods.isShaderPackActive()
                && IrisBurnerFlameRenderQueue.isCollectingWorldFrame()) {
            final RenderType irisRenderType =
                    AeroRenderTypes.irisCompositeBurnerFlame();
            if (RenderBridge.isDiscoveringLayers()) {
                buffer.getBuffer(irisRenderType);
                return;
            }
            if (RenderBridge.isReplayingLayer()) {
                if (RenderBridge.isReplayingLayer(irisRenderType)) {
                    IrisBurnerFlameRenderQueue.enqueue(
                            pose,
                            animationPhase,
                            intensity,
                            palette
                    );
                }
                return;
            }
            if (IrisBurnerFlameRenderQueue.enqueue(
                    pose,
                    animationPhase,
                    intensity,
                    palette
            )) {
                return;
            }
        }

        renderFlame(
                pose,
                buffer.getBuffer(AeroRenderTypes.burnerFlame()),
                animationPhase,
                intensity,
                palette
        );
    }

    static void renderFlame(
            final Matrix4f pose,
            final VertexConsumer consumer,
            final float animationPhase,
            final float intensity,
            final float palette
    ) {
        // Keep each queued quad self-contained. Ponder and Sable can replay
        // buffered vertices, but cannot safely replay per-draw shader uniforms.
        addFlameVertex(
                consumer,
                pose,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                1.0F,
                animationPhase,
                intensity,
                palette
        );
        addFlameVertex(
                consumer,
                pose,
                FLAME_SIZE,
                0.0F,
                0.0F,
                1.0F,
                1.0F,
                animationPhase,
                intensity,
                palette
        );
        addFlameVertex(
                consumer,
                pose,
                FLAME_SIZE,
                FLAME_SIZE,
                0.0F,
                1.0F,
                0.0F,
                animationPhase,
                intensity,
                palette
        );
        addFlameVertex(
                consumer,
                pose,
                0.0F,
                FLAME_SIZE,
                0.0F,
                0.0F,
                0.0F,
                animationPhase,
                intensity,
                palette
        );
    }

    private static void addFlameVertex(
            final VertexConsumer consumer,
            final Matrix4f pose,
            final float x,
            final float y,
            final float z,
            final float u,
            final float v,
            final float animationPhase,
            final float intensity,
            final float palette
    ) {
        consumer.addVertex(pose, x, y, z)
                .setUv(u, v)
                .setColor(animationPhase, intensity, palette, 1.0F);
    }
}
