package dev.eriksonn.aeronautics.index;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.content.display_sources.GasDisplaySource;

public class AeroDisplaySources {

    public static final RegistryEntry<DisplaySource, GasDisplaySource> GAS_DISPLAY = simple("gas_display", GasDisplaySource::new);

    private static <T extends DisplaySource> RegistryEntry<DisplaySource, T> simple(final String name, final NonNullSupplier<T> supplier) {
        return Aeronautics.getRegistrate().displaySource(name, supplier).register();
    }

}
