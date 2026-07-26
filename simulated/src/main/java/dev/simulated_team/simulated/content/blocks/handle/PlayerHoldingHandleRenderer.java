package dev.simulated_team.simulated.content.blocks.handle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerHoldingHandleRenderer {
    private static final Set<UUID> holdingPlayers = new HashSet<>();

    public static void updatePlayerList(final Collection<UUID> uuids) {
        holdingPlayers.clear();
        holdingPlayers.addAll(uuids);
    }

    public static void afterSetupAnim(final Player player, final HumanoidModel<?> model) {
        if (holdingPlayers.contains(player.getUUID()))
            setHangingPose(model);
    }

    private static void setHangingPose(final HumanoidModel<?> model) {
        if (Minecraft.getInstance().isPaused()) {
            return;
        }
        model.leftArm.zRot = 0.0f;
        model.leftArm.zRot = 0.0f;

        model.leftArm.xRot = (float) Math.toRadians(-80.0f)+ model.head.xRot;
        model.rightArm.xRot = (float) Math.toRadians(-80.0f)+ model.head.xRot;

        model.rightArm.yRot = (float) Math.toRadians(-15);
        model.leftArm.yRot = (float) Math.toRadians(15);
    }
}
