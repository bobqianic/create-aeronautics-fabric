package dev.ryanhcode.sable.api.physics;

import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Comparator;
import java.util.ServiceLoader;

/**
 * A provider of physics pipelines for levels
 */
public interface PhysicsPipelineProvider {

    /**
     * @since 1.0.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @interface LoadPriority {
        /**
         * @return The load priority value. The highest priority available pipeline provider is loaded
         */
        int value() default 1000;
    }

    /**
     * The physics pipeline provider instance.
     */
    PhysicsPipelineProvider INSTANCE = ServiceLoader.load(PhysicsPipelineProvider.class)
            .stream().max(Comparator.comparingInt(provider -> {
                Class<? extends PhysicsPipelineProvider> type = provider.type();
                LoadPriority annotation = type.getAnnotation(LoadPriority.class);
                return annotation != null ? annotation.value() : 1000;
            }))
            .map(ServiceLoader.Provider::get)
            .orElseThrow(() -> new RuntimeException("Failed to find any physics pipeline providers"));

    /**
     * Creates a physics pipeline for a given level
     * @param level the level to create the pipeline for
     * @return the new physics pipeline
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    PhysicsPipeline createPipeline(@NotNull final ServerLevel level);

}
