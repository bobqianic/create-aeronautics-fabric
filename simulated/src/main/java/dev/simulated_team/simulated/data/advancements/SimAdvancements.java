package dev.simulated_team.simulated.data.advancements;

import com.google.common.collect.Sets;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.index.SimItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static dev.simulated_team.simulated.data.advancements.SimulatedAdvancement.TaskType;

public class SimAdvancements implements DataProvider {
    public static final List<SimulatedAdvancement> SIM_ADVANCEMENTS = new ArrayList<>();
    private final PackOutput output;    public static final SimulatedAdvancement

//         Root Advancements
    ROOT = create("root", b -> b
            .icon(SimItems.CONTRAPTION_DIAGRAM)
            .title("Create Simulated")
            .description("Physics Be Upon Ye")
            .awardedForFree()
            .special(TaskType.SILENT)),

    APPLIED_KINEMATICS = create("applied_kinematics", b -> b
            .icon(SimBlocks.PHYSICS_ASSEMBLER)
            .title("Applied Kinematics")
            .description("Obtain a Physics Assembler, the heart of every Simulated Contraption")
            .special(TaskType.NOISY)
            .after(ROOT)
            .whenIconCollected()),

    // -----------------

    OPPOSITES_ATTRACT = create("opposite_attract", b -> b
            .icon(SimBlocks.REDSTONE_MAGNET)
            .title("Opposites Attract")
            .description("Place and power a Redstone Magnet")
            .after(APPLIED_KINEMATICS)),

    A_CALCULATED_CONNECTION = create("a_calculated_connection", b -> b
            .icon(SimBlocks.DOCKING_CONNECTOR)
            .title("A Calculated Connection")
            .description("Successfully align and connect two Docking Connectors")
            .after(OPPOSITES_ATTRACT)
            .special(TaskType.NOISY)),

    YOU_SPIN_ME_RIGHT_ROUND = create("you_spin_me_right_round", b -> b
            .icon(SimBlocks.SWIVEL_BEARING)
            .title("You Spin Me Right Round")
            .description("Assemble a Simulated Contraption using a Swivel Bearing")
            .after(APPLIED_KINEMATICS)
            .special(TaskType.NOISY)),
    // -----------------

    LEARNING_THE_ROPES = create("learning_the_ropes", b -> b
            .icon(SimItems.ROPE_COUPLING)
            .title("Learning the Ropes")
            .after(APPLIED_KINEMATICS)
            .description("Connect a Rope Connector or Rope Spool with Rope")),

    STUCK_TOGETHER = create("stuck_together", b ->  b
         .icon(SimItems.PLUNGER_LAUNCHER)
         .title("Stuck Together")
         .after(LEARNING_THE_ROPES)
         .description("Craft a Plunger Launcher")
         .whenIconCollected()),

    // Essential Advancements
    NOT_GONNA_SUGARCOAT_IT = create("not_gonna_sugarcoat_it", b -> b
            .icon(SimItems.HONEY_GLUE)
            .title("Not Gonna Sugarcoat It")
            .description("Use Honey Glue to connect a group of blocks for assembly")
            .after(APPLIED_KINEMATICS)),

    I_DECLARE_THEE = create("i_declare_thee", b -> b
            .lazyIcon(() -> SimBlocks.NAMEPLATES.get(DyeColor.WHITE))
            .title("I Declare Thee...")
            .description("Name a Simulated Contraption using a Nameplate")
            .special(TaskType.NOISY)
            .after(NOT_GONNA_SUGARCOAT_IT)),

    MEASURE_ONCE_BUILD_TWICE = create("measure_once_build_twice", b -> b
            .icon(SimItems.CONTRAPTION_DIAGRAM)
            .title("Measure Once, Build Twice")
            .description("Inspect a Contraption Diagram")
            .after(NOT_GONNA_SUGARCOAT_IT)),

    // -----------------

