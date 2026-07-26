package com.zurrtum.create.foundation.data;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.registry.CreateRegistries;

public class CreateRegistrate extends Registrate {
    public CreateRegistrate(String modId) {
        super(modId);
    }

    public <T extends DisplaySource> NoConfigBuilder<DisplaySource, T, CreateRegistrate> displaySource(
            String name, NonNullSupplier<T> supplier) {
        return generic(self(), name, (net.minecraft.resources.ResourceKey) CreateRegistries.DISPLAY_SOURCE.key(), supplier);
    }

    public <T extends DisplayTarget> NoConfigBuilder<DisplayTarget, T, CreateRegistrate> displayTarget(
            String name, NonNullSupplier<T> supplier) {
        return generic(self(), name, (net.minecraft.resources.ResourceKey) CreateRegistries.DISPLAY_TARGET.key(), supplier);
    }

}
