package dev.eriksonn.aeronautics.index;

import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.client.content.kinetics.base.ShaftRenderer;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import dev.simulated_team.simulated.service.SimInventoryService;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlockEntity;
import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerRenderer;
import dev.eriksonn.aeronautics.content.blocks.hot_air.steam_vent.SteamVentBlockEntity;
import dev.eriksonn.aeronautics.content.blocks.hot_air.steam_vent.SteamVentRenderer;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.gyroscopic_propeller_bearing.GyroscopicPropellerBearingBlockEntity;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.gyroscopic_propeller_bearing.GyroscopicPropellerBearingRenderer;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.gyroscopic_propeller_bearing.GyroscopicPropellerBearingVisual;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing.PropellerBearingBlockEntity;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing.PropellerBearingRenderer;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing.PropellerBearingVisual;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.andesite.AndesitePropellerBlockEntity;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.andesite.AndesitePropellerRenderer;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.andesite.AndesitePropellerVisual;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.smart_propeller.SmartPropellerBlockEntity;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.smart_propeller.SmartPropellerRenderer;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.wooden.WoodenPropellerBlockEntity;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.wooden.WoodenPropellerRenderer;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.wooden.WoodenPropellerVisual;
import dev.eriksonn.aeronautics.content.blocks.mounted_potato_cannon.MountedPotatoCannonBlockEntity;
import dev.eriksonn.aeronautics.content.blocks.mounted_potato_cannon.MountedPotatoCannonRenderer;
import dev.eriksonn.aeronautics.content.blocks.mounted_potato_cannon.MountedPotatoCannonVisual;

public class AeroBlockEntityTypes {
    private static final SimulatedRegistrate REGISTRATE = Aeronautics.getRegistrate();

    public static final BlockEntityEntry<SteamVentBlockEntity> STEAM_VENT = REGISTRATE
            .blockEntity("steam_vent", SteamVentBlockEntity::new)
            .validBlocks(AeroBlocks.STEAM_VENT)
            .renderer(() -> SteamVentRenderer::new)
            .register();

    public static final BlockEntityEntry<HotAirBurnerBlockEntity> HOT_AIR_BURNER = REGISTRATE
            .blockEntity("adjustable_burner", HotAirBurnerBlockEntity::new)
            .validBlocks(AeroBlocks.HOT_AIR_BURNER)
            .renderer(() -> HotAirBurnerRenderer::new)
            .register();

    public static final BlockEntityEntry<PropellerBearingBlockEntity> PROPELLER_BEARING = REGISTRATE
            .blockEntity("propeller_bearing", PropellerBearingBlockEntity::new)
            .validBlocks(AeroBlocks.PROPELLER_BEARING)
            .renderer(() -> PropellerBearingRenderer::new)
            .register();

    public static final BlockEntityEntry<GyroscopicPropellerBearingBlockEntity> GYROSCOPIC_PROPELLER_BEARING = REGISTRATE
            .blockEntity("gyroscopic_propeller_bearing", GyroscopicPropellerBearingBlockEntity::new)
            .validBlocks(AeroBlocks.GYROSCOPIC_PROPELLER_BEARING)
            .renderer(() -> GyroscopicPropellerBearingRenderer::new)
            .register();

    public static final BlockEntityEntry<KineticBlockEntity> ENVELOPE_ENCASED_SHAFT = REGISTRATE
            .blockEntity("envelope_encased_shaft", KineticBlockEntity::new)
            .validBlocks(AeroBlocks.ENVELOPE_ENCASED_SHAFTS::toArray)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static final BlockEntityEntry<AndesitePropellerBlockEntity> ANDESITE_PROPELLER = REGISTRATE
            .blockEntity("andesite_propeller", AndesitePropellerBlockEntity::new)
            .validBlocks(AeroBlocks.ANDESITE_PROPELLER)
            .renderer(() -> AndesitePropellerRenderer::new)
            .register();

    public static final BlockEntityEntry<WoodenPropellerBlockEntity> WOODEN_PROPELLER = REGISTRATE
            .blockEntity("wooden_propeller", WoodenPropellerBlockEntity::new)
            .renderer(() -> WoodenPropellerRenderer::new)
            .validBlocks(AeroBlocks.WOODEN_PROPELLER)
            .register();

    public static final BlockEntityEntry<SmartPropellerBlockEntity> SMART_PROPELLER = REGISTRATE
            .blockEntity("smart_propeller", SmartPropellerBlockEntity::new)
            .renderer(() -> SmartPropellerRenderer::new)
            .validBlocks(AeroBlocks.SMART_PROPELLER)
            .register();

    public static final BlockEntityEntry<MountedPotatoCannonBlockEntity> MOUNTED_POTATO_CANNON = REGISTRATE
            .blockEntity("mounted_potato_cannon", MountedPotatoCannonBlockEntity::new)
            .onRegister(SimInventoryService.INSTANCE.registerInventory((be, dir) -> be.getInventory()))
            .validBlocks(AeroBlocks.MOUNTED_POTATO_CANNON)
            .renderer(() -> MountedPotatoCannonRenderer::new)
            .register();

    public static void init() {
    }
}
