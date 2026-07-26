package dev.simulated_team.simulated.util.click_interactions;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * An interface denoting this class as a client-side InteractCallback. <br/>
 * Exposes Use, Attack, and Pick keys, and client-side ticking, along with mouse scrolling. <br/>
 * Keys and scrolling are both cancelable utilizing {@link Result input results}
 */
public interface InteractCallback {

    /**
     * Filters the given button and calls the appropriate method inside the given InteractCallback interface.
     *
     * @param clickInteraction The interface to call methods from.
     * @param input The input being modified
     * @param modifiers Modifiers held down during this interaction.
     * @param action The type of action this interaction is.
     * @param associatedMappings Commonly used key mappings
     * @return Whether this interaction should be canceled, or handled by vanilla logic
     */
    @NotNull
    static InteractCallback.Result filterInteract(final InteractCallback clickInteraction, final Input input, final int modifiers, final int action, final KeyMappings associatedMappings) {
        if (input.matches(associatedMappings.attack)) {
            return clickInteraction.onAttack(modifiers, action, associatedMappings.attack);
        }

        if (input.matches(associatedMappings.middle)) {
            return clickInteraction.onPick(modifiers, action, associatedMappings.middle);
        }

        if (input.matches(associatedMappings.use)) {
            return clickInteraction.onUse(modifiers, action, associatedMappings.use);
        }

        return Result.empty();
    }

    default Result onPick(final int modifiers, final int action, final KeyMapping middleKey) {
        return Result.empty();
    }

    default Result onAttack(final int modifiers, final int action, final KeyMapping leftKey) {
        return Result.empty();
    }

    default Result onUse(final int modifiers, final int action, final KeyMapping rightKey) {
        return Result.empty();
    }

    default Result onScroll(final double deltaX, final double deltaY) {
        return Result.empty();
    }

    default Result onMouseMove(final double yaw, final double pitch) {
        return Result.empty();
    }

    /**
     * The general client tick. Called at the end of the level's ticking event.
     */
    default void clientTick(final Level level, final LocalPlayer player) {
    }

    /**
     * Commonly used key mappings.
     */
    record KeyMappings(KeyMapping use, KeyMapping attack, KeyMapping middle) {
        private static final KeyMappings MAPPINGS = populateMappings();

        public static KeyMappings getMappings() {
            return MAPPINGS;
        }

        private static KeyMappings populateMappings() {
            final Options options = Minecraft.getInstance().options;
            return new KeyMappings(options.keyUse, options.keyAttack, options.keyPickItem);
        }
    }

    record Input(boolean mouse, int key, int scanCode) {
        public static Input mouse(final int key) {
            return new Input(true, key, -1);
        }

        public static Input key(final int key, final int scanCode) {
            return new Input(false, key, scanCode);
        }

        public boolean matches(final KeyMapping mapping) {
            if (mouse) {
                return mapping.matchesMouse(new MouseButtonEvent(0, 0, new MouseButtonInfo(key, 0)));
            }
            return mapping.matches(new KeyEvent(key, scanCode, 0));
        }
    }

    /**
     * Determines whether this interaction should be canceled, or handled by vanilla logic.
     */
    record Result(boolean cancelled) {
        private static final Result EMPTY = new Result(false);
        public static Result empty() {
            return EMPTY;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj.getClass() != this.getClass()) {
                return false;
            }

            final Result otherEvent = (Result) obj;
            return otherEvent.cancelled == this.cancelled;
        }
    }
}
