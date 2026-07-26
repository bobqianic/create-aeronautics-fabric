package dev.simulated_team.simulated.content.blocks.throttle_lever;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.index.SimBlocks;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

public class ThrottleLeverClientGripHandler {

    private static final PoseStack stack = new PoseStack();
    private static final Set<ThrottleLeverBlockEntity> nearbyThrottleLevers = new ObjectOpenHashSet<>();

    public static void tickGrip(final ThrottleLeverBlockEntity blockEntity) {
        if (isInvalid(blockEntity)) return;

        nearbyThrottleLevers.add(blockEntity);
    }

    private static boolean isInvalid(final ThrottleLeverBlockEntity blockEntity) {
        if (blockEntity.isRemoved()) {
            return true;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;

        if (player == null) {
            return true;
        }

        final double reach = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue() + 2.0;

        final BlockPos blockPos = blockEntity.getBlockPos();

        return player.distanceToSqr(blockPos.getCenter()) > reach * reach;
    }

    public static void clearNearbyThrottleLevers() {
        nearbyThrottleLevers.removeIf(ThrottleLeverClientGripHandler::isInvalid);
    }

    public static Collection<ThrottleLeverBlockEntity> getNearbyThrottleLevers() {
        return nearbyThrottleLevers;
    }

    public static Double raycastLever(final Vec3 eyePosMoj, final Vec3 viewVectorMoj, final ThrottleLeverBlockEntity lever, final float partialTicks) {
        final LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;

        final BlockPos leverPos = lever.getBlockPos();

        final Vector3d eyePos = JOMLConversion.toJOML(eyePosMoj);
        final Vector3d viewVector = JOMLConversion.toJOML(viewVectorMoj);

        final ClientSubLevel subLevel = Sable.HELPER.getContainingClient(lever);
        if (subLevel != null) {
            final Pose3dc pose = subLevel.renderPose(partialTicks);

            pose.transformPositionInverse(eyePos);
            pose.transformNormalInverse(viewVector);
        }

        stack.pushPose();
        stack.translate(leverPos.getX() - eyePos.x, leverPos.getY() - eyePos.y, leverPos.getZ() - eyePos.z);
        ThrottleLeverRenderer.transformHandleExternal(lever, partialTicks, stack);

        final Matrix4f pose = stack.last().pose();
        pose.invert();
        stack.popPose();

        final Vector3f localViewPosition = pose.transformPosition(new Vector3f());
        final Vector3f localViewDirection = pose.transformDirection(new Vector3f((float) viewVector.x, (float) viewVector.y, (float) viewVector.z));

        final VoxelShape leverShape = SimBlocks.THROTTLE_LEVER.get().getHandleShape(SimBlocks.THROTTLE_LEVER.getDefaultState());

        eyePos.set(localViewPosition);
        viewVector.set(localViewDirection).mul(player.blockInteractionRange()).add(eyePos);

        final BlockHitResult hitResult = leverShape.clip(JOMLConversion.toMojang(eyePos), JOMLConversion.toMojang(viewVector), BlockPos.ZERO);

        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
            return null;
        }

        final Vec3 location = hitResult.getLocation();
        return eyePos.distanceSquared(location.x, location.y, location.z);
    }
}