    // Control
    GET_A_GRIP = create("get_a_grip", b -> b
            .icon(SimBlocks.IRON_HANDLE)
            .title("Get a Grip!")
            .description("Grab on to a Handle")
            .after(APPLIED_KINEMATICS)),

    GOT_A_GRIP = create("got_a_grip", b -> b
            .icon(SimBlocks.IRON_HANDLE)
            .title("Got a Grip!")
            .description("Break a very long fall by grabbing on to a Handle")
            .special(TaskType.SECRET)
            .after(GET_A_GRIP)),

    UNPOWERED_STEERING = create("unpowered_steering", b -> b
            .icon(SimBlocks.STEERING_WHEEL)
            .title("Unpowered Steering")
            .description("Grab and spin a Steering Wheel")
            .after(GET_A_GRIP)),

    STEAMLESS_ENGINE = create("steamless_engine", b -> b
            .lazyIcon(SimBlocks.RED_PORTABLE_ENGINE)
            .title("Steamless Engine")
            .description("Place and power a Portable Engine")
            .after(UNPOWERED_STEERING)),

    THAT_SHOULD_DO_FOR_NOW = create("that_should_do_for_now", b -> b
            .lazyIcon(SimBlocks.RED_PORTABLE_ENGINE)
            .title("That Should Do For Now")
            .description("Place over 10 hours of fuel into a Portable Engine")
            .special(TaskType.SECRET)
            .after(STEAMLESS_ENGINE)),

    WHAT_GOES_DOWN = create("what_goes_down", b -> b
            .icon(SimItems.SPRING)
            .title("What Goes Down...")
            .description("Boing! Obtain a Spring item")
            .whenIconCollected()
            .after(GET_A_GRIP)),

    MUST_COME_UP = create("must_come_up", b -> b
            .icon(SimItems.SPRING)
            .title("...Must Come Up")
            .description("Watch a Spring item bounce a great distance")
            .after(WHAT_GOES_DOWN)
            .special(TaskType.SECRET)),

    REWIND_TIME = create("rewind_time", b -> b
            .icon(SimBlocks.TORSION_SPRING)
            .title("Rewind Time")
            .description("Watch a Torsion Spring unwind to its original position")
            .after(WHAT_GOES_DOWN)),

    I_PAID_FOR_THE_WHOLE_TYPEWRITER = create("i_paid_for_the_whole_typewriter", b -> b
            .icon(SimBlocks.LINKED_TYPEWRITER)
            .title("I Paid for the Whole Typewriter")
            .description("Bind 26 or more keys to frequencies on the Linked Typewriter")
            .after(GET_A_GRIP)
            .special(TaskType.SECRET)),
    // -----------------

    // Redstone
    NO_PRESSURE = create("no_pressure", b -> b
            .icon(SimBlocks.ALTITUDE_SENSOR)
            .title("No Pressure")
            .description("Obtain and place an Altitude Sensor")
            .after(APPLIED_KINEMATICS)
            .whenIconPlaced()),

    CAN_WE_GET_MUCH_HIGHER = create("can_we_get_much_higher", b -> b
            .icon(SimBlocks.ALTITUDE_SENSOR)
            .title("Can We Get Much Higher?")
            .description("Observe an Altitude Sensor at 0% atmospheric pressure")
            .after(NO_PRESSURE)
            .special(TaskType.SECRET)),

    CONVOLUTED_CIRCUMVOLUTIONS = create("convoluted_circumvolutions", b -> b
            .icon(SimItems.GYRO_MECHANISM)
            .title("Convoluted Circumvolutions")
            .description("Obtain a Gyroscopic Mechanism")
            .after(NO_PRESSURE)
            .special(TaskType.NOISY)
            .whenIconCollected()),

    THE_DEFINITION_OF_UP = create("the_definition_of_up", b -> b
            .icon(SimBlocks.GIMBAL_SENSOR)
            .title("The Definition of \"Up\"")
            .description("Obtain and place a Gimbal Sensor to help you keep balance")
            .after(CONVOLUTED_CIRCUMVOLUTIONS)
            .whenIconPlaced()),

