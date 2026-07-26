package dev.simulated_team.simulated.content.physics_staff;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import java.lang.reflect.Method;

public final class OptionalShaderMods {
    private static final Method IRIS_PACK_IN_USE = findIrisPackMethod();

    private OptionalShaderMods() {
    }

    public static boolean isIrisLoaded() {
        return IRIS_PACK_IN_USE != null;
    }

    public static boolean isShaderPackActive() {
        if (IRIS_PACK_IN_USE == null) {
            return false;
        }

        try {
            return (boolean) IRIS_PACK_IN_USE.invoke(null);
        } catch (final ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    /**
     * Registers a mod-defined pipeline with Iris without making Iris a runtime
     * dependency. The caller can fall back to a vanilla pipeline when the
     * installed Iris version does not expose this compatibility API.
     */
    public static boolean registerIrisPipeline(final RenderPipeline pipeline, final String shaderKeyName) {
        if (!isIrisLoaded()) {
            return false;
        }

        try {
            final ClassLoader classLoader = OptionalShaderMods.class.getClassLoader();
            final Class<?> shaderKeyClass = Class.forName(
                    "net.irisshaders.iris.pipeline.programs.ShaderKey", false, classLoader);
            final Class<?> irisPipelinesClass = Class.forName(
                    "net.irisshaders.iris.pipeline.IrisPipelines", false, classLoader);
            final Object shaderKey = getEnumConstant(shaderKeyClass, shaderKeyName);

            irisPipelinesClass
                    .getMethod("assignPipeline", RenderPipeline.class, shaderKeyClass)
                    .invoke(null, pipeline, shaderKey);
            return true;
        } catch (final ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return false;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object getEnumConstant(final Class<?> enumClass, final String name) {
        return Enum.valueOf((Class<? extends Enum>) enumClass.asSubclass(Enum.class), name);
    }

    private static Method findIrisPackMethod() {
        try {
            return Class.forName("net.irisshaders.iris.Iris", false, OptionalShaderMods.class.getClassLoader())
                    .getMethod("isPackInUseQuick");
        } catch (final ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }
}
