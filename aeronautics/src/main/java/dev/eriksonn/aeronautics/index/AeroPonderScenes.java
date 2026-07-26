package dev.eriksonn.aeronautics.index;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.content.ponder.scenes.*;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class AeroPonderScenes {

    public static void register(final PonderSceneRegistrationHelper<ResourceLocation> registry) {
        final PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> helper = registry.withKeyFunction(ItemProviderEntry::getId);

        helper.forComponents(aeroItemProvider("levitite_blend_bucket"))
                .addStoryBoard("levitite/blend", LevititeScenes::levititeBlend)
                .addStoryBoard("levitite/levitite",(builder,util) -> LevititeScenes.levitite(builder,util,false));
        helper.forComponents(AeroBlocks.LEVITITE)
                .addStoryBoard("levitite/levitite",(builder,util) -> LevititeScenes.levitite(builder,util,false))
                .addStoryBoard("levitite/blend", LevititeScenes::levititeBlend);
        helper.forComponents(AeroBlocks.PEARLESCENT_LEVITITE)
                .addStoryBoard("levitite/levitite",(builder,util) -> LevititeScenes.levitite(builder,util,true))
                .addStoryBoard("levitite/blend", LevititeScenes::levititeBlend);

        helper.forComponents(AeroBlocks.HOT_AIR_BURNER)
                .addStoryBoard("hot_air/adjustable_burner", (scene, util) -> HotAirScenes.hotAirBurner(scene, util, false))
                        .addStoryBoard("airpressure/burner",(builder,util) -> AirPressureScenes.airPressure(builder,util,new AirPressureScenes.Burner()));
        helper.forComponents(AeroBlocks.STEAM_VENT)
                .addStoryBoard("hot_air/steam_vent", (scene, util) -> HotAirScenes.hotAirBurner(scene, util, true))
                .addStoryBoard("airpressure/vent",(builder,util) -> AirPressureScenes.airPressure(builder,util,new AirPressureScenes.Vent()));
        helper.forComponents(itemProviders(AeroBlocks.DYED_ENVELOPE_BLOCKS))
                .addStoryBoard("hot_air/envelope", HotAirScenes::envelope);

        //KINETICS
        helper.forComponents(AeroBlocks.PROPELLER_BEARING)
                .addStoryBoard("propeller_bearing/thrust", (builder,util) -> PropellerScenes.propellerBearingThrust(builder,util,false))
                .addStoryBoard("propeller_bearing/size", PropellerScenes::propellerBearingSize)
                .addStoryBoard("airpressure/propeller", (builder,util) -> AirPressureScenes.airPressure(builder,util,new AirPressureScenes.PropellerBearing()));
        helper.forComponents(AeroBlocks.GYROSCOPIC_PROPELLER_BEARING)
                .addStoryBoard("gyroscopic_propeller_bearing/helicopter", PropellerScenes::gyroBearingStabilize)
                .addStoryBoard("gyroscopic_propeller_bearing/island", PropellerScenes::gyroBearingIsland)
                .addStoryBoard("gyroscopic_propeller_bearing/redstone", PropellerScenes::gyroBearingRedstone)
                .addStoryBoard("airpressure/gyro", (builder,util) -> AirPressureScenes.airPressure(builder,util,new AirPressureScenes.GyroBearing()));
        helper.forComponents(AeroBlocks.MOUNTED_POTATO_CANNON)
                .addStoryBoard("mounted_potato_cannon/intro", MountedPotatoCannonScenes::mountedPotatoCannonIntro);
        helper.forComponents(AeroBlocks.ANDESITE_PROPELLER)
                .addStoryBoard("propeller_bearing/thrust", (builder,util) -> PropellerScenes.propellerBearingThrust(builder,util,true))
                        .addStoryBoard("airpressure/miniprop",(builder,util) -> AirPressureScenes.airPressure(builder,util,new AirPressureScenes.Miniprop()));
        helper.forComponents(AeroBlocks.WOODEN_PROPELLER)
                .addStoryBoard("propeller_bearing/thrust", (builder,util) -> PropellerScenes.propellerBearingThrust(builder,util,true))
                .addStoryBoard("airpressure/miniprop",(builder,util) -> AirPressureScenes.airPressure(builder,util,new AirPressureScenes.Miniprop()));
    }

    private static ItemProviderEntry<Item, Item> aeroItemProvider(final String id) {
        return new ItemProviderEntry<>(
                Aeronautics.getRegistrate(),
                io.github.fabricators_of_create.porting_lib.util.DeferredHolder.create(ResourceKey.create(Registries.ITEM, Aeronautics.path(id)))
        );
    }

    private static Iterable<ItemProviderEntry<?, ?>> itemProviders(final Iterable<? extends net.minecraft.world.level.ItemLike> items) {
        final List<ItemProviderEntry<?, ?>> entries = new ArrayList<>();
        items.forEach(item -> entries.add(new ItemProviderEntry<>(
                Aeronautics.getRegistrate(),
                io.github.fabricators_of_create.porting_lib.util.DeferredHolder.create(
                        ResourceKey.create(Registries.ITEM, net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.asItem()))
                )
        )));
        return entries;
    }
}
