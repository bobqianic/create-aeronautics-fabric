package dev.simulated_team.simulated.content.blocks.rope.strand.client;

import com.zurrtum.create.AllTags;
import com.zurrtum.create.client.foundation.utility.RaycastHelper;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.math.OrientedBoundingBox3d;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.index.SimTags;
import dev.simulated_team.simulated.network.packets.RopeBreakPacket;
import dev.simulated_team.simulated.network.packets.RopeRidingPacket;
import dev.simulated_team.simulated.service.SimConfigService;
import dev.simulated_team.simulated.util.SimColors;
import dev.simulated_team.simulated.util.SimMathUtils;
import dev.simulated_team.simulated.util.click_interactions.InteractCallback;
import foundry.veil.api.network.VeilPacketManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.UUID;

public class ZiplineClientManager implements InteractCallback {
    private static final double CONTINUOUS_STEP_SIZE = 0.25;
    private static final double HALF_THICKNESS = 4.0 / 16.0;
    public static UUID ridingRope = null;
    public static UUID hoveringRope = null;
    private static int groundedTimer = 0;

    public static void tick() {
        if (ridingRope != null) {
            ridingTick();
        } else {
            groundedTimer = 0;
        }

        final Minecraft mc = Minecraft.getInstance();

        if (!isRopeInteractable(mc.player.getMainHandItem())) {
            hoveringRope = null;
            return;
        }

        final double maxRange = mc.player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;
        final HitResult hitResult = mc.hitResult;

        final ClientLevelRopeManager ropeManager = ClientLevelRopeManager.getOrCreate(mc.level);
        final Vector3d from = JOMLConversion.toJOML(mc.player.getEyePosition());
        final Vector3d to = JOMLConversion.toJOML(RaycastHelper.getTraceTarget(mc.player, maxRange, JOMLConversion.toMojang(from)));
        final double bestDiffSqr = hitResult == null ? Float.MAX_VALUE : Sable.HELPER.projectOutOfSubLevel(mc.level, hitResult.getLocation()).distanceToSqr(from.x, from.y, from.z);

        hoveringRope = raycastRope(ropeManager, from, to, bestDiffSqr, HALF_THICKNESS);

        if (ridingRope == null && hoveringRope == null &&  mc.options.keyUse.isDown() && !mc.player.isShiftKeyDown()) {
            holdUseSearch(ropeManager, mc.player, from, to, bestDiffSqr);
        }
    }

    /**
     * @param bestDiffSqr initial closest point (squared)
     * @param halfThickness bounding box half-thickness
     * @return UUID of rope strand hit, or null if none
     */
    public static @Nullable UUID raycastRope(final ClientLevelRopeManager ropeManager,
                                             final Vector3dc from, final Vector3dc to, double bestDiffSqr, final double halfThickness) {
        final Vector3d localFrom = new Vector3d();
        final Vector3d localTo = new Vector3d();
        final Vector3d normal = new Vector3d();
        UUID bestHovering = null;

        for (final ClientRopeStrand strand : ropeManager.getAllStrands()) {
            final ObjectArrayList<ClientRopePoint> points = strand.getPoints();

            for (int i = 0; i < points.size() - 1; i++) {
                final ClientRopePoint point0 = points.get(i);
                final ClientRopePoint point1 = points.get(i + 1);

                point1.position().sub(point0.position(), normal).normalize();
                final AABB bounds = new AABB(-halfThickness, 0.0, -halfThickness, halfThickness, point0.position().distance(point1.position()), halfThickness);

                final Quaternionf rot = SimMathUtils.getQuaternionfFromVectorRotation(OrientedBoundingBox3d.UP, normal);

                rot.transformInverse(localFrom.set(from).sub(point0.position()));
                rot.transformInverse(localTo.set(to).sub(point0.position()));

                final Optional<Vec3> clip = bounds.clip(JOMLConversion.toMojang(localFrom), JOMLConversion.toMojang(localTo));

                if (clip.isEmpty()) {
                    continue;
                }

                final double distanceToSqr = clip.get().distanceToSqr(localFrom.x, localFrom.y, localFrom.z);

                if (distanceToSqr > bestDiffSqr)
                    continue;

                bestDiffSqr = distanceToSqr;
                bestHovering = strand.getUuid();
            }
        }

        return bestHovering;
    }

    
    /**
     * @param from Original from position. Mutates!
     * @param to   Original to position. Mutates!
     * @param bestDiffSqr initial closest point (squared)
     */
    private static void holdUseSearch(final ClientLevelRopeManager ropeManager, final Player player,
                                      final Vector3d from, final Vector3d to, final double bestDiffSqr) {
        final Vec3 oldPlayerPosition = new Vec3(player.xo, player.yo, player.zo);
        Vec3 playerMovement = player.position().subtract(oldPlayerPosition);
        final double length = Math.min(playerMovement.length(), 15.0);

        if (length > 1e-4) {
            playerMovement = playerMovement.normalize();
        }

        final Vector3d offsetFrom = new Vector3d();
        final Vector3d offsetTo = new Vector3d();
        final Vector3d offset = new Vector3d();

        for (double i = 0; i < Math.max(length, 0.01); i += CONTINUOUS_STEP_SIZE) {
            JOMLConversion.toJOML(playerMovement, offset).mul(-i);

            offsetFrom.set(from).add(offset);
            offsetTo.set(to).add(offset);

            final UUID foundStrand = raycastRope(ropeManager, offsetFrom, offsetTo, bestDiffSqr, HALF_THICKNESS);
            if (foundStrand != null) {
                final ClientRopeStrand strand = ropeManager.getStrand(foundStrand);
                if (strand == null) {
                    continue;
                }
                final ClosestQuery query = getClosestPointOnStrand(strand, player);
                if (!canStartRiding(query, player, true)) {
                    continue;
                }
                embark(foundStrand);
                player.swing(InteractionHand.MAIN_HAND);
                return;
            }
        }
    }

