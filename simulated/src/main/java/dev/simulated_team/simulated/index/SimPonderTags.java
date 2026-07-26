package dev.simulated_team.simulated.index;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.infrastructure.ponder.AllCreatePonderTags;
import dev.simulated_team.simulated.Simulated;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class SimPonderTags {

    public static final ResourceLocation
            NAVIGATION_ITEMS = Simulated.path("navigation_items"),
            PHYSICS_BEHAVIOR = Simulated.path("physics_behavior"),
            THRUST_PRODUCING_BLOCKS = Simulated.path("thrust_blocks"),
            PHYSICS_SENSORS = Simulated.path("physics_sensors");

    public static void register(final PonderTagRegistrationHelper<ResourceLocation> helper) {
        final PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(
                RegisteredObjectsHelper::getKeyOrThrow);

        // Sim Tags

        helper.registerTag(NAVIGATION_ITEMS)
                .addToIndex()
                .item(SimBlocks.NAVIGATION_TABLE.asItem())
                .title("Navigation Items")
                .description("Components which offer a destination to a Navigation Table")
                .register();

        itemHelper.addToTag(NAVIGATION_ITEMS)
                .add(SimBlocks.NAVIGATION_TABLE.asItem()) // why create... why
                .add(Items.COMPASS)
                .add(Blocks.LODESTONE)
                .add(Items.RECOVERY_COMPASS)
                .add(Items.FILLED_MAP)
                .add(SimBlocks.REDSTONE_MAGNET.asItem());

        helper.registerTag(PHYSICS_BEHAVIOR)
                .addToIndex()
                .item(SimBlocks.PHYSICS_ASSEMBLER.asItem())
                .title("Physics Behavior")
                .description("Components which have unique physics behavior or interactions")
                .register();

        itemHelper.addToTag(PHYSICS_BEHAVIOR)
                .add(SimBlocks.PHYSICS_ASSEMBLER.asItem()) // why create... why
                .add(SimBlocks.SWIVEL_BEARING.asItem())
                .add(AllBlocks.STICKER.asItem())
                .add(AllBlocks.WEIGHTED_EJECTOR.asItem())
                .add(SimBlocks.DOCKING_CONNECTOR.asItem())
                .add(SimBlocks.REDSTONE_MAGNET.asItem())
                .add(AllBlocks.SAIL.asItem())
                .add(SimBlocks.WHITE_SYMMETRIC_SAIL.asItem())
                .add(AllItems.BELT_CONNECTOR.asItem())
                .add(SimItems.SPRING.asItem())
                .add(SimItems.ROPE_COUPLING.asItem());

        helper.registerTag(THRUST_PRODUCING_BLOCKS)
                .addToIndex()
                .item(AllBlocks.ENCASED_FAN.asItem())
                .title("Thrust Producing Blocks")
                .description("Components which produce thrust on Simulated Contraptions")
                .register();

        itemHelper.addToTag(THRUST_PRODUCING_BLOCKS)
                .add(AllBlocks.ENCASED_FAN.asItem())
                .add(AllBlocks.NOZZLE.asItem());

        helper.registerTag(PHYSICS_SENSORS)
                .addToIndex()
                .item(SimBlocks.OPTICAL_SENSOR.asItem())
                .title("Physics Sensor Blocks")
                .description("Components which provide dynamic information about the world around them")
                .register();

        itemHelper.addToTag(PHYSICS_SENSORS)
                .add(SimBlocks.ALTITUDE_SENSOR.asItem())
                .add(SimBlocks.VELOCITY_SENSOR.asItem())
                .add(SimBlocks.GIMBAL_SENSOR.asItem())
                .add(SimBlocks.OPTICAL_SENSOR.asItem())
                .add(SimBlocks.NAVIGATION_TABLE.asItem())
                .add(SimBlocks.LASER_SENSOR.asItem());

        // Create Tags

        itemHelper.addToTag(AllCreatePonderTags.KINETIC_RELAYS)
                .add(SimBlocks.DIRECTIONAL_GEARSHIFT.asItem())
                .add(SimBlocks.TORSION_SPRING.asItem())
                .add(SimBlocks.ANALOG_TRANSMISSION.asItem());

        itemHelper.addToTag(AllCreatePonderTags.KINETIC_SOURCES)
                .add(SimBlocks.STEERING_WHEEL.asItem())
                .add(SimBlocks.RED_PORTABLE_ENGINE.get().asItem());

        itemHelper.addToTag(AllCreatePonderTags.KINETIC_APPLIANCES)
                .add(SimBlocks.SWIVEL_BEARING.asItem())
                .add(SimBlocks.ROPE_WINCH.asItem());

        itemHelper.addToTag(AllCreatePonderTags.FLUIDS)
                .add(SimBlocks.DOCKING_CONNECTOR.asItem());

        itemHelper.addToTag(AllCreatePonderTags.LOGISTICS)
                .add(SimBlocks.AUGER_SHAFT.asItem())
                .add(SimBlocks.AUGER_COG.asItem())
                .add(SimBlocks.DOCKING_CONNECTOR.asItem());

        itemHelper.addToTag(AllCreatePonderTags.REDSTONE)
                .add(SimBlocks.THROTTLE_LEVER.asItem())
                .add(SimBlocks.LINKED_TYPEWRITER.asItem())
                .add(SimBlocks.DIRECTIONAL_LINKED_RECEIVER.asItem())
                .add(SimBlocks.MODULATING_LINKED_RECEIVER.asItem())
                .add(SimBlocks.REDSTONE_ACCUMULATOR.asItem())
                .add(SimBlocks.REDSTONE_INDUCTOR.asItem());

        itemHelper.addToTag(AllCreatePonderTags.MOVEMENT_ANCHOR)
                .add(SimBlocks.PHYSICS_ASSEMBLER.asItem())
                .add(SimBlocks.SWIVEL_BEARING.asItem());

        itemHelper.addToTag(AllCreatePonderTags.SAILS)
                .add(SimBlocks.WHITE_SYMMETRIC_SAIL.asItem());

        itemHelper.addToTag(AllCreatePonderTags.ARM_TARGETS)
                .add(SimBlocks.RED_PORTABLE_ENGINE.get().asItem())
                .add(SimBlocks.NAVIGATION_TABLE.asItem());

        itemHelper.addToTag(AllCreatePonderTags.DISPLAY_SOURCES)
                .add(SimBlocks.AUGER_SHAFT.asItem())
                .add(SimBlocks.AUGER_COG.asItem())
                .add(SimBlocks.RED_PORTABLE_ENGINE.get().asItem())
                .add(SimBlocks.ALTITUDE_SENSOR.asItem())
                .add(SimBlocks.VELOCITY_SENSOR.asItem())
                .add(SimBlocks.GIMBAL_SENSOR.asItem())
                .add(SimBlocks.OPTICAL_SENSOR.asItem())
                .add(SimBlocks.NAVIGATION_TABLE.asItem())
                .add(SimBlocks.DOCKING_CONNECTOR.asItem())
                .add(SimBlocks.LINKED_TYPEWRITER.asItem());

        itemHelper.addToTag(AllCreatePonderTags.DISPLAY_TARGETS)
                .add(SimBlocks.NAMEPLATES.get(DyeColor.WHITE).asItem());

        itemHelper.addToTag(AllCreatePonderTags.THRESHOLD_SWITCH_TARGETS)
                .add(SimBlocks.ROPE_WINCH.asItem());
    }
}
