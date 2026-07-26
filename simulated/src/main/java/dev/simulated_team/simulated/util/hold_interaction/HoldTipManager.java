package dev.simulated_team.simulated.util.hold_interaction;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueSettingsInputHandler;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import dev.simulated_team.simulated.content.blocks.behaviour.HoldTipBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Renders hold tips
 */
public class HoldTipManager {
    private static MutableComponent lastHoverTip;
    private static int hoverTicks;
    private static int hoverWarmup;

    public static void tick() {
        if (hoverWarmup > 0) {
            hoverWarmup--;
        }
        if (hoverTicks > 0) {
            hoverTicks--;
        }

        final Minecraft mc = Minecraft.getInstance();
        final HitResult target = mc.hitResult;
        if (mc.screen != null || !(target instanceof final BlockHitResult result)) {
            return;
        }

        final ClientLevel world = mc.level;
        if (world == null) {
            return;
        }

        final BlockPos pos = result.getBlockPos();

        if (!(world.getBlockEntity(pos) instanceof final SmartBlockEntity sbe)) {
            return;
        }

        for (final BlockEntityBehaviour<?> blockEntityBehaviour : sbe.getAllBehaviours()) {
            if (!(blockEntityBehaviour instanceof final HoldTipBehaviour behaviour))
                continue;
            final MutableComponent hoverTip = behaviour.getHoverTip(mc.player, pos, sbe.getBlockState());
            if (hoverTip != null) {
                showHoverTip(hoverTip);
                return;
            }
        }
    }

    private static void showHoverTip(final MutableComponent hoverTip) {
        // Match Create's value-setting hover prompt timing. The assembler used
        // to delegate to that renderer, but it is no longer guaranteed to run
        // for a custom hold interaction in the 1.21.10 HUD pipeline.
        if (hoverWarmup < 6) {
            hoverWarmup += 2;
            return;
        }

        hoverWarmup++;
        hoverTicks = hoverTicks == 0 ? 11 : Math.max(hoverTicks, 6);
        lastHoverTip = hoverTip;
    }

    public static void renderOverlay(final GuiGraphics graphics) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || !ValueSettingsInputHandler.canInteract(mc.player)) {
            return;
        }
        if (hoverTicks == 0 || lastHoverTip == null) {
            return;
        }

        final float alpha = hoverTicks > 5
                ? (11 - hoverTicks) / 5.0f
                : Math.min(1.0f, hoverTicks / 5.0f);
        final Color color = new Color(0xffffff);
        color.setAlpha(alpha);

        final int x = graphics.guiWidth() / 2;
        // Preserve the blank title-line spacing used by the reference UI.
        final int y = graphics.guiHeight() - 75 - 12;
        graphics.drawString(
                mc.font,
                lastHoverTip,
                x - mc.font.width(lastHoverTip) / 2,
                y,
                color.getRGB(),
                true
        );
    }
}