    private static void ridingTick() {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused())
            return;
        if (!AllTags.AllItemTags.CHAIN_RIDEABLE.matches(mc.player.getMainHandItem())) {
            disembark();
            return;
        }

        final ClientLevelRopeManager ropeHandler = ClientLevelRopeManager.getOrCreate(mc.level);
        final ClientRopeStrand strand = ropeHandler.getStrand(ridingRope);
        if (mc.player.onGround()) {
            groundedTimer++;
        } else {
            groundedTimer = 0;
        }

        if (groundedTimer > 5 || mc.player.isShiftKeyDown() || mc.player.getAbilities().flying || strand == null) {
            disembark();
            return;
        }

        final float chainYOffset = 0.5f * mc.player.getScale();
        final Vec3 playerPosition = mc.player.position()
                .add(0, mc.player.getBoundingBox()
                        .getYsize() + chainYOffset, 0);

        final ClosestQuery query = getClosestPointOnStrand(strand, playerPosition);

        final boolean isEnd = query.position().distanceSquared(strand.getPoints().getLast().position()) < 0.25;
        final boolean isStart = query.position().distanceSquared(strand.getPoints().getFirst().position()) < 0.25;

        final Vec3 mojNormal = new Vec3(query.normal.x, query.normal.y, query.normal.z);

        final double exitThreshold = 0.6;
        final Vec3 exitingMovement = mc.player.getDeltaMovement();

        if (exitingMovement.lengthSqr() > 1e-8 && ((isEnd && exitingMovement.normalize().dot(mojNormal) > exitThreshold)
                || (isStart && exitingMovement.normalize().dot(mojNormal) < -exitThreshold))) {
            disembark();
            return;
        }

        final Vec3 target = JOMLConversion.toMojang(query.position());
        final Vec3 diff = target.subtract(playerPosition);
        final Vec3 normal = JOMLConversion.toMojang(query.normal());
        final Vec3 assistanceForce = normal.scale(mc.player.getDeltaMovement().dot(normal)).scale(0.04);
        final double reach = mc.player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;

        if (diff.lengthSqr() > reach * reach) {
            disembark();
            return;
        }

        Vec3 dampingForce = mc.player.getDeltaMovement().scale(-0.6);
        dampingForce = dampingForce.subtract(normal.scale(normal.dot(dampingForce)));

        final float diffLength = diff.lengthSqr() > 0.0 ? Mth.sqrt((float) diff.length()) : 0.0f;
        mc.player.setDeltaMovement(mc.player.getDeltaMovement()
                .add(dampingForce)
                .add(assistanceForce)
                .add(diff.scale(diffLength * 0.3)));
        mc.player.fallDistance = 0.0f;

