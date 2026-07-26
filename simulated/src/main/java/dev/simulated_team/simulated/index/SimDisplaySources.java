package dev.simulated_team.simulated.index;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.content.redstone.displayLink.source.ItemThroughputDisplaySource;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.display_sources.*;

import java.util.function.Supplier;

public class SimDisplaySources {
    public static final RegistryEntry<DisplaySource, ItemThroughputDisplaySource> AUGER_DISPLAY = simple("auger_throughput", ItemThroughputDisplaySource::new);
    public static final RegistryEntry<DisplaySource, VelocitySensorDisplaySource> VELO_DISPLAY = simple("velocity_display", VelocitySensorDisplaySource::new);
    public static final RegistryEntry<DisplaySource, GimbalSensorDisplaySource> GIMBAL_DISPLAY = simple("gimbal_display", GimbalSensorDisplaySource::new);
    public static final RegistryEntry<DisplaySource, OpticalSensorDisplaySource> OPTICAL_SENSOR_DISPLAY = simple("optical_sensor_display", OpticalSensorDisplaySource::new);
    public static final RegistryEntry<DisplaySource, LaserSensorDisplaySource> LASER_SENSOR_DISPLAY = simple("laser_sensor_display", LaserSensorDisplaySource::new);
    public static final RegistryEntry<DisplaySource, AltitudeSensorDisplaySource> ALTITUDE_SENSOR_DISPLAY = simple("altitude_sensor_display", AltitudeSensorDisplaySource::new);
    public static final RegistryEntry<DisplaySource, PortableEngineDisplaySource> PORTABLE_ENGINE_DISPLAY = simple("portable_engine_display", PortableEngineDisplaySource::new);
    public static final RegistryEntry<DisplaySource, DockingConnectorDisplaySource> DOCKING_CONNECTOR_DISPLAY = simple("docking_connector_display", DockingConnectorDisplaySource::new);
    public static final RegistryEntry<DisplaySource, LinkedTypewriterDisplaySource> LINKED_TYPEWRITER__DISPLAY = simple("linked_typewriter_display", LinkedTypewriterDisplaySource::new);
    public static final RegistryEntry<DisplaySource, NavigationTableDisplaySource> NAV_TABLE_DISPLAY = simple("nav_table_display", NavigationTableDisplaySource::new);

    private static <T extends DisplaySource> RegistryEntry<DisplaySource, T> simple(final String name, final NonNullSupplier<T> supplier) {
        return Simulated.getRegistrate().displaySource(name, supplier).register();
    }

}
