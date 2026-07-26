package dev.simulated_team.simulated.compat.create;

/**
 * Marks Create renderer extraction performed by Sable's immediate Sodium pass.
 *
 * <p>Create normally skips most block-entity renderers when Flywheel owns the
 * corresponding visual. Flywheel's visual is positioned in plot space, though,
 * while this pass applies the moving sublevel transform. The immediate fallback
 * therefore has to behave as if visualization support were unavailable.</p>
 */
public final class SableCreateRenderContext {
    private static final ThreadLocal<Boolean> ACTIVE =
            ThreadLocal.withInitial(() -> false);

    private SableCreateRenderContext() {
    }

    public static boolean isActive() {
        return ACTIVE.get();
    }

    public static void run(final Runnable action) {
        final boolean previous = ACTIVE.get();
        ACTIVE.set(true);
        try {
            action.run();
        } finally {
            if (previous) {
                ACTIVE.set(true);
            } else {
                ACTIVE.remove();
            }
        }
    }
}