    THATAWAY = create("thataway", b -> b
            .icon(SimBlocks.NAVIGATION_TABLE)
            .title("Thataway!")
            .description("Obtain and place a Navigation Table to point you in the right direction")
            .whenIconPlaced()
            .special(TaskType.NOISY)
            .after(NO_PRESSURE)),

    FAR_FROM_HOME = create("far_from_home", b -> b
            .icon(SimBlocks.NAVIGATION_TABLE)
            .title("Far From Home")
            .description("Set a Navigation Table's target to a location over 5000 blocks away")
            .special(TaskType.SECRET)
            .after(THATAWAY)),

    SPEED_IS_KEY = create("speed_is_key", b -> b
            .icon(SimBlocks.VELOCITY_SENSOR)
            .title("Speed is Key")
            .description("Obtain and place a Velocity Sensor to satiate your need for speed")
            .after(NO_PRESSURE)
            .whenIconPlaced()),

    BIG_BEAM = create("big_beam", b -> b
            .icon(SimBlocks.LASER_POINTER)
            .title("Big Beam")
            .description("Power a Laser Pointer. Please do not stare directly into the Laser Pointer")
            .after(NO_PRESSURE)),

    NEARSIGHTED = create("nearsighted", b -> b
            .icon(SimBlocks.OPTICAL_SENSOR)
            .title("Nearsighted")
            .description("Obtain and place an Optical Sensor to show you what's right there")
            .after(BIG_BEAM)
            .whenIconPlaced()),

    MY_EYE = create("my_eye", b -> b
            .icon(SimBlocks.LASER_SENSOR)
            .title("My Eye!")
            .description("Shine a laser into a Laser Sensor and activate it")
            .after(BIG_BEAM)),

    CALL_OF_THE_VOID = create("call_of_the_void", b -> b
            .icon(Blocks.END_PORTAL_FRAME)
            .title("Call of the Void")
            .description("Visit a glimmering sea at the end of the world")
            .special(TaskType.SECRET)
            .after(APPLIED_KINEMATICS));

    // -----------------
    private final CompletableFuture<HolderLookup.Provider> registries;
    public SimAdvancements(final PackOutput output, final CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
    }

    public static void provideLang(final BiConsumer<String, String> consumer) {
        for (final SimulatedAdvancement advancement : SIM_ADVANCEMENTS) {
            advancement.provideLang(consumer);
        }
    }

    private static SimulatedAdvancement create(final String id, final UnaryOperator<SimulatedAdvancement.Builder> b) {
        final SimulatedAdvancement advancement = new SimulatedAdvancement(id, b, Simulated.path("gui/advancement"), Simulated.MOD_ID, SimAdvancementTriggers::addSimple);
        SIM_ADVANCEMENTS.add(advancement);
        return advancement;
    }

    public static void register() {
    }

    public List<SimulatedAdvancement> getAdvancementsArray() {
        return SIM_ADVANCEMENTS;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull final CachedOutput cachedOutput) {
        return this.registries.thenCompose(provider -> {
            final PackOutput.PathProvider pathProvider = this.output.createPathProvider(PackOutput.Target.DATA_PACK, "advancement");
            final List<CompletableFuture<?>> futures = new ArrayList<>();

            final Set<ResourceLocation> set = Sets.newHashSet();
            final Consumer<AdvancementHolder> consumer = (advancement) -> {
                final ResourceLocation id = advancement.id();
                if (!set.add(id)) {
                    throw new IllegalStateException("Duplicate advancement " + id);
                }
                final Path path = pathProvider.json(id);
                futures.add(DataProvider.saveStable(cachedOutput, provider, Advancement.CODEC, advancement.value(), path));
            };

            for (final SimulatedAdvancement advancement : this.getAdvancementsArray()) {
                advancement.save(consumer);
            }
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public @NotNull String getName() {
        return "Create Simulated's Advancements";
    }


}
