package dev.eriksonn.aeronautics.content.ponder;

import com.zurrtum.create.client.infrastructure.ponder.AllCreatePonderTags;
import dev.simulated_team.simulated.index.SimPonderTags;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.eriksonn.aeronautics.index.AeroTags;
import dev.eriksonn.aeronautics.service.AeroLevititeService;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.client.ponder.api.registration.MultiTagBuilder;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class AeroPonderTags {

    public static final ResourceLocation
            LEVITITE_BREAKABLE = Aeronautics.path("levitite_breakable");

    public static void register(final PonderTagRegistrationHelper<ResourceLocation> helper) {
        final PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(
                RegisteredObjectsHelper::getKeyOrThrow);

        // Aero Tags

        helper.registerTag(LEVITITE_BREAKABLE)
                .item(AeroLevititeService.INSTANCE.getBucket())
                .title("Breaks When Crystallizing")
                .description("Blocks that are broken when nearby Levitite Blend crystallizes into Levitite. Useful for making molds for casting")
                .register();

        itemHelper.addToTag(LEVITITE_BREAKABLE).add(AeroLevititeService.INSTANCE.getBucket());
        itemHelper.addToTag(LEVITITE_BREAKABLE)
                .add(Blocks.CLAY)
                .add(Blocks.MUD)
                .add(Blocks.PACKED_MUD)
                .add(Blocks.COARSE_DIRT);

        // Simulated Tags

        itemHelper.addToTag(SimPonderTags.PHYSICS_BEHAVIOR)
                .add(AeroBlocks.PROPELLER_BEARING.asItem())
                .add(AeroBlocks.GYROSCOPIC_PROPELLER_BEARING.asItem())
                .add(AeroBlocks.SMART_PROPELLER.asItem())
                .add(AeroBlocks.ANDESITE_PROPELLER.asItem())
                .add(AeroBlocks.WOODEN_PROPELLER.asItem())
                .add(AeroBlocks.WHITE_ENVELOPE_BLOCK.asItem())
                .add(AeroBlocks.HOT_AIR_BURNER.asItem())
                .add(AeroBlocks.STEAM_VENT.asItem())
                .add(AeroBlocks.LEVITITE.asItem())
                .add(AeroBlocks.PEARLESCENT_LEVITITE.asItem());

        itemHelper.addToTag(SimPonderTags.THRUST_PRODUCING_BLOCKS)
                .add(AeroBlocks.PROPELLER_BEARING.asItem())
                .add(AeroBlocks.GYROSCOPIC_PROPELLER_BEARING.asItem())
                .add(AeroBlocks.SMART_PROPELLER.asItem())
                .add(AeroBlocks.ANDESITE_PROPELLER.asItem())
                .add(AeroBlocks.WOODEN_PROPELLER.asItem());

        // Create Tags

        itemHelper.addToTag(AllCreatePonderTags.KINETIC_APPLIANCES)
                .add(AeroBlocks.PROPELLER_BEARING.asItem())
                .add(AeroBlocks.GYROSCOPIC_PROPELLER_BEARING.asItem())
                .add(AeroBlocks.SMART_PROPELLER.asItem())
                .add(AeroBlocks.ANDESITE_PROPELLER.asItem())
                .add(AeroBlocks.WOODEN_PROPELLER.asItem())
                .add(AeroBlocks.MOUNTED_POTATO_CANNON.asItem());

        itemHelper.addToTag(AllCreatePonderTags.ARM_TARGETS)
                .add(AeroBlocks.MOUNTED_POTATO_CANNON.asItem());

        //todo remove if this isn't actually implemented before release
        itemHelper.addToTag(AllCreatePonderTags.THRESHOLD_SWITCH_TARGETS)
                .add(AeroBlocks.HOT_AIR_BURNER.asItem())
                .add(AeroBlocks.STEAM_VENT.asItem());

        itemHelper.addToTag(AllCreatePonderTags.DISPLAY_SOURCES)
                .add(AeroBlocks.HOT_AIR_BURNER.asItem())
                .add(AeroBlocks.STEAM_VENT.asItem())
                .add(AeroBlocks.PROPELLER_BEARING.asItem())
                .add(AeroBlocks.GYROSCOPIC_PROPELLER_BEARING.asItem())
                .add(AeroBlocks.SMART_PROPELLER.asItem())
                .add(AeroBlocks.ANDESITE_PROPELLER.asItem())
                .add(AeroBlocks.WOODEN_PROPELLER.asItem());
    }
}
