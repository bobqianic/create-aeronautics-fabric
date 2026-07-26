package dev.simulated_team.simulated.events;

import dev.ryanhcode.sable.util.SableDistUtil;
import dev.simulated_team.simulated.SimulatedClient;
import dev.simulated_team.simulated.client.BlockPropertiesTooltip;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterInteractionHandler;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ZiplineClientManager;
import dev.simulated_team.simulated.content.blocks.throttle_lever.ThrottleLeverClientGripHandler;
import dev.simulated_team.simulated.content.end_sea.EndSeaRenderer;
import dev.simulated_team.simulated.content.items.rope.RopeItem.ClientRopeItemHandler;
import dev.simulated_team.simulated.index.SimClickInteractions;
import dev.simulated_team.simulated.index.SimKeys;
import dev.simulated_team.simulated.service.SimConfigService;
import dev.simulated_team.simulated.util.SimDistUtil;
import dev.simulated_team.simulated.util.click_interactions.InteractCallback;
import dev.simulated_team.simulated.util.click_interactions.InteractCallback.Result;
import dev.simulated_team.simulated.util.hold_interaction.HoldInteractionManager;
import dev.simulated_team.simulated.util.hold_interaction.HoldTipManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimulatedCommonClientEvents {

    /**
     * Called from loader specific implementation of events. Used to call separate onInput inside of client handlers. Can not be cancelled.
     *
     * @param button    The button being pressed. Use {@link org.lwjgl.glfw.GLFW#GLFW_KEY_SPACE GLFW} keys to determine what button is being pressed
     * @param modifiers The modifier key that is being applied to this action.
     * @param action    The action of this key. Use {@link org.lwjgl.glfw.GLFW#GLFW_RELEASE GLFW} Action keys.
     */
    public static void onAfterMouseInput(final int button, final int modifiers, final int action) {
    }

    /**
     * Called from loader specific implementation of events. Used to call separate onInput inside of client handlers. Can be cancelled.
     *
     * @param input     The button being pressed. Use {@link org.lwjgl.glfw.GLFW#GLFW_KEY_SPACE GLFW} keys to determine what button is being pressed
     * @param modifiers The modifier key that is being applied to this action.
     * @param action    The action of this key. Use {@link org.lwjgl.glfw.GLFW#GLFW_RELEASE GLFW} Action keys.
     */
    public static Result onBeforeMouseInput(final InteractCallback.Input input, final int modifiers, final int action) {
        final Minecraft mc = Minecraft.getInstance();
        final InteractCallback.KeyMappings mappings = InteractCallback.KeyMappings.getMappings();

        if (mc.screen != null) {
            return Result.empty();
        }

        for (final InteractCallback interactCallback : SimClickInteractions.CLICK_INTERACTION_ENTRIES) {
            final Result returnEvent = InteractCallback.filterInteract(interactCallback, input, modifiers, action, mappings);

            if (!Result.empty().equals(returnEvent)) {
                return returnEvent;
            }
        }

        return Result.empty();
    }


    /**
     * Called from loader specific implementation of events. Used to call onMouseMove inside of client handlers. Can be cancelled.
     *
     * @param yaw    The resulting change in yaw
     * @param pitch  The modifier key that is being applied to this action.
     */
    public static Result onMouseMove(final double yaw, final double pitch) {
        final Minecraft mc = Minecraft.getInstance();

        if (mc.screen != null) {
            return Result.empty();
        }

        for (final InteractCallback interactCallback : SimClickInteractions.CLICK_INTERACTION_ENTRIES) {
            final Result returnEvent = interactCallback.onMouseMove(yaw, pitch);
            if (!Result.empty().equals(returnEvent)) {
                return returnEvent;
            }
        }

        return Result.empty();
    }

    public static void onAfterKeyPress(final int key, final int scanCode, final int action, final int modifiers) {
        LinkedTypewriterInteractionHandler.onKeyPress(key, scanCode, action, modifiers);
    }

    /**
     * Called whenever the scrollwheel on a mouse is used.
     *
     * @param deltaX x scroll value
     * @param deltaY y scroll value
     */
    public static Result onMouseScroll(final double deltaX, final double deltaY) {
        if (Minecraft.getInstance().screen == null) {
            for (final InteractCallback interactCallback : SimClickInteractions.CLICK_INTERACTION_ENTRIES) {
                final Result result = interactCallback.onScroll(deltaX, deltaY);

                if (!Result.empty().equals(result)) {
                    return result;
                }
            }
        }

        return Result.empty();
    }

    /**
     * Render all overlays used.
     *
     * @param graphics Minecraft's abstract GUIGraphics class
     */
    public static void renderOverlays(final GuiGraphics graphics, final float pt) {
        final int width = graphics.guiWidth();
        final int height = graphics.guiHeight();

        HoldTipManager.renderOverlay(graphics);
        HoldInteractionManager.renderOverlay(graphics, width, height);
    }

    /**
     * Called at the beginning of a ticking event for the given level
     */
    public static void preClientTick(final Minecraft instance) {
        ThrottleLeverClientGripHandler.clearNearbyThrottleLevers();
        double delta = 0;
        if (SimKeys.SCROLL_UP.isPressed()) {
            delta++;
        }
        if (SimKeys.SCROLL_DOWN.isPressed()) {
            delta--;
        }
        if (delta != 0) {
            onMouseScroll(0, delta);
        }
    }

    /**
     * Called at the end of a ticking event for the given level
     */
    public static void postClientTick(final Minecraft instance) {
        SimulatedClient.PHYSICS_STAFF_CLIENT_HANDLER.tick();

	    if (instance.player == null || instance.level == null) {
		    return;
	    }

	    final Level level = SableDistUtil.getClientLevel();
	    final LocalPlayer player = (LocalPlayer) SimDistUtil.getClientPlayer();

	    SimulatedClient.MERGING_GLUE_ITEM_HANDLER.clientTick(level, player);
	    ClientRopeItemHandler.tick();
        ZiplineClientManager.tick();
        LinkedTypewriterInteractionHandler.tick();

        if (instance.screen != null) {
            HoldInteractionManager.stop();
        }

        for (final InteractCallback interactCallback : SimClickInteractions.CLICK_INTERACTION_ENTRIES) {
            interactCallback.clientTick(level, player);
        }

        HoldInteractionManager.tick(level, player);
        HoldTipManager.tick();
        EndSeaRenderer.tick();

        SimulatedClient.PLUNGER_LAUNCHER_RENDER_HANDLER.tick();
    }

    /**
     * Called at the head of {@link net.minecraft.client.multiplayer.MultiPlayerGameMode#performUseItemOn(LocalPlayer, InteractionHand, BlockHitResult)}
     * @return non-null to cancel and use given result
     */
    @Nullable
    public static InteractionResult onRightClickBlock(final Player player, final InteractionHand hand, final BlockPos pos, final BlockHitResult hitResult) {
        if (HoldInteractionManager.isActive()) {
            return InteractionResult.FAIL;
        }

        return null;
    }

    /**
     * Called when highlighting an itemstack or when populating the creative search menu, to modify item tooltips
     * @param player null when used for creative search filtering
     */
    public static void appendTooltip(final ItemStack stack, final TooltipFlag iTooltipFlag, final @Nullable Player player, final List<Component> itemTooltip) {
        final BlockPropertiesTooltip.Condition propertiesCondition = SimConfigService.INSTANCE.client().itemConfig.displayProperties.get();
        if (BlockPropertiesTooltip.shouldShowTooltip(propertiesCondition, iTooltipFlag, player)) {
            BlockPropertiesTooltip.appendTooltip(stack, iTooltipFlag, player, itemTooltip);
        }
    }

	public static void useItemOnAirEvent(final Level level, final Player player, final ItemStack itemStack, final InteractionHand hand) {
		SimulatedClient.MERGING_GLUE_ITEM_HANDLER.resetWhenShiftRC(player, itemStack);
	}

    /**
     * Called when attempting to right-click in world
     * @return true to cancel
     */
    public static boolean useItemMappingTriggered() {
        return HoldInteractionManager.isActive();
    }

    /**
     * Called when using an item on a block
     * @return true to cancel
     */
	public static boolean useItemOnBlockEvent(final Level level, final Player player, final ItemStack itemStack, final InteractionHand hand) {
        return SimulatedClient.MERGING_GLUE_ITEM_HANDLER.onItemUseBlock(level, player, itemStack, hand);
    }
}
