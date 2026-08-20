package dev.ryanhcode.sable.compatibility.entityculling;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.platform.SableLoaderPlatform;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Keeps EntityCulling from testing plot-space block entity bounds while Sable
 * extracts render states for a transformed sub-level.
 */
public final class EntityCullingCompat {

    private static final Scope NO_OP = new Scope(null, null);
    private static final AtomicBoolean ACCESS_WARNING_LOGGED = new AtomicBoolean();
    private static final Access ACCESS = createAccess();

    private EntityCullingCompat() {
    }

    public static Scope suspendBlockEntityCulling() {
        if (ACCESS == null) {
            return NO_OP;
        }

        try {
            final Object instance = ACCESS.instanceField().get(null);
            if (instance == null) {
                return NO_OP;
            }

            final Object config = ACCESS.configField().get(instance);
            if (config == null || ACCESS.skipBlockEntityCullingField().getBoolean(config)) {
                return NO_OP;
            }

            ACCESS.skipBlockEntityCullingField().setBoolean(config, true);
            return new Scope(config, ACCESS.skipBlockEntityCullingField());
        } catch (final ReflectiveOperationException | RuntimeException exception) {
            logAccessWarning(exception);
            return NO_OP;
        }
    }

    private static Access createAccess() {
        if (!SableLoaderPlatform.INSTANCE.isModLoaded("entityculling")) {
            return null;
        }

        try {
            final ClassLoader classLoader = EntityCullingCompat.class.getClassLoader();
            final Class<?> modClass = Class.forName(
                    "dev.tr7zw.entityculling.EntityCullingModBase",
                    false,
                    classLoader
            );
            final Class<?> configClass = Class.forName(
                    "dev.tr7zw.entityculling.versionless.Config",
                    false,
                    classLoader
            );
            return new Access(
                    modClass.getField("instance"),
                    modClass.getField("config"),
                    configClass.getField("skipBlockEntityCulling")
            );
        } catch (final ReflectiveOperationException | RuntimeException exception) {
            logAccessWarning(exception);
            return null;
        }
    }

    private static void logAccessWarning(final Exception exception) {
        if (ACCESS_WARNING_LOGGED.compareAndSet(false, true)) {
            Sable.LOGGER.warn(
                    "Unable to suspend EntityCulling while rendering sub-level block entities",
                    exception
            );
        }
    }

    private record Access(
            Field instanceField,
            Field configField,
            Field skipBlockEntityCullingField
    ) {
    }

    public static final class Scope implements AutoCloseable {

        private final Object config;
        private final Field skipBlockEntityCullingField;

        private Scope(final Object config, final Field skipBlockEntityCullingField) {
            this.config = config;
            this.skipBlockEntityCullingField = skipBlockEntityCullingField;
        }

        @Override
        public void close() {
            if (this.config == null) {
                return;
            }

            try {
                this.skipBlockEntityCullingField.setBoolean(this.config, false);
            } catch (final IllegalAccessException | RuntimeException exception) {
                logAccessWarning(exception);
            }
        }
    }
}
