package dev.simulated_team.simulated.content.physics_staff;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.foundation.item.render.CustomRenderedItemModel;
import com.zurrtum.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.zurrtum.create.foundation.item.render.PartialItemModelRenderer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.SimulatedClient;
import dev.simulated_team.simulated.client.render.FirstPersonItemFocus;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.index.SimRenderTypes;
import dev.simulated_team.simulated.util.SimDistUtil;
import dev.simulated_team.simulated.util.SimMathUtils;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import java.lang.Math;
import java.util.UUID;

public class PhysicsStaffItemRenderer extends CustomRenderedItemModelRenderer {
    private static final FirstPersonItemFocus FIRST_PERSON_FOCUS = new FirstPersonItemFocus();

    static void captureFirstPersonFocus(final PoseStack ms, final Minecraft minecraft, final float partialTicks) {
        FIRST_PERSON_FOCUS.captureProjected(ms, minecraft, partialTicks);
    }

    static void captureFirstPersonBodyFocus(final PoseStack ms) {
        FIRST_PERSON_FOCUS.captureCameraRelativeWorld(ms);
    }

    public static Vec3 getFirstPersonFocusPos(final float pt) {
        return FIRST_PERSON_FOCUS.resolveCameraRelative(pt, false);
    }

    public static boolean isFirstPersonFocusWorldRelative() {
        return FIRST_PERSON_FOCUS.isCameraRelativeWorldSpace();
    }

    @Override
    protected void render(final ItemStack stack, final CustomRenderedItemModel model, final PartialItemModelRenderer renderer, final ItemDisplayContext context, final PoseStack ms,
                          final MultiBufferSource buffer, final int light, final int overlay) {
        float openAmount = 0;
        float cubeScale = 0;
        final PhysicsStaffClientHandler clientHandler = SimulatedClient.PHYSICS_STAFF_CLIENT_HANDLER;
        final Minecraft minecraft = Minecraft.getInstance();

        final float partialTicks = AnimationTickHolder.getPartialTicks();

        // only staffs held in a players hand should attempt to render opened
        final Player player = SimDistUtil.getClientPlayer();

        if (player != null && (context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {
            if (player.getMainHandItem() == stack || player.getOffhandItem() == stack) {
                // smooth lerping for your own staff you're holding
                openAmount = Mth.lerp(partialTicks, clientHandler.previousExtension, clientHandler.extension);
                cubeScale = Mth.lerp(partialTicks, clientHandler.previousCubeScale, clientHandler.cubeScale);
            } else {
                // look up existing beams for their associated player
                for (final UUID playerUUID : clientHandler.beams.keySet()) {
                    final Player otherPlayer = minecraft.level.getPlayerByUUID(playerUUID);
                    if (otherPlayer != null && (otherPlayer.getMainHandItem() == stack || otherPlayer.getOffhandItem() == stack)) {
                        openAmount = Mth.lerp(partialTicks, clientHandler.beams.get(playerUUID).previousExtension, clientHandler.beams.get(playerUUID).extension);
                        cubeScale = Mth.lerp(partialTicks, clientHandler.beams.get(playerUUID).previousCubeScale, clientHandler.beams.get(playerUUID).cubeScale);
                        break;
                    }
                }
            }
        }

        final float tiltAmount = Mth.lerp(partialTicks, clientHandler.previousTilt, clientHandler.tilt);
        final Quaternionf utilQuat = new Quaternionf();

        boolean shadersActive = OptionalShaderMods.isShaderPackActive();

        if (context.firstPerson()) {
            if (clientHandler.getDragSession() != null) {
                final PhysicsStaffClientHandler.ClientDragSession dragSession = clientHandler.getDragSession();

                final Quaternionf rotation = minecraft.gameRenderer.getMainCamera().rotation();
                final Vector3d globalAnchor = ((ClientSubLevel) dragSession.dragSubLevel()).renderPose().transformPosition(new Vector3d(dragSession.dragLocalAnchor()));
                final Vector3d dirToAnchor = globalAnchor.sub(JOMLConversion.toJOML(player.getEyePosition(partialTicks))).normalize();
                rotation.transformInverse(dirToAnchor);

                final Quaternionf quat = SimMathUtils.getQuaternionfFromVectorRotation(new Vector3d(0.0, 0.0, -1.0), dirToAnchor);
                ms.mulPose(utilQuat.identity().rotateY(-Mth.HALF_PI));
                ms.mulPose(quat.slerp(utilQuat.identity(), 0.6f));
                ms.mulPose(utilQuat.identity().rotateY(Mth.HALF_PI));
            }
            final float tiltMultiplier = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -1.0f : 1.0f;

            ms.mulPose(utilQuat.identity().rotateZ((float) Math.toRadians((tiltAmount * 0.5 + 0.5) * -61.0f) * tiltMultiplier));
        }


        renderer.render(model.getOriginalModel(), Sheets.cutoutBlockSheet(), light);

        renderer.render(SimPartialModels.PHYSICS_STAFF_CORE.get(), SimRenderTypes.itemGlowingSolid(shadersActive), LightTexture.FULL_BRIGHT);
        renderer.render(SimPartialModels.PHYSICS_STAFF_CORE_GLOW.get(), SimRenderTypes.itemGlowingTranslucent(shadersActive), LightTexture.FULL_BRIGHT);
        final float worldTime = AnimationTickHolder.getRenderTime() / 20;

        ms.pushPose();
        ms.translate(0, 6.5 / 16.0, 0);
        renderer.render(SimPartialModels.PHYSICS_STAFF_RING.get(), Sheets.cutoutBlockSheet(), light);
        ms.popPose();

        ms.translate(0, 9 / 16.0, 0);
        for (int i = 0; i < 2; i++) {
            ms.pushPose();
            ms.mulPose(Axis.YP.rotationDegrees(i * 180));
            ms.translate(-3 / 16.0, 0, 0);
            ms.mulPose(Axis.ZP.rotationDegrees(openAmount * 20));
            renderer.render(SimPartialModels.PHYSICS_STAFF_SIGMA.get(), Sheets.cutoutBlockSheet(), light);
            ms.popPose();
        }
        ms.translate(0, 6 / 16.0, 0);

        if (context.firstPerson()) {
            if (clientHandler.getDragSession() != null) {
                clientHandler.lastCubeOrientation.set(clientHandler.getDragSession().dragOrientation());
            }

            final Matrix4f m = new Matrix4f(ms.last().pose());
            m.m30(0).m31(0).m32(0);
            m.invert();
            m.rotate(clientHandler.lastCubeOrientation);
            ms.mulPose(m);
        }

        cubeScale = Mth.lerp(cubeScale, -0.05f, 1f);
        cubeScale = Mth.clamp(cubeScale, 0, 1);
        cubeScale *= 0.8f;
        ms.scale(cubeScale, cubeScale, cubeScale);
        renderer.render(SimPartialModels.PHYSICS_STAFF_INNER_CUBE.get(), SimRenderTypes.itemGlowingSolid(shadersActive), LightTexture.FULL_BRIGHT);

        if (context.firstPerson()) {
            captureFirstPersonFocus(ms, minecraft, partialTicks);
        }

        ms.scale(1.2f, 1.2f, 1.2f);
        renderer.render(SimPartialModels.PHYSICS_STAFF_OUTER_CUBE.get(), SimRenderTypes.itemGlowingTranslucent(shadersActive), LightTexture.FULL_BRIGHT);

        // Iris doesn't allow individual render types to be ended, so all batches must be ended for the translucent parts to draw correctly
        if (OptionalShaderMods.isIrisLoaded() && !shadersActive) {
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
        }
    }
}