        if (AnimationTickHolder.getTicks() % 10 == 0)
            VeilPacketManager.server().sendPacket(new RopeRidingPacket(ridingRope, false));
    }

    public static boolean canStartRidingDistance(final ClosestQuery query, final Player player) {
        final double reach = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;
        return query.position.distanceSquared(JOMLConversion.toJOML(player.position())) <= reach * reach;
    }

    public static boolean canStartRidingSteepness(final ClosestQuery query, final Player player) {
        final double verticalDot = query.normal.dot(new Vector3d(0.0, 1.0, 0.0));
        return Math.abs(verticalDot) <= Math.sin(Math.toRadians(SimConfigService.INSTANCE.server().blocks.maxRopeZiplineAngle.getF()));
    }

    public static boolean canStartRiding(final ClosestQuery query, final Player player, final boolean sendMessage) {
        if (!canStartRidingDistance(query, player)) {
            if (sendMessage) {
                player.displayClientMessage(SimLang.translate("zipline.too_far").color(SimColors.NUH_UH_RED).component(), true);
            }
            return false;
        }
        if (!canStartRidingSteepness(query, player)) {
            if (sendMessage) {
                player.displayClientMessage(SimLang.translate("zipline.too_steep").color(SimColors.NUH_UH_RED).component(), true);
            }
            return false;
        }
        return true;
    }

    public static ClosestQuery getClosestPointOnStrand(final ClientRopeStrand strand, final Vec3 playerPosition) {
        final ObjectArrayList<ClientRopePoint> points = strand.getPoints();

        double minDistanceSquared = Double.MAX_VALUE;
        final Vector3d minPoint = new Vector3d();
        final Vector3d minNormal = new Vector3d();

        final Vector3d point = new Vector3d();
        final Vector3d diff = new Vector3d();
        final Vector3d normalizedDiff = new Vector3d();

        for (int i = 0; i < points.size() - 1; i++) {
            final Vector3dc pointA = points.get(i).position();
            final Vector3dc pointB = points.get(i + 1).position();

            pointB.sub(pointA, diff);
            diff.normalize(normalizedDiff);

            point.set(playerPosition.x, playerPosition.y, playerPosition.z)
                    .sub(pointA);

            double along = point.dot(normalizedDiff);
            along = Mth.clamp(along, 0, diff.length());

            point.set(pointA).fma(along, normalizedDiff);

            final double distance = point.distanceSquared(playerPosition.x, playerPosition.y, playerPosition.z);
            if (distance < minDistanceSquared) {
                minPoint.set(point);
                minNormal.set(normalizedDiff);
                minDistanceSquared = distance;
            }
        }

        return new ClosestQuery(minPoint, minNormal);
    }
    public static ClosestQuery getClosestPointOnStrand(final ClientRopeStrand strand, final Player player) {
        final float chainYOffset = 0.5f * player.getScale();
        final Vec3 playerPosition = player.position()
                .add(0, player.getBoundingBox()
                        .getYsize() + chainYOffset, 0);

        return getClosestPointOnStrand(strand, playerPosition);
    }

    public static boolean isRopeInteractable(final ItemStack stack) {
        return AllTags.AllItemTags.CHAIN_RIDEABLE.matches(stack) || stack.is(SimTags.Items.DESTROYS_ROPE);
    }

    public static void embark(final UUID rope) {
        final Minecraft mc = Minecraft.getInstance();
        final Component component = Component.translatable("mount.onboard", mc.options.keyShift.getTranslatedKeyMessage());
        mc.gui.setOverlayMessage(component, false);
        mc.getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.WOOL_HIT, 1f, 0.5f));
        ridingRope = rope;
        mc.player.getAbilities().flying = false;
        mc.player.stopFallFlying();
        VeilPacketManager.server().sendPacket(new RopeRidingPacket(ridingRope, false));
    }

    public static void disembark() {
        if (ridingRope != null)
            VeilPacketManager.server().sendPacket(new RopeRidingPacket(ridingRope, true));

        ridingRope = null;
        Minecraft.getInstance()
                .getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.WOOL_HIT, 0.75f, 0.35f));
    }

    @Override
    public Result onUse(final int modifiers, final int action, final KeyMapping rightKey) {
        if (action == GLFW.GLFW_RELEASE || hoveringRope == null || ridingRope == hoveringRope)
            return Result.empty();

        final Minecraft mc = Minecraft.getInstance();
        final ItemStack mainHandItem = mc.player.getMainHandItem();

        final boolean isWrench = AllTags.AllItemTags.CHAIN_RIDEABLE.matches(mainHandItem);
        final boolean isDestroyer = mainHandItem.is(SimTags.Items.DESTROYS_ROPE);
        if (isWrench && !mc.player.isShiftKeyDown()) {
            final ClientLevelRopeManager ropeManager = ClientLevelRopeManager.getOrCreate(mc.player.level());
            final ClientRopeStrand strand = ropeManager.getStrand(hoveringRope);

            if (strand == null) {
                return Result.empty();
            }

            final ClosestQuery query = getClosestPointOnStrand(strand, mc.player);

            if (!canStartRiding(query, mc.player, true)) {
                return Result.empty();
            }

            embark(hoveringRope);
            mc.player.swing(InteractionHand.MAIN_HAND);
            return new Result(true);
        }

        if (isDestroyer || (isWrench && mc.player.isShiftKeyDown())) {
            VeilPacketManager.server().sendPacket(new RopeBreakPacket(hoveringRope));
            mc.player.swing(InteractionHand.MAIN_HAND);
            return new Result(true);
        }

        return Result.empty();
    }

    public record ClosestQuery(Vector3d position, Vector3d normal) {
    }
}
