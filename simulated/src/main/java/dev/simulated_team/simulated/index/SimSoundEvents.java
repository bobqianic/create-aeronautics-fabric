package dev.simulated_team.simulated.index;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.api.sound.SimSoundEntry;
import dev.simulated_team.simulated.api.sound.SoundEventRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class SimSoundEvents {
    public static final SoundEventRegistry REGISTRY = new SoundEventRegistry(Simulated.MOD_ID);

    public static final String BLOCK_PLACED = "subtitles.block.generic.place";

    public static final SimSoundEntry
        // Block Sounds

        ABSORBER_EATS = REGISTRY.create("block.absorber.eats", definition -> definition
                .subtitle("Foul beast consumes wretched meal")
                .addFileVariants("block/absorber/foulbeast", 2)),

        PLUNGER_PLACE = REGISTRY.create("block.plunger.place", definition -> definition
                .defaultSubtitle(BLOCK_PLACED)
                .addFileVariants("block/plunger/place", 3)),

        ASSEMBLER_SHIFT = REGISTRY.create("block.physics_assembler.shift", definition -> definition
                .addFileVariant("block/physics_assembler/shift")),

        ASSEMBLER_TICK = REGISTRY.create("block.physics_assembler.tick", definition -> definition
                .subtitle("Physics Assembler clunks")
                .addFileVariants("block/physics_assembler/tick", 4)),

        ASSEMBLER_FAIL = REGISTRY.create("block.physics_assembler.fail", definition -> definition
                .subtitle("Physics Assembler fails")
                .addFileVariants("block/physics_assembler/fail", 3)),

        DOCKING_CONNECTOR_DOCKS = REGISTRY.create("block.docking_connector.dock", definition -> definition
                .subtitle("Docking Connector docks")
                .addFileVariants("block/docking_connector/dock", 4)),

        DOCKING_CONNECTOR_EXTENDS = REGISTRY.create("block.docking_connector.extend", definition -> definition
                .subtitle("Docking Connector extends")
                .addFileVariant("block/docking_connector/retract")),

        DOCKING_CONNECTOR_RETRACTS = REGISTRY.create("block.docking_connector.retract", definition -> definition
                .subtitle("Docking Connector retracts")
                .addFileVariant("block/docking_connector/retract")),

        ROPE_WINCH_STRETCH = REGISTRY.create("block.rope_winch.stretch", definition -> definition
                .subtitle("Rope Spool strains")
                .addFileVariants("block/rope_winch/rope_stretch", 3)),

        LINKED_TYPEWRITER_TAP = REGISTRY.create("block.linked_typewriter.tap", definition -> definition
                .subtitle("Linked Typewriter taps")
                .addFileVariants("block/linked_typewriter/tap", 5)),

        LINKED_TYPEWRITER_UNTAP = REGISTRY.create("block.linked_typewriter.untap", definition -> definition
                .addFileVariant("block/linked_typewriter/untap")),

        LINKED_TYPEWRITER_DING = REGISTRY.create("block.linked_typewriter.ding", definition -> definition
                .addFileVariant("block/linked_typewriter/ding")),

        SIMULATED_CONTRAPTION_MOVES = REGISTRY.create("block.physics_assembler.assemble", definition -> definition
                .subtitle("Simulated Contraption moves")
                .addFileVariants("block/physics_assembler/assemble", 5)),

        SIMULATED_CONTRAPTION_STOPS = REGISTRY.create("block.physics_assembler.disassemble", definition -> definition
                .subtitle("Simulated Contraption stops")
                .addFileVariant("block/physics_assembler/disassemble")),

        PORTABLE_ENGINE_AMBIENT = REGISTRY.create("block.portable_engine.ambient", definition -> definition
                .subtitle("Portable Engine crackles")
                .addFileVariants("block/portable_engine/ambient", 5)),

        PORTABLE_ENGINE_PUFF = REGISTRY.create("block.portable_engine.puff", definition -> definition
                .addFileVariants("block/portable_engine/puff", 5)),

        PORTABLE_ENGINE_ROARS = REGISTRY.create("block.portable_engine.activate", definition -> definition
                .subtitle("Portable Engine roars")
                .addEventVariant(SoundEvents.FIRECHARGE_USE)),

        AUGER_SHAFT_ENCASING = REGISTRY.create("block.auger_shaft.encase", definition -> definition
                .subtitle("Encasing Auger Shaft")
                .addEventVariant(SoundEvents.NETHERITE_BLOCK_HIT)),

        // Item Sounds

        PLUNGER_LAUNCH = REGISTRY.create("item.plunger_launcher.fwoomp", SoundSource.PLAYERS, definition -> definition
                .subtitle("Plunger Launcher fwoomps")
                .addFileVariant("item/plunger_launcher/fwoomp")),

        PLUNGER_RELEASE = REGISTRY.create("item.plunger_launcher.release", SoundSource.PLAYERS, definition -> definition
                .subtitle("Plunger Launcher releases")
                .addFileVariants("block/plunger/break", 4)),

        HONEY_ADDED = REGISTRY.create("item.honey_glue.use", SoundSource.PLAYERS, definition -> definition
                .subtitle("Honey squishes")
                .addEventVariant(SoundEvents.HONEY_BLOCK_PLACE)),

        STAFF_IDLE = REGISTRY.create("item.physics_staff.idle", SoundSource.PLAYERS, definition -> definition
                .addFileVariant("item/physics_staff/idle")),

            STAFF_IGNITE = REGISTRY.create("item.physics_staff.ignite", SoundSource.PLAYERS, definition -> definition
                .subtitle("Physics Staff ignites")
                .addFileVariants("item/physics_staff/ignite", 3)),

            STAFF_EXTINGUISH = REGISTRY.create("item.physics_staff.extinguish", SoundSource.PLAYERS, definition -> definition
                .subtitle("Physics Staff extinguishes")
                .addFileVariants("item/physics_staff/extinguish", 3)),

        STAFF_LOCK = REGISTRY.create("item.physics_staff.lock", SoundSource.PLAYERS, definition -> definition
                .subtitle("Physics Staff locks")
                .addFileVariants("item/physics_staff/lock", 3)),

        STAFF_UNLOCK = REGISTRY.create("item.physics_staff.unlock", SoundSource.PLAYERS, definition -> definition
                .subtitle("Physics Staff unlocks")
                .addFileVariants("item/physics_staff/lock", 3)),

        // GUI Sounds

        DIAGRAM_CHECKMARK = REGISTRY.create("gui.contraption_diagram.check", SoundSource.PLAYERS, definition -> definition
                .addFileVariant("gui/contraption_diagram/check")),

        DIAGRAM_ERASE = REGISTRY.create("gui.contraption_diagram.erase", SoundSource.PLAYERS, definition -> definition
                .addFileVariant("gui/contraption_diagram/erase")),

        DIAGRAM_TAP = REGISTRY.create("gui.contraption_diagram.tap", SoundSource.PLAYERS, definition -> definition
                .addFileVariants("gui/contraption_diagram/tap", 2));

    public static void init() {

    }
}
