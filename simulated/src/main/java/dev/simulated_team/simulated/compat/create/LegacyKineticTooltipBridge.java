package dev.simulated_team.simulated.compat.create;

import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class LegacyKineticTooltipBridge {
    public interface Delegate {
        boolean addToTooltip(KineticBlockEntity blockEntity, List<Component> tooltip, boolean isPlayerSneaking);

        boolean addToGoggleTooltip(KineticBlockEntity blockEntity, List<Component> tooltip, boolean isPlayerSneaking);

        void addStressImpactStats(KineticBlockEntity blockEntity, List<Component> tooltip, float stressAtBase);
    }

    private static final Delegate NO_OP = new Delegate() {
        @Override
        public boolean addToTooltip(final KineticBlockEntity blockEntity, final List<Component> tooltip,
                                    final boolean isPlayerSneaking) {
            return false;
        }

        @Override
        public boolean addToGoggleTooltip(final KineticBlockEntity blockEntity, final List<Component> tooltip,
                                          final boolean isPlayerSneaking) {
            return false;
        }

        @Override
        public void addStressImpactStats(final KineticBlockEntity blockEntity, final List<Component> tooltip,
                                         final float stressAtBase) {
        }
    };

    private static Delegate delegate = NO_OP;

    private LegacyKineticTooltipBridge() {
    }

    public static void setDelegate(final Delegate delegate) {
        LegacyKineticTooltipBridge.delegate = delegate == null ? NO_OP : delegate;
    }

    public static boolean addToTooltip(final KineticBlockEntity blockEntity, final List<Component> tooltip,
                                       final boolean isPlayerSneaking) {
        return delegate.addToTooltip(blockEntity, tooltip, isPlayerSneaking);
    }

    public static boolean addToGoggleTooltip(final KineticBlockEntity blockEntity, final List<Component> tooltip,
                                             final boolean isPlayerSneaking) {
        return delegate.addToGoggleTooltip(blockEntity, tooltip, isPlayerSneaking);
    }

    public static void addStressImpactStats(final KineticBlockEntity blockEntity, final List<Component> tooltip,
                                            final float stressAtBase) {
        delegate.addStressImpactStats(blockEntity, tooltip, stressAtBase);
    }
}
