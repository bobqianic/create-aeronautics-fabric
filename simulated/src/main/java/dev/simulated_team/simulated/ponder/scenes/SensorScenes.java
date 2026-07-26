package dev.simulated_team.simulated.ponder.scenes;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import com.zurrtum.create.client.foundation.ponder.element.ExpandedParrotElement;
import dev.simulated_team.simulated.content.blocks.altitude_sensor.AltitudeSensorBlock;
import dev.simulated_team.simulated.content.blocks.gimbal_sensor.GimbalSensorBlockEntity;
import dev.simulated_team.simulated.content.blocks.lasers.laser_pointer.LaserPointerBlock;
import dev.simulated_team.simulated.content.blocks.lasers.laser_pointer.LaserPointerBlockEntity;
import dev.simulated_team.simulated.content.blocks.lasers.laser_sensor.LaserSensorBlockEntity;
import dev.simulated_team.simulated.content.blocks.lasers.optical_sensor.OpticalSensorBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import dev.simulated_team.simulated.content.blocks.velocity_sensor.VelocitySensorBlock;
import dev.simulated_team.simulated.content.blocks.velocity_sensor.VelocitySensorBlockEntity;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.ponder.SceneScheduler;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.instructions.*;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.ParrotElement;
import com.zurrtum.create.client.ponder.api.element.ParrotPose;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.scene.*;
import com.zurrtum.create.client.ponder.foundation.element.ElementLinkImpl;
import com.zurrtum.create.client.ponder.foundation.instruction.CreateParrotInstruction;
import com.zurrtum.create.client.ponder.foundation.instruction.FadeOutOfSceneInstruction;
import com.zurrtum.create.client.ponder.foundation.instruction.RotateSceneInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.UnaryOperator;

public class SensorScenes {

    // Altitude Sensor

    private static void setAltitudeSensorRedstone(final SceneBuilder scene, final Selection nixie, final BlockPos redstone, final int power, final boolean particles) {
        scene.world().modifyBlockEntityNBT(nixie, NixieTubeBlockEntity.class,
                tag -> tag.putInt("RedstoneStrength", power), true);
        scene.world().modifyBlock(redstone,
                s -> s.setValue(RedStoneWireBlock.POWER, power), false);
        if (particles) {
            scene.effects().indicateRedstone(redstone);
        }
    }

    public static void altitudeSensorIntro(final SceneBuilder scene, final SceneBuildingUtil util) {
        scene.title("altitude_sensor_into", "Using the Altitude Sensor");
        scene.configureBasePlate(1, 1, 5);
        final Selection base = util.select().fromTo(2, 0, 2, 4, 1, 4);
        final Selection outer = util.select().fromTo(1, 0, 1, 5, 0, 5)
                .substract(util.select().fromTo(2, 0, 2, 4, 0, 4));
        final Selection clouds = util.select().fromTo(0, 2, 0, 7, 5, 8);

        final BlockPos sensorPos = util.grid().at(2, 1, 3);
        final Selection sensorSelection = util.select().position(2, 1, 3);
        final BlockPos redstonePos = util.grid().at(3, 1, 3);
        final Selection nixieSelection = util.select().position(4, 1, 3);

        final WorldInstructions world = scene.world();

        ElementLink<WorldSectionElement> outerSection = world.showIndependentSection(outer, Direction.UP);
        world.showSection(base.substract(sensorSelection), Direction.UP);
        scene.idle(10);

        world.showSection(sensorSelection, Direction.DOWN);
        scene.addInstruction(new AltitudeSensorVisualHeightInstruction.Linear(sensorPos, 0, -0.5f, -0.5f, f -> f));
        scene.idle(5);

        setAltitudeSensorRedstone(scene, nixieSelection, redstonePos, 5, true);
        scene.idle(5);

        scene.overlay().showText(90)
                .attachKeyFrame()
                .text("The Altitude Sensor outputs a redstone signal based on its elevation")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(sensorPos, Direction.WEST));
        scene.idle(100);

        scene.overlay().showText(75)
                .attachKeyFrame()
                .text("By default, travelling upwards increases the signal strength")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(sensorPos, Direction.WEST));
        scene.idle(30);

        final ElementLink<WorldSectionElement> cloudsSection;
        {
            final SceneScheduler scheduler = new SceneScheduler(scene);

            final SceneScheduler.Sequence seqC = scheduler.get(0);

            seqC.addInstruction(CustomAnimateWorldSectionInstruction.move(outerSection,
                    new Vec3(0, -4, 0), 30, SmoothMovementUtils.quadraticRise()));
            seqC.addInstruction(CustomMoveBaseShadowInstruction.delta(
                    new Vec3(0, -4, 0), 30, SmoothMovementUtils.quadraticRise()));
            seqC.addInstruction(new AltitudeSensorVisualHeightInstruction.Linear(
                    sensorPos, 60, -0.5f, 0f, SmoothMovementUtils.cubicSmoothing())
            );
            seqC.idle(15);

            seqC.world().hideIndependentSection(outerSection, Direction.DOWN);
            seqC.addInstruction(new CustomToggleBaseShadowInstruction());
            seqC.idle(15);

            cloudsSection = seqC.world().showIndependentSection(clouds, Direction.DOWN);
            seqC.world().moveSection(cloudsSection, new Vec3(0, 2, 0), 0);
            seqC.addInstruction(CustomAnimateWorldSectionInstruction.move(cloudsSection,
                    new Vec3(0, -4, 0), 30, SmoothMovementUtils.quadraticRiseDual()));
            seqC.idle(30);

            final SceneScheduler.Sequence seqR = scheduler.get(1);

            for (int i = 6; i <= 10; i++) {
                seqR.idle(seqC.getDuration() / 5);
                setAltitudeSensorRedstone(seqR, nixieSelection, redstonePos, i, i == 10);
            }

            scheduler.run();
        }
        scene.idle(35);

        scene.overlay().showControls(util.vector().topOf(sensorPos), Pointing.DOWN, 60).rightClick();
        scene.overlay().showText(60)
                .attachKeyFrame()
                .text("The minimum and maximum altitudes can be changed in the UI")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(sensorPos, Direction.WEST));
        scene.idle(80);

        scene.overlay().showText(80)
                .attachKeyFrame()
                .text("It can also be set to decrease in strength as it travels upwards")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(sensorPos, Direction.WEST));
        scene.idle(50);

        setAltitudeSensorRedstone(scene, nixieSelection, redstonePos, 5, true);
        scene.idle(50);

        // THE CLOUDS DO A LITTLE WIGGLE :3
        // not anymore 3:

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(cloudsSection,
                new Vec3(0, -1, 0), 30, SmoothMovementUtils.quadraticRiseInOut()));
        scene.addInstruction(new AltitudeSensorVisualHeightInstruction.Linear(sensorPos, 30, 0f, 0.33f, SmoothMovementUtils.quadraticRiseInOut()));
        scene.idle(30);

        setAltitudeSensorRedstone(scene, nixieSelection, redstonePos, 4, true);
        scene.idle(30);

        scene.overlay().showControls(util.vector().blockSurface(sensorPos, Direction.EAST), Pointing.RIGHT, 60).withItem(AllItems.GOGGLES.getDefaultInstance());
        scene.overlay().showText(110)
                .attachKeyFrame()
                .text("When wearing Engineers' Goggles, information about air pressure and exact altitude can be seen")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(sensorPos, Direction.WEST));
        scene.idle(130);

        scene.overlay().showText(75)
                .attachKeyFrame()
                .text("A Wrench can also be used to see more precise changes in elevation")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(sensorPos, Direction.WEST));
        scene.overlay().showControls(util.vector().blockSurface(sensorPos, Direction.EAST), Pointing.RIGHT, 30).withItem(AllItems.WRENCH.getDefaultInstance());
        scene.idle(20);
        world.modifyBlock(sensorPos,
                state -> state.setValue(AltitudeSensorBlock.DIAL, AltitudeSensorBlock.FaceType.RADIAL), true);
        scene.addInstruction(new AltitudeSensorVisualHeightInstruction.Radial(sensorPos, 0, 10, 10, f -> f));
        scene.idle(75);

        {
            final SceneScheduler scheduler = new SceneScheduler(scene);

            final SceneScheduler.Sequence seqC = scheduler.get(0);

            seqC.addInstruction(CustomAnimateWorldSectionInstruction.move(cloudsSection,
                    new Vec3(0, 4, 0), 30, SmoothMovementUtils.quadraticRise()));
            seqC.idle(15);

            seqC.world().hideIndependentSection(cloudsSection, Direction.UP);
            seqC.idle(15);

            outerSection = seqC.world().showIndependentSection(outer, Direction.UP);
            seqC.world().moveSection(outerSection, new Vec3(0, -4, 0), 0);
            seqC.addInstruction(CustomAnimateWorldSectionInstruction.move(outerSection,
                    new Vec3(0, 4, 0), 30, SmoothMovementUtils.quadraticRiseDual()));
            seqC.addInstruction(new CustomToggleBaseShadowInstruction());
            seqC.addInstruction(CustomMoveBaseShadowInstruction.delta(
                    new Vec3(0, 4, 0), 30, SmoothMovementUtils.quadraticRiseDual()));
            seqC.idle(20);

            seqC.idle(10);

            final SceneScheduler.Sequence seqR = scheduler.get(1);

            seqR.addInstruction(new AltitudeSensorVisualHeightInstruction.Radial(
                    sensorPos, 60, 10, 1, SmoothMovementUtils.quadraticRiseInOut()));
            setAltitudeSensorRedstone(seqR, nixieSelection, redstonePos, 5, false);
            for (int i = 6; i <= 10; i++) {
                seqR.idle(seqC.getDuration() / 5);
                setAltitudeSensorRedstone(seqR, nixieSelection, redstonePos, i, i == 10);
            }

            scheduler.run();
        }
    }

    //

    // Optical Sensor

    private static void opticalSensorSetHitPos(final WorldInstructions world, final BlockPos hitBlockPos, final BlockPos lp, final Direction laserDir, final float scale) {
        final Vec3 newHit = Vec3.atCenterOf(hitBlockPos).add(Vec3.atLowerCornerOf(laserDir.getOpposite().getUnitVec3i()).scale(scale));
        world.modifyBlockEntity(lp, OpticalSensorBlockEntity.class, laser -> laser.laser.setVirtualHitPos(newHit));
    }

    public static void opticalSensor(final SceneBuilder scene, final SceneBuildingUtil util) {
        scene.title("optical_sensor", "Using Optical Sensors");
        scene.configureBasePlate(0, 0, 5);
        final WorldInstructions world = scene.world();
        world.showSection(util.select().layer(0), Direction.UP);

        final BlockPos sensor1Pos = new BlockPos(3, 1, 1);
        final BlockPos trapdoorPos = new BlockPos(1, 1, 1);
        final BlockPos nixieTubeL = new BlockPos(3, 2, 3);
        final BlockPos nixieTubeR = new BlockPos(1, 2, 3);

        final Selection sensor1 = util.select().fromTo(1, 1, 1, 3, 1, 1);
        final Selection sensors2 = util.select().fromTo(1, 1, 3, 3, 1, 3);
        final Selection sensorNixieTubes = util.select().fromTo(1, 2, 3, 3, 2, 3);
        final Selection sensorTargets = util.select().fromTo(1, 1, 0, 3, 1, 1);
        final Selection redstone = util.select().position(3, 1, 2);
        final Selection redstoneLampSelection = util.select().position(3, 1, 0);

        final Vec3 leftSlot = new Vec3(3.5, 2, 3.5);
        final Vec3 rightSlot = new Vec3(1.5, 2, 3.5);

        world.showSection(util.select().position(3, 1, 1), Direction.DOWN);
        world.showSection(util.select().position(1, 1, 1), Direction.DOWN);

        scene.idle(10);

        world.showSection(redstone, Direction.DOWN);

        final ElementLink<WorldSectionElement> redstoneLamp = world.showIndependentSection(redstoneLampSelection, Direction.DOWN);
        world.moveSection(redstoneLamp, new Vec3(0, 0, 3), 0);

        scene.idle(20);
        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Optical Sensors can detect blocks which obstruct its laser pointer")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(sensor1Pos, Direction.UP));
        scene.idle(70);
        world.toggleRedstonePower(sensor1);
        world.toggleRedstonePower(redstone);

        world.toggleRedstonePower(redstoneLampSelection);

        world.modifyBlock(trapdoorPos, s -> s.setValue(TrapDoorBlock.OPEN, true), false);
        final EffectInstructions effects = scene.effects();
        effects.indicateRedstone(new BlockPos(3, 1, 1));
        final BlockPos leftSensor = new BlockPos(3, 1, 3);
        effects.indicateRedstone(leftSensor);
        opticalSensorSetHitPos(world, trapdoorPos, sensor1Pos, Direction.WEST, 3 / 16f);
        scene.idle(20);
        scene.overlay().showText(50)
                .text("When a block is detected, a Redstone signal is emitted")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(sensor1Pos, Direction.UP));
        scene.idle(50);
        world.hideSection(sensor1, Direction.UP);
        world.hideSection(redstone, Direction.UP);
        world.hideIndependentSection(redstoneLamp, Direction.UP);
        scene.idle(20);

        world.replaceBlocks(util.select().position(1, 1, 1), Blocks.AIR.defaultBlockState(), false);
        world.replaceBlocks(util.select().position(3, 1, 0), Blocks.AIR.defaultBlockState(), false);
        world.replaceBlocks(util.select().position(3, 1, 2), Blocks.AIR.defaultBlockState(), false);

        world.replaceBlocks(util.select().position(3, 1, 1), Blocks.OAK_TRAPDOOR.defaultBlockState(), false);

        world.replaceBlocks(util.select().position(3, 1, 3), SimBlocks.OPTICAL_SENSOR.getDefaultState(), false);

        world.toggleRedstonePower(util.select().position(3, 1, 3));

        world.modifyBlockEntityNBT(util.select().position(nixieTubeL), NixieTubeBlockEntity.class,
                nbt -> nbt.putInt("RedstoneStrength", 0));

        world.showSection(sensors2, Direction.DOWN);

        scene.idle(10);

        world.showSection(sensorNixieTubes, Direction.DOWN);

        scene.idle(20);
        world.setBlock(new BlockPos(3, 1, 1), Blocks.IRON_BLOCK.defaultBlockState(), false);
        opticalSensorSetHitPos(world, new BlockPos(3, 1, 1), leftSensor, Direction.NORTH, 0.5f);
        world.setBlock(new BlockPos(1, 1, 0), Blocks.IRON_BLOCK.defaultBlockState(), false);
        final BlockPos rightSensor = new BlockPos(1, 1, 3);
        opticalSensorSetHitPos(world, new BlockPos(1, 1, 0), rightSensor, Direction.NORTH, 0.5f);
        world.showSection(sensorTargets.substract(redstoneLampSelection), Direction.DOWN);
        world.toggleRedstonePower(sensors2);
        effects.indicateRedstone(leftSensor);
        effects.indicateRedstone(rightSensor);
        world.modifyBlockEntityNBT(util.select().position(nixieTubeL), NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 14));
        world.modifyBlockEntityNBT(util.select().position(nixieTubeR), NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 13));

        scene.idle(20);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .text("The Optical Sensor's signal strength is relative to its detected block's distance")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(nixieTubeR, Direction.WEST));

        scene.idle(100);

        scene.rotateCameraY(-100);

        scene.idle(20);

        scene.overlay().showText(80)
                .attachKeyFrame()
                .text("The maximum distance can be configured using the value panel")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(rightSensor, Direction.SOUTH));

        scene.idle(40);

        scene.overlay().showControls(util.vector().blockSurface(rightSensor, Direction.SOUTH), Pointing.DOWN, 20).rightClick();
        scene.overlay().showScrollInput(util.vector().blockSurface(rightSensor, Direction.SOUTH), Direction.SOUTH, 20);

        scene.idle(5);

        effects.indicateRedstone(nixieTubeR);
        world.modifyBlockEntityNBT(util.select().position(nixieTubeR), NixieTubeBlockEntity.class, nbt -> nbt.putInt("RedstoneStrength", 7));

        scene.idle(55);

        scene.rotateCameraY(100);

        scene.idle(20);

        world.hideSection(util.select().position(nixieTubeL).add(util.select().position(nixieTubeR)), Direction.UP);
        scene.idle(20);

        scene.overlay().showText(30)
                .attachKeyFrame()
                .text("When a filter is applied...")
                .placeNearTarget()
                .pointAt(rightSlot);

        scene.overlay().showFilterSlotInput(leftSlot, Direction.UP, 100);
        scene.overlay().showFilterSlotInput(rightSlot, Direction.UP, 100);

        final ItemStack iron = new ItemStack(Items.IRON_BLOCK);
        final ItemStack gold = new ItemStack(Items.GOLD_BLOCK);

        scene.overlay().showControls(leftSlot, Pointing.DOWN, 40).withItem(gold);
        world.toggleRedstonePower(util.select().position(3, 1, 3));

        world.modifyBlockEntityNBT(util.select().position(3, 1, 3), OpticalSensorBlockEntity.class,
                nbt -> nbt.store("Filter", ItemStack.OPTIONAL_CODEC, gold));

        scene.idle(20);

        scene.overlay().showControls(rightSlot, Pointing.DOWN, 40).withItem(iron);

        world.modifyBlockEntityNBT(util.select().position(1, 1, 3), OpticalSensorBlockEntity.class,
                nbt -> nbt.store("Filter", ItemStack.OPTIONAL_CODEC, iron));

        scene.idle(60);

        scene.overlay().showText(50)
                .text("...only matching blocks will emit a signal")
                .pointAt(util.vector().blockSurface(nixieTubeL, Direction.DOWN));

        scene.idle(70);
        world.replaceBlocks(util.select().position(3, 1, 1), Blocks.GOLD_BLOCK.defaultBlockState(), true);
        world.toggleRedstonePower(util.select().position(3, 1, 3));
        effects.indicateRedstone(leftSensor);
    }

    //

    // Laser Sensor & Pointer

    private static void laserSetRedstone(final int power, final WorldInstructions world, final BlockPos sensorNixiePos, final BlockPos sensorRedstonePos) {
        world.modifyBlock(sensorRedstonePos, s -> s.setValue(RedStoneWireBlock.POWER, power), false);
        world.modifyBlockEntity(sensorNixiePos, NixieTubeBlockEntity.class, be -> {
            be.updateRedstoneStrength(power);
            be.updateDisplayedStrings();
        });
    }

    private static void laserSetHitPos(final WorldInstructions world, final BlockPos hitBlockPos, final BlockPos lp, final Direction laserDir) {
        final Vec3 newHit = Vec3.atCenterOf(hitBlockPos).add(Vec3.atLowerCornerOf(laserDir.getOpposite().getUnitVec3i()).scale(0.5f));
        world.modifyBlockEntity(lp, LaserPointerBlockEntity.class, laser -> laser.sensorInteraction.setVirtualHitPos(newHit));
    }

    public static void lasers(final SceneBuilder scene, final SceneBuildingUtil util) {
        scene.title("laser_pointer", "Using Laser Pointers and Laser Sensors");
        scene.configureBasePlate(0, 0, 5);
        final SelectionUtil select = util.select();
        final WorldInstructions world = scene.world();
        final OverlayInstructions overlay = scene.overlay();
        scene.showBasePlate();

        //Laser Pointer
        final BlockPos laserPointerPos = new BlockPos(3, 1, 4);
        final Selection laserPointer = select.position(laserPointerPos);
        final Direction laserPointerDir = Direction.NORTH;

        //Redstone
        final BlockPos analogLeverPos = new BlockPos(1, 1, 4);
        final BlockPos redstoneWirePos = new BlockPos(2, 1, 4);
        final BlockPos redstoneNixiePos = new BlockPos(0, 1, 4);
        final Selection redstone = select.fromTo(redstoneWirePos, redstoneNixiePos);

        // Laser sensor
        final BlockPos laserSensorPos = new BlockPos(3, 1, 0);
        final BlockPos laserNixiePos = new BlockPos(1, 1, 0);
        final BlockPos laserRedstonePos = new BlockPos(2, 1, 0);
        final Selection laserSensor = select.fromTo(laserSensorPos, laserNixiePos);
        final Vec3 sensorFilterPos = util.vector().blockSurface(laserSensorPos, Direction.UP);

        final BlockPos laserPos = new BlockPos(3, 1, 2);

        //show redstone and LP
        world.showSection(laserPointer, Direction.DOWN);
        scene.idle(10);
        world.showSection(redstone, Direction.EAST);
        scene.idle(20);

        //power redstone and LP
        world.toggleRedstonePower(redstone);
        world.toggleRedstonePower(laserPointer);
        for (int i = 0; i < 15; i++) {
            final int state = i + 1;
            world.modifyBlockEntityNBT(select.position(analogLeverPos), AnalogLeverBlockEntity.class, nbt -> nbt.putInt("State", state));
            laserSetRedstone(state, world, redstoneNixiePos, redstoneWirePos);
            scene.idle(3);
        }
        scene.idle(10);
        overlay.showText(50)
                .attachKeyFrame()
                .text("Laser pointers emit a laser when powered")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(laserPointerPos, Direction.UP));
        scene.idle(60);

        //Obstruct laser
        final Selection ironBlock = select.position(3, 1, 2);
        world.showSection(ironBlock, Direction.UP);
        laserSetHitPos(world, new BlockPos(3, 1, 2), laserPointerPos, laserPointerDir);
        scene.idle(30);
        overlay.showText(50)
                .attachKeyFrame()
                .text("The laser can be obstructed by blocks")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(new BlockPos(3, 1, 2), Direction.UP));
        scene.idle(60);

        //spawn laser sensor and show functionality
        world.showSection(laserSensor, Direction.WEST);
        scene.idle(20);
        world.replaceBlocks(ironBlock, Blocks.AIR.defaultBlockState(), true);
        laserSetHitPos(world, laserSensorPos, laserPointerPos, laserPointerDir);
        world.toggleRedstonePower(laserSensor);
        laserSetRedstone(15, world, laserNixiePos, laserRedstonePos);
        scene.idle(20);
        overlay.showText(50)
                .attachKeyFrame()
                .text("Laser Sensors can detect the laser when unobstructed")
                .placeNearTarget()
                .pointAt(sensorFilterPos);
        scene.idle(70);

        //Show Laser sensor changing signal strength
        overlay.showText(50)
                .text("They will emit a signal dependent on the laser power")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(sensorFilterPos);
        scene.idle(60);
        for (int i = 1; i < 10; i++) {
            final int state = 15 - i;
            world.modifyBlockEntityNBT(select.position(analogLeverPos), AnalogLeverBlockEntity.class, nbt -> nbt.putInt("State", state));
            laserSetRedstone(state, world, redstoneNixiePos, redstoneWirePos);
            laserSetRedstone(state, world, laserNixiePos, laserRedstonePos);
            scene.idle(3);
        }

        scene.idle(30);

        scene.overlay().showText(30)
                .attachKeyFrame()
                .text("When a filter is applied...")
                .placeNearTarget()
                .pointAt(sensorFilterPos);

        scene.overlay().showFilterSlotInput(sensorFilterPos, Direction.UP, 45);

        final ItemStack dye = new ItemStack(Items.RED_DYE);

        scene.overlay().showControls(sensorFilterPos, Pointing.DOWN, 40).withItem(dye);

        world.modifyBlockEntityNBT(laserSensor, LaserSensorBlockEntity.class, nbt -> nbt.store("Filter", ItemStack.OPTIONAL_CODEC, dye));
        world.toggleRedstonePower(laserSensor);
        laserSetRedstone(0, world, laserNixiePos, laserRedstonePos);

        scene.idle(50);

        scene.overlay().showText(80)
                .text("...only lasers with matching colors will activate the sensor")
                .pointAt(util.vector().centerOf(laserPos));

        scene.idle(50);

        scene.overlay().showControls(util.vector().topOf(laserPointerPos), Pointing.DOWN, 20).withItem(dye);
        world.modifyBlockEntity(laserPointerPos, LaserPointerBlockEntity.class,
                laser -> laser.setLaserColor(((DyeItem) dye.getItem()).getDyeColor().getTextColor()));

        world.toggleRedstonePower(laserSensor);
        laserSetRedstone(6, world, laserNixiePos, laserRedstonePos);
        scene.idle(50);

        scene.addKeyframe();
        scene.overlay().showControls(util.vector().blockSurface(laserPointerPos, Direction.NORTH), Pointing.RIGHT, 20).withItem(AllItems.WRENCH.getDefaultInstance());
        scene.idle(10);
        world.cycleBlockProperty(laserPointerPos, LaserPointerBlock.INVERTED);
        laserSetRedstone(9, world, laserNixiePos, laserRedstonePos);
        scene.idle(20);
        scene.overlay().showText(80)
                .text("Right clicking the front or back face with a wrench inverts redstone power")
                .pointAt(util.vector().centerOf(laserPointerPos));

        scene.idle(100);
        for (int i = 5; i >= 0; i--) {
            final int finalI = i;
            world.modifyBlockEntityNBT(select.position(analogLeverPos), AnalogLeverBlockEntity.class, nbt -> nbt.putInt("State", finalI));
            laserSetRedstone(i, world, redstoneNixiePos, redstoneWirePos);
            laserSetRedstone(15 - i, world, laserNixiePos, laserRedstonePos);
            scene.idle(3);
        }
        scene.effects().indicateRedstone(laserNixiePos);

        scene.idle(40);

        world.hideSection(laserSensor, Direction.WEST);
        scene.overlay().showText(80)
                .text("The max casting distance can be adjusted on the back")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(laserPointerPos));
        scene.idle(40);

        scene.overlay().showControls(util.vector().blockSurface(laserPointerPos, Direction.SOUTH), Pointing.DOWN, 20).rightClick();
        scene.idle(10);
        world.modifyBlockEntityNBT(laserPointer, LaserPointerBlockEntity.class, nbt -> nbt.putInt("ScrollValue", 2));
        laserSetHitPos(world, new BlockPos(3, 1, 2), laserPointerPos, laserPointerDir);
        scene.idle(30);

        scene.overlay().showControls(util.vector().blockSurface(laserPointerPos, Direction.SOUTH), Pointing.DOWN, 20).rightClick();
        scene.idle(10);
        world.modifyBlockEntityNBT(laserPointer, LaserPointerBlockEntity.class, nbt -> nbt.putInt("ScrollValue", 8));
        laserSetHitPos(world, new BlockPos(3, 1, -4), laserPointerPos, laserPointerDir);
        scene.markAsFinished();

        scene.idle(100);

        scene.overlay().showControls(util.vector().blockSurface(laserPointerPos, Direction.NORTH), Pointing.RIGHT, 20).withItem(Items.NETHER_STAR.getDefaultInstance());
        scene.idle(10);
        world.modifyBlockEntity(laserPointerPos, LaserPointerBlockEntity.class, laser -> laser.setRainbow(true));
    }

    // Gimbal Sensor

    public static void gimbalSensorPowerRedstoneLamp(final SceneBuilder scene, final SceneBuildingUtil util, final Integer index, final Boolean power) {
        final BlockPos[] wireLocations = new BlockPos[]{util.grid().at(1, 3, 2), util.grid().at(2, 3, 3), util.grid().at(3, 3, 2), util.grid().at(2, 3, 1)};
        final BlockPos[] lampLocations = new BlockPos[]{util.grid().at(0, 2, 2), util.grid().at(2, 2, 4), util.grid().at(4, 2, 2), util.grid().at(2, 2, 0)};
        final Direction[] directions = new Direction[]{Direction.WEST, Direction.SOUTH, Direction.EAST, Direction.NORTH};
        final BlockPos gimbalLocation = new BlockPos(2, 3, 2);

        scene.world().modifyBlockEntityNBT(util.select().position(gimbalLocation), GimbalSensorBlockEntity.class,
            nbt -> {
                final CompoundTag powers = nbt.getCompoundOrEmpty("Powers");
                powers.putInt(directions[index].getName(), power ? 15 : 0);
                nbt.put("Powers", powers);
            });

        scene.world().toggleRedstonePower(util.select().position(lampLocations[index]));
        scene.world().modifyBlock(wireLocations[index], s -> s.setValue(RedStoneWireBlock.POWER, power ? 5 : 0), false);
        if (power) {
            scene.effects().indicateRedstone(lampLocations[index]);
        }
    }

    public static void gimbalSensorRepositionSelection(final SceneBuilder scene, final ElementLink<WorldSectionElement> selection, final Vec3 position, final Vec3 rotation) {
        scene.world().moveSection(selection, position, 0);

        scene.world().configureCenterOfRotation(selection, new Vec3(2.5, 2.5, 2.5));
        scene.world().rotateSection(selection, rotation.x, rotation.y, rotation.z, 0);
    }

    public static void gimbalSensorModifyNixieTube(final SceneBuilder scene, final SceneBuildingUtil util, final int index, final Integer strength, final boolean particles) {
        final BlockPos[] nixieTubeLocations = new BlockPos[]{util.grid().at(0, 3, 2), util.grid().at(2, 3, 4), util.grid().at(4, 3, 2), util.grid().at(2, 3, 0)};
        final Direction[] directions = new Direction[]{Direction.WEST, Direction.SOUTH, Direction.EAST, Direction.NORTH};
        final BlockPos gimbalLocation = new BlockPos(2, 3, 2);

        scene.world().modifyBlockEntityNBT(util.select().position(nixieTubeLocations[index]), NixieTubeBlockEntity.class,
                nixie -> nixie.putInt("RedstoneStrength", strength)
        );
        scene.world().modifyBlockEntityNBT(util.select().position(gimbalLocation), GimbalSensorBlockEntity.class,
                nbt -> {
                    final CompoundTag powers = nbt.getCompoundOrEmpty("Powers");
                    powers.putInt(directions[index].getName(), strength == 0 ? 0 : 15);
                    nbt.put("Powers", powers);
                });
        if (particles) {
            scene.effects().indicateRedstone(nixieTubeLocations[index]);
        }
    }

    public static void gimbalSensorModifyRedstoneDust(final SceneBuilder scene, final BlockPos pos, final Integer strength) {
        scene.world().modifyBlock(pos, s -> s.setValue(RedStoneWireBlock.POWER, strength), false);
    }

    public static void setGimbalAngle(final SceneBuilder scene, final SceneBuildingUtil util, final BlockPos pos, final Float angle1, final Float angle2) {
        scene.world().modifyBlockEntityNBT(util.select().position(pos), GimbalSensorBlockEntity.class,
                nbt -> {
                    nbt.putFloat("Angle1", angle1);
                    nbt.putFloat("Angle2", angle2);
                });
    }

    public static void gimbalSensor(final SceneBuilder scene, final SceneBuildingUtil util) {
        scene.title("gimbal_sensor_intro", "Using the Gimbal Sensor");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        final WorldInstructions world = scene.world();


        final BlockPos gimbalSensor = new BlockPos(2, 3, 2);
        final BlockPos[] wireLocations = new BlockPos[]{util.grid().at(1, 3, 2), util.grid().at(2, 3, 3), util.grid().at(3, 3, 2), util.grid().at(2, 3, 1)};
        final BlockPos[] lampLocations = new BlockPos[]{util.grid().at(0, 2, 2), util.grid().at(2, 2, 4), util.grid().at(4, 2, 2), util.grid().at(2, 2, 0)};

        final Selection redstoneLamps = util.select().position(lampLocations[0]).add(util.select().position(lampLocations[1])).add(util.select().position(lampLocations[2])).add(util.select().position(lampLocations[3]));
        final Selection centralRedstone = util.select().fromTo(1, 3, 1, 3, 3, 3);
        final Selection fullPlatform = util.select().fromTo(0, 2, 0, 4, 2, 4).add(centralRedstone);
        final Selection lamplessPlatform = util.select().fromTo(0, 2, 0, 4, 2, 4).add(centralRedstone).substract(redstoneLamps);

        final Selection nixieTubesX = util.select().position(util.grid().at(0, 3, 2)).add(util.select().position(util.grid().at(4, 3, 2)));
        final Selection nixieTubesZ = util.select().position(util.grid().at(2, 3, 4)).add(util.select().position(util.grid().at(2, 3, 0)));

        scene.idle(10);

        ElementLink<WorldSectionElement> platformSelection = world.showIndependentSection(fullPlatform, Direction.DOWN);
        //repositionSelection(scene, platformSelection, Vec3.ZERO, Vec3.ZERO);

        world.moveSection(platformSelection, new Vec3(0, -1, 0), 0);

        scene.idle(30);

        scene.overlay().showText(90)
                .text("The Gimbal Sensor outputs a redstone signal based on its orientation")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 2, 2));

        scene.idle(100);

        scene.addKeyframe();

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(platformSelection, new Vec3(0, 0.5, 0), 30, SmoothMovementUtils.cubicSmoothing()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(platformSelection, new Vec3(-5, 0, 0), 40, SmoothMovementUtils.cubicSmoothing()));

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(platformSelection, new Vec3(0, 0, 4), 20, SmoothMovementUtils.cubicRise()));

        scene.idle(20);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(platformSelection, new Vec3(0, 0, -4), 80, SmoothMovementUtils.cubicRise()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(platformSelection, new Vec3(0, 0, -0.025), 80, SmoothMovementUtils.cubicSmoothing()));

        for (int i = 0; i < 80; i++) {
            world.rotateSection(platformSelection, Math.sin(Math.toRadians(4 * i)) / 2, 0, Math.cos(Math.toRadians(4 * i)) / 2, 1);
            scene.idle(1);
            setGimbalAngle(scene, util, gimbalSensor, (float) Math.sin(Math.toRadians(2 * i)) / 4, (float) -Math.sin(Math.toRadians(4 * i)) / 4);

            if (i == 0) {
                gimbalSensorPowerRedstoneLamp(scene, util, 0, true);
            }
            if (i == 20) {
                gimbalSensorPowerRedstoneLamp(scene, util, 1, true);
                scene.overlay().showText(90)
                        .text("By default, the signal is emitted downhill")
                        .placeNearTarget()
                        .pointAt(util.vector().centerOf(1, 2, 2));
            }
            if (i == 50) {
                gimbalSensorPowerRedstoneLamp(scene, util, 0, false);
                gimbalSensorPowerRedstoneLamp(scene, util, 2, true);
            }
        }

        gimbalSensorPowerRedstoneLamp(scene, util, 1, false);
        gimbalSensorPowerRedstoneLamp(scene, util, 3, true);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(platformSelection, new Vec3(-2, 0, 4.5), 30, SmoothMovementUtils.quadraticRiseOut()));
        scene.addInstruction(new GimbalSensorVisualRotationInstruction(gimbalSensor, false));

        for (int i = 0; i < 30; i++) {
            scene.idle(1);
            final int finalI = i / 3 + 80;
            if (i == 25) {
                gimbalSensorPowerRedstoneLamp(scene, util, 2, false);
            }
            setGimbalAngle(scene, util, gimbalSensor, -0.3f * i / 30 + (float) Math.cos(Math.toRadians(2 * finalI) - Math.PI / 2) / 4, (float) -Math.sin(Math.toRadians(4 * finalI)) / 4);
        }

        scene.idle(20);

        // Silently separate lamps from main platform
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, platformSelection));

        platformSelection = world.showIndependentSectionImmediately(lamplessPlatform);
        ElementLink<WorldSectionElement> redstoneLampSelection = world.showIndependentSectionImmediately(redstoneLamps);

        gimbalSensorRepositionSelection(scene, platformSelection, new Vec3(0, -0.5, 0), new Vec3(-5, 0, 0));
        gimbalSensorRepositionSelection(scene, redstoneLampSelection, new Vec3(0, -0.5, 0), new Vec3(-5, 0, 0));

        world.hideIndependentSection(redstoneLampSelection, Direction.UP);

        scene.idle(10);
        scene.addInstruction(new RotateSceneInstruction(35, -55, true));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(platformSelection, new Vec3(-11, 0, 0), 30, SmoothMovementUtils.quadraticRiseInOut()));
        scene.idle(10);

        world.replaceBlocks(redstoneLamps, AllBlocks.ANDESITE_SCAFFOLD.defaultBlockState().setValue(ScaffoldingBlock.BOTTOM, true), false);

        redstoneLampSelection = world.showIndependentSection(redstoneLamps, Direction.DOWN);
        final ElementLink<WorldSectionElement> nixieTubesZSelection = world.showIndependentSection(nixieTubesZ, Direction.DOWN);
        gimbalSensorRepositionSelection(scene, redstoneLampSelection, new Vec3(0, -0.5, 0), new Vec3(-15, 0, 0));
        gimbalSensorRepositionSelection(scene, nixieTubesZSelection, new Vec3(0, -0.5, 0), new Vec3(-15, 0, 0));
        gimbalSensorModifyNixieTube(scene, util, 3, 4, false);


        scene.idle(15);

        // Silently merge new scaffolding with main platform
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, platformSelection));
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, redstoneLampSelection));

        platformSelection = world.showIndependentSectionImmediately(fullPlatform);
        gimbalSensorRepositionSelection(scene, platformSelection, new Vec3(0, -0.5, 0), new Vec3(-15, 0, 0));

        scene.idle(20);

        final Vec3 valueSlotLeft = new Vec3(2, 2.97, 2.245);

        scene.overlay().showText(80)
                .text("The value boxes can configure the angle of maximum signal...")
                .colored(PonderPalette.BLUE)
                .placeNearTarget()
                .pointAt(valueSlotLeft)
                .attachKeyFrame();

        scene.idle(32);


        scene.overlay().showControls(valueSlotLeft.add(0, 0.1, 0), Pointing.DOWN, 20).rightClick();
        scene.addInstruction(new ColoredValueSlotInstruction(scene, valueSlotLeft, Direction.WEST, new Vec3(15, 0, 0), PonderPalette.INPUT, 20));

        scene.idle(8);

        gimbalSensorModifyNixieTube(scene, util, 3, 8, true);

        scene.idle(50);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(platformSelection, new Vec3(30, 0, 0), 70, SmoothMovementUtils.quadraticRiseInOut()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(nixieTubesZSelection, new Vec3(30, 0, 0), 70, SmoothMovementUtils.quadraticRiseInOut()));

        for (int i = 0; i < 70; i++) {
            setGimbalAngle(scene, util, gimbalSensor, (float) Math.toRadians(i - 35) * (3f / 7f), 0.0f);
            gimbalSensorModifyNixieTube(scene, util, 1, Mth.floor(Math.max(0, (i - 30) * 8 / 35)), false);
            gimbalSensorModifyNixieTube(scene, util, 3, Mth.floor(Math.max(0, (35 - i) * 8 / 35)), false);
            scene.idle(1);
        }

        scene.idle(12);

        final Vec3 valueSlotRight = valueSlotLeft.add(0, 0, 0.51);

        scene.overlay().showControls(valueSlotRight.add(0, 0.1, 0), Pointing.DOWN, 20).rightClick();
        scene.addInstruction(new ColoredValueSlotInstruction(scene, valueSlotRight, Direction.WEST, new Vec3(-15, 0, 0), PonderPalette.INPUT, 20));

        scene.idle(8);

        gimbalSensorModifyNixieTube(scene, util, 1, 0, true);
        gimbalSensorModifyNixieTube(scene, util, 3, 8, true);

        scene.idle(20);


        scene.overlay().showText(60)
                .text("...and invert the output direction")
                .colored(PonderPalette.BLUE)
                .placeNearTarget()
                .pointAt(valueSlotRight)
                .attachKeyFrame();

        scene.idle(60);

        scene.addInstruction(new RotateSceneInstruction(-35, 45, true));

        scene.idle(10);

        final ElementLink<WorldSectionElement> nixieTubesXSelection = world.showIndependentSection(nixieTubesX, Direction.DOWN);
        gimbalSensorRepositionSelection(scene, nixieTubesXSelection, new Vec3(0, -0.5, 0), new Vec3(15, 0, 0));

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(platformSelection, new Vec3(-15, 0, 0), 20, SmoothMovementUtils.quadraticRiseInOut()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(nixieTubesXSelection, new Vec3(-15, 0, 0), 20, SmoothMovementUtils.quadraticRiseInOut()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(nixieTubesZSelection, new Vec3(-15, 0, 0), 20, SmoothMovementUtils.quadraticRiseInOut()));

        scene.addInstruction(new GimbalSensorVisualRotationInstruction(gimbalSensor, true));

        for (int i = 0; i < 18; i++) {
            gimbalSensorModifyNixieTube(scene, util, 3, 8 - i / 2, false);
            gimbalSensorModifyRedstoneDust(scene, wireLocations[3], 8 - i / 2);
            scene.idle(1);
        }

        scene.idle(10);

        scene.addInstruction(new GimbalSensorVisualRotationInstruction(gimbalSensor, false));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(platformSelection, new Vec3(-7, 0, 7), 40, SmoothMovementUtils.quadraticRiseOut()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(nixieTubesXSelection, new Vec3(-7, 0, 7), 40, SmoothMovementUtils.quadraticRiseOut()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(nixieTubesZSelection, new Vec3(-7, 0, 7), 40, SmoothMovementUtils.quadraticRiseOut()));

        for (int i = 0; i < 41; i++) {
            setGimbalAngle(scene, util, gimbalSensor, -(float) Math.toRadians(i) / 5, -(float) Math.toRadians(i) / 5);

            gimbalSensorModifyNixieTube(scene, util, 1, i * 15 / 40, false);
            gimbalSensorModifyNixieTube(scene, util, 0, i * 7 / 40, false);

            gimbalSensorModifyRedstoneDust(scene, wireLocations[1], i * 15 / 40);
            gimbalSensorModifyRedstoneDust(scene, wireLocations[0], i * 7 / 40);
            scene.idle(1);
        }

        scene.idle(20);
        scene.addKeyframe();

        scene.overlay().showControls(new Vec3(3, 2.4, 2.5), Pointing.DOWN, 20).rightClick();
        scene.addInstruction(new ColoredValueSlotInstruction(scene, new Vec3(2.3, 3, 1.8), Direction.NORTH, new Vec3(0, 0, -5), PonderPalette.RED, 20));
        scene.idle(8);

        gimbalSensorModifyNixieTube(scene, util, 0, 0, false);
        gimbalSensorModifyNixieTube(scene, util, 2, 7, true);

        gimbalSensorModifyRedstoneDust(scene, wireLocations[0], 0);
        gimbalSensorModifyRedstoneDust(scene, wireLocations[2], 7);

        scene.idle(40);
        scene.overlay().showText(60)
                .text("Each axis operates independently of one another")
                .colored(PonderPalette.WHITE)
                .placeNearTarget()
                .pointAt(new Vec3(2.9, 2.35, 2.5));
        scene.idle(40);
    }

    //

    // Navigation Table

    private static void setNavTableItem(final CreateSceneBuilder.WorldInstructions world, final SceneBuildingUtil util, final BlockPos blockPos, final ItemStack stack) {
        world.modifyBlockEntity(blockPos, NavTableBlockEntity.class, be -> {
            be.setHeldItem(stack);
            be.isPowering = true;
        });
    }

    public static void navigationTable(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();

        scene.title("navigation_table", "Using the Navigation Table");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.addInstruction(new TranslateYSceneInstruction(-0.5f, 1));
        scene.addInstruction(new ScaleSceneInstruction(0.9f, 1));

        final BlockPos navigationTable1 = new BlockPos(3, 3, 3);
        final BlockPos swivelBearing = new BlockPos(3, 1, 3);
        final BlockPos[] wireLocations = new BlockPos[]{
                util.grid().at(3, 3, 2),
                util.grid().at(4, 3, 3),
                util.grid().at(3, 3, 4),
                util.grid().at(2, 3, 3)
        };
        final BlockPos[] lampLocations = new BlockPos[]{
                util.grid().at(3, 2, 1),
                util.grid().at(5, 2, 3),
                util.grid().at(3, 2, 5),
                util.grid().at(1, 2, 3)
        };
        final BlockPos[] nixieTubeLocations = new BlockPos[]{
                util.grid().at(1, 3, 3), util.grid().at(3, 3, 5),
                util.grid().at(5, 3, 3), util.grid().at(3, 3, 1)
        };

        final BlockPos navigationTable2 = new BlockPos(1, 1, 1);
        final BlockPos banner = new BlockPos(4, 1, 4);

        final Selection allKinetics = util.select().position(7, 0, 2).add(util.select().fromTo(6, 1, 2, 3, 1, 3));
        final Selection inverseKinetics = util.select().position(6, 1, 2).add(util.select().position(4, 1, 3));

        final Selection lodestonePillar = util.select().fromTo(3, 0, 7, 3, 4, 7);
        final Selection centralRedstone = util.select().fromTo(2, 3, 2, 4, 3, 4);
        final Selection nixieTubes = util.select().position(3, 3, 5).add(util.select().position(5, 3, 3));

        final Selection navTableLamps2 = util.select().position(3, 1, 1).add(util.select().position(1, 1, 3));
        final Selection navTableRedstone2 = util.select().position(2, 1, 1).add(util.select().position(1, 1, 2));

        final Selection navTable2Left = util.select().position(2, 1, 1).add(util.select().position(3, 1, 1));
        final Selection navTable2Right = util.select().position(1, 1, 2).add(util.select().position(1, 1, 3));

        final Selection gravestone = util.select().fromTo(2, 1, 6, 3, 2, 6);
        final Selection gravestoneBaseplate = util.select().fromTo(1, 0, 5, 2, 0, 5);

        final Selection lodestone = util.select().fromTo(5, 1, 1, 5, 2, 1);
        final Selection fullPlatform = util.select().fromTo(1, 2, 1, 5, 2, 5).add(centralRedstone).substract(lodestone);

        final DataComponentPatch lodestoneComponent = DataComponentPatch.builder()
                .set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(new GlobalPos(Level.OVERWORLD, new BlockPos(0, 0, 0))), true))
                .build();

        final ItemStack map = Items.FILLED_MAP.getDefaultInstance();
        final ItemStack recoveryCompass = Items.RECOVERY_COMPASS.getDefaultInstance();
        final ItemStack lodestoneCompass = Items.COMPASS.getDefaultInstance();
        lodestoneCompass.applyComponents(lodestoneComponent);

        scene.idle(20);
        world.showSection(allKinetics, Direction.DOWN);
        scene.idle(10);
        final ElementLink<WorldSectionElement> platformLink = world.showIndependentSection(fullPlatform, Direction.DOWN);
        scene.idle(10);
        world.showSection(lodestonePillar, Direction.DOWN);

        scene.idle(30);

        scene.overlay().showText(90)
                .text("The Navigation Table provides redstone feedback for Navigation Items")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(3, 3, 3));

        scene.idle(110);
        scene.addKeyframe();

        scene.overlay().showText(90)
                .text("When a Navigation Item is provided...")
                .placeNearTarget()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(3, 3, 3));

        scene.idle(10);
        scene.overlay().showControls(util.vector().blockSurface(new BlockPos(3, 3, 7), Direction.NORTH), Pointing.RIGHT, 15).withItem(lodestoneCompass);

        final AABB bb_pillar = new AABB(new Vec3(3, 3, 7), new Vec3(4, 4, 7));
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, bb_pillar, bb_pillar, 5);
        scene.idle(5);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, bb_pillar, bb_pillar.expandTowards(0, 0, 1), 60);

        scene.idle(30);

        scene.overlay().showControls(util.vector().blockSurface(navigationTable1, Direction.UP), Pointing.DOWN, 30).withItem(lodestoneCompass);

        scene.idle(5);

        setNavTableItem(world, util, navigationTable1, lodestoneCompass);
        scene.addInstruction(new NavTableRotationInstruction(navigationTable1, 90, 5));
        world.toggleRedstonePower(util.select().position(lampLocations[2]).add(util.select().position(wireLocations[2])));
        scene.effects().indicateRedstone(wireLocations[2]);
        scene.effects().indicateRedstone(lampLocations[2]);

        scene.idle(60);

        scene.addInstruction(new NavTableRotationInstruction(navigationTable1, 360, 160));

        scene.idle(2);

        world.setKineticSpeed(allKinetics, 8);
        world.setKineticSpeed(inverseKinetics, -8);
        world.modifyBlockEntityNBT(util.select().position(swivelBearing), SwivelBearingBlockEntity.class, nbt -> {
            nbt.getCompoundOrEmpty("SwivelCog").putFloat("Speed", 8f);
        });

        world.rotateSection(platformLink, 0, 360, 0, 160);

        for (int i = 0; i < 80; i++) {
            // Every quarter rotation toggle lamps as needed
            if (i % 20 == 0) {
                final int quarter = i / 20;
                final int quarterOn = (quarter + 3) % 4;
                world.toggleRedstonePower(util.select().position(lampLocations[quarterOn]));
                scene.effects().indicateRedstone(lampLocations[quarterOn]);
                if (quarter > 0) {
                    final int quarterOff = (quarter + 1) % 4;
                    world.toggleRedstonePower(util.select().position(lampLocations[quarterOff]));
                }
            }

            // Use abs curve to have redstone signal strength dynamically adapt as expected
            for (int j = 0; j < 4; j++) {
                final int quarter = (j + 2) % 4;
                final int finalI = i;
                world.modifyBlock(wireLocations[j], s -> s.setValue(RedStoneWireBlock.POWER, Mth.ceil(Math.max(0, 15 - Math.abs(0.75 * finalI - 15 * quarter)))), false);

                // Hardcode turning the first one back on after a full loop because I'm tired
                if (i > 60)
                    world.modifyBlock(wireLocations[2], s -> s.setValue(RedStoneWireBlock.POWER, Mth.ceil((finalI - 60) * 0.75)), false);
            }

            if (i == 25) {
                scene.overlay().showText(100)
                        .text("...it will emit a Redstone signal towards the destination")
                        .placeNearTarget()
                        .colored(PonderPalette.OUTPUT)
                        .pointAt(util.vector().topOf(3, 2, 5));
            }

            scene.idle(2);
        }

        world.toggleRedstonePower(util.select().position(lampLocations[1]));
        world.setKineticSpeed(allKinetics, 0);
        world.setKineticSpeed(inverseKinetics, 0);
        world.modifyBlockEntityNBT(util.select().position(swivelBearing), SwivelBearingBlockEntity.class, nbt -> {
            nbt.getCompoundOrEmpty("SwivelCog").putFloat("Speed", 0f);
        });

        scene.idle(20);

        world.showSectionAndMerge(nixieTubes, Direction.DOWN, platformLink);

        scene.idle(15);
        scene.addInstruction(new NavTableRotationInstruction(navigationTable1, -45, 20));
        scene.idle(2);

        world.rotateSection(platformLink, 0, -45, 0, 20);

        world.toggleRedstonePower(util.select().position(lampLocations[1]));
        scene.effects().indicateRedstone(lampLocations[1]);
        world.setKineticSpeed(allKinetics, -8);
        world.setKineticSpeed(inverseKinetics, 8);
        world.modifyBlockEntityNBT(util.select().position(swivelBearing), SwivelBearingBlockEntity.class, nbt -> {
            nbt.getCompoundOrEmpty("SwivelCog").putFloat("Speed", -8f);
        });

        for (int i = 0; i < 20; i++) {
            final int finalI = i;
            final int chargingPower = Mth.floor(Math.max(0, 0.375 * finalI));
            final int dechargingPowwer = Mth.ceil(Math.max(0, 15 - 0.375 * finalI));

            world.modifyBlock(wireLocations[1], s -> s.setValue(RedStoneWireBlock.POWER, chargingPower), false);
            world.modifyBlockEntityNBT(util.select().position(5, 3, 3), NixieTubeBlockEntity.class,
                    nbt -> nbt.putInt("RedstoneStrength", chargingPower));

            world.modifyBlock(wireLocations[2], s -> s.setValue(RedStoneWireBlock.POWER, dechargingPowwer), false);
            world.modifyBlockEntityNBT(util.select().position(3, 3, 5), NixieTubeBlockEntity.class,
                    nbt -> nbt.putInt("RedstoneStrength", dechargingPowwer));

            scene.idle(1);
        }

        world.setKineticSpeed(allKinetics, 0);
        world.setKineticSpeed(inverseKinetics, 0);
        world.modifyBlockEntityNBT(util.select().position(swivelBearing), SwivelBearingBlockEntity.class, nbt -> {
            nbt.getCompoundOrEmpty("SwivelCog").putFloat("Speed", 0f);
        });

        scene.overlay().showText(90)
                .text("The Redstone signal is weaker when not directly facing the destination")
                .placeNearTarget()
                .pointAt(util.vector().topOf(0, 4, 3))
                .attachKeyFrame();

        scene.idle(100);

        world.hideIndependentSection(platformLink, Direction.UP);
        world.hideSection(allKinetics, Direction.UP);
        world.hideSection(lodestonePillar, Direction.UP);
        scene.addInstruction(new ScaleSceneInstruction(1, 25));
        scene.addInstruction(new TranslateYSceneInstruction(0.5f, 25, SmoothMovementUtils.cubicSmoothing()));

        scene.idle(20);

        world.showSection(util.select().position(navigationTable2), Direction.DOWN);
        scene.idle(2);
        world.showSection(navTableRedstone2, Direction.DOWN);
        scene.idle(2);
        world.showSection(navTableLamps2, Direction.DOWN);

        scene.idle(20);

        scene.overlay().showControls(util.vector().blockSurface(navigationTable2, Direction.UP), Pointing.DOWN, 50).withItem(lodestoneCompass);
        scene.idle(5);
        world.modifyBlockEntityNBT(util.select().position(navigationTable2), NavTableBlockEntity.class,
                tag -> tag.store("CurrentStack", ItemStack.OPTIONAL_CODEC, lodestoneCompass), true);
        world.showSection(lodestone, Direction.SOUTH);

        world.toggleRedstonePower(navTable2Left);
        scene.effects().indicateRedstone(new BlockPos(2, 1, 1));

        scene.overlay().showText(210)
                .text("Different Navigation Items provide unique targets")
                .placeNearTarget()
                .pointAt(util.vector().topOf(1, 1, 1))
                .colored(PonderPalette.GREEN)
                .attachKeyFrame();

        scene.idle(15);
        final AABB bb1 = new AABB(new Vec3(5, 1, 1), new Vec3(6, 1, 2));
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, bb1, bb1, 1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, bb1, bb1.expandTowards(0, 2, 0), 35);
        scene.idle(35);

        world.hideSection(lodestone, Direction.SOUTH);

        scene.idle(20);

        scene.world().hideSection(gravestoneBaseplate, Direction.DOWN);
        scene.idle(5);
        final ElementLink<WorldSectionElement> gravestoneSection = world.showIndependentSection(gravestone, Direction.DOWN);
        scene.world().moveSection(gravestoneSection, new Vec3(-1, -1, -1), 0);

        scene.overlay().showControls(util.vector().blockSurface(navigationTable2, Direction.UP), Pointing.DOWN, 50).withItem(recoveryCompass);
        scene.idle(5);
        setNavTableItem(world, util, navigationTable2, recoveryCompass);
        scene.addInstruction(new NavTableRotationInstruction(navigationTable2, 90, 5));
        world.toggleRedstonePower(navTable2Left);
        world.toggleRedstonePower(navTable2Right);
        scene.effects().indicateRedstone(new BlockPos(2, 1, 1));

        scene.idle(5);

        final AABB bb2 = new AABB(new Vec3(1, 1, 5), new Vec3(3, 1, 6));
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, bb2, bb2, 1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, bb2, bb2.expandTowards(0, 1, 0), 35);

        scene.idle(45);

        world.hideIndependentSection(gravestoneSection, Direction.DOWN);
        scene.idle(20);
        world.showSection(gravestoneBaseplate, Direction.UP);

        scene.overlay().showControls(util.vector().blockSurface(navigationTable2, Direction.UP), Pointing.DOWN, 50).withItem(map);
        world.showSection(util.select().position(banner), Direction.EAST);
        scene.idle(5);
        setNavTableItem(world, util, navigationTable2, map);
        world.toggleRedstonePower(navTable2Left);
        scene.addInstruction(new NavTableRotationInstruction(navigationTable2, -45, 5));
        scene.effects().indicateRedstone(new BlockPos(2, 1, 1));

        scene.idle(10);
        final AABB bb3 = new AABB(new Vec3(4, 1, 4), new Vec3(5, 1, 5));
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, bb3, bb3, 1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.BLUE, bb3, bb3.expandTowards(0, 1.75, 0), 35);
    }
    //

    // Velocity Sensor

    private static void modifyKineticSpeed(final SceneBuilder scene, final Selection selection, final UnaryOperator<Float> speedFunc) {
        scene.world().modifyBlockEntityNBT(selection, KineticBlockEntity.class, nbt -> {
            if (nbt.contains("SwivelCog")) {
                final CompoundTag innerTag = nbt.getCompoundOrEmpty("SwivelCog");
                innerTag.putFloat("Speed", -speedFunc.apply(innerTag.getFloatOr("Speed", 0)));
            } else {
                nbt.putFloat("Speed", speedFunc.apply(nbt.getFloatOr("Speed", 0)));
            }
        });
    }

    private static void setVelocitySensor(final SceneBuilder scene, final Selection selection, final float velocity, final int config) {
        scene.world().modifyBlockEntityNBT(selection, VelocitySensorBlockEntity.class, tag -> {
            tag.putFloat("AdjustedVelocity", velocity);
            tag.putInt("ScrollValue", config);
            tag.putInt("SignedRedstoneStrength", (int) Math.clamp(15 * velocity / config, 0, 15));
        });
    }

    private static void setVeloNixie(final SceneBuilder scene, final Selection selection, final int power) {
        scene.world().modifyBlockEntityNBT(selection, NixieTubeBlockEntity.class,
                nbt -> nbt.putInt("RedstoneStrength", power));
    }

    public static void velocitySensor(final SceneBuilder scene, final SceneBuildingUtil util) {
        scene.title("velocity_sensor", "Using the Velocity Sensor");
        scene.configureBasePlate(0, 2, 5);

        final BlockPos sensorPos = new BlockPos(2, 2, 4);
        final Selection sensorSelect = util.select().position(sensorPos);

        final Selection startingPlatformIceSelection = util.select().fromTo(1, 0, 3, 3, 0, 6);
        final Selection startingPlatformSelection = util.select().fromTo(0, 0, 1, 4, 0, 6).substract(startingPlatformIceSelection);

        final ElementLink<WorldSectionElement> startingPlatform = scene.world().showIndependentSection(startingPlatformSelection, Direction.UP);
        scene.world().showSection(startingPlatformIceSelection, Direction.UP);


        scene.idle(5);
        scene.world().showSection(util.select().fromTo(1, 1, 3, 3, 1, 5), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(sensorSelect, Direction.DOWN);
        scene.idle(5);
        scene.overlay().showText(70)
                .text("The Velocity Sensor outputs a redstone signal based on its speed")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(sensorPos, Direction.WEST));

        scene.idle(80);

        scene.world().hideSection(startingPlatformIceSelection, Direction.DOWN);

        scene.idle(20);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                scene.world().setBlock(new BlockPos(i + 1, 0, j + 3),
                        ((i + j) % 2 == 0 ? Blocks.BLUE_ICE : Blocks.PACKED_ICE).defaultBlockState(),
                        false);
            }
        }

        final Selection loopingPlatformSelection = util.select().fromTo(0, 0, 7, 4, 0, 42);
        final ElementLink<WorldSectionElement> loopingPlatform1 = scene.world().showIndependentSection(loopingPlatformSelection, Direction.UP);
        final ElementLink<WorldSectionElement> loopingPlatform2 = scene.world().showIndependentSection(loopingPlatformSelection, Direction.UP);
        scene.world().showSectionAndMerge(startingPlatformIceSelection, Direction.UP, startingPlatform);
        scene.world().moveSection(loopingPlatform2, new Vec3(0, 0, 36), 0);
        scene.idle(10);

        final Selection hammerSelection = util.select().fromTo(0, 2, 1, 8, 4, 2);
        final ElementLink<WorldSectionElement> hammer = scene.world().showIndependentSection(hammerSelection, Direction.SOUTH);
        scene.world().configureCenterOfRotation(hammer, new Vec3(7.5, 2, 1.5));
        scene.world().rotateSection(hammer, 0, -30, 0, 0);
        scene.world().moveSection(hammer, new Vec3(0, -1, 0), 0);

        final Selection kineticSelection = util.select().fromTo(5, 0, 1, 7, 1, 2);
        final ElementLink<WorldSectionElement> kinetics = scene.world().showIndependentSection(kineticSelection, Direction.SOUTH);
        scene.idle(20);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(hammer, new Vec3(0, 30, 0), 8,
                SmoothMovementUtils.quadraticRise()));
        modifyKineticSpeed(scene, kineticSelection, f -> 16f);
        scene.idle(8);
        modifyKineticSpeed(scene, kineticSelection, f -> 0f);
        scene.addInstruction(new CustomToggleBaseShadowInstruction());

        setVelocitySensor(scene, sensorSelect, 4, 8);
        scene.world().modifyBlock(sensorPos, state -> state.setValue(VelocitySensorBlock.POWERED, 1), false);
        final int dist = 60;
        final int dur = dist * 5;
        // 4 blocks per second
        scene.world().moveSection(startingPlatform, new Vec3(0, 0, -dist), dur);
        scene.world().moveSection(loopingPlatform1, new Vec3(0, 0, -dist), dur);
        scene.world().moveSection(loopingPlatform2, new Vec3(0, 0, -dist), dur);
        scene.world().moveSection(hammer, new Vec3(0, 0, -dist), dur);
        scene.world().moveSection(kinetics, new Vec3(0, 0, -dist), dur);
        scene.idle(20);
        scene.world().hideIndependentSection(hammer, Direction.NORTH);
        scene.world().hideIndependentSection(kinetics, Direction.NORTH);
        scene.rotateCameraY(-25);

        final BlockPos tubeTowardsPos = new BlockPos(2, 2, 5);
        final Selection tubeTowardsSelect = util.select().position(tubeTowardsPos);

        {
            final SceneScheduler scheduler = new SceneScheduler(scene);
            { // moving ground
                final SceneScheduler.Sequence seq = scheduler.get(0);
                seq.idle(dur - 20);
                seq.world().hideIndependentSection(startingPlatform, Direction.DOWN);
                seq.world().moveSection(loopingPlatform1, new Vec3(0, 0, 36 * 2), 0);
                seq.world().moveSection(loopingPlatform1, new Vec3(0, 0, -36), 36 * 5);
                seq.world().moveSection(loopingPlatform2, new Vec3(0, 0, -36), 36 * 5);
                seq.idle(36 * 5);
                seq.world().moveSection(loopingPlatform2, new Vec3(0, 0, 36 * 2), 0);
                seq.world().moveSection(loopingPlatform1, new Vec3(0, 0, -36), 36 * 5);
                seq.world().moveSection(loopingPlatform2, new Vec3(0, 0, -36), 36 * 5);
                seq.idle(36 * 5);
                seq.world().moveSection(loopingPlatform1, new Vec3(0, 0, 36 * 2), 0);
                seq.world().moveSection(loopingPlatform1, new Vec3(0, 0, -28), 140);
                seq.world().moveSection(loopingPlatform2, new Vec3(0, 0, -28), 140);
            }
            { // actual ponder
                final SceneScheduler.Sequence seq = scheduler.get(1);
                seq.idle(20);
                seq.overlay().showText(70)
                        .text("By default, power is output away from the direction of motion")
                        .placeNearTarget()
                        .pointAt(util.vector().blockSurface(sensorPos, Direction.SOUTH));
                seq.addLazyKeyframe();
                seq.idle(80);

                final BlockPos tubeAwayPos = new BlockPos(2, 2, 3);
                final Selection tubeAwaySelect = util.select().position(tubeAwayPos);
                seq.world().showSection(tubeAwaySelect, Direction.DOWN);
                seq.world().showSection(tubeTowardsSelect, Direction.DOWN);

                seq.idle(10);
                seq.effects().indicateRedstone(tubeAwayPos);
                setVeloNixie(seq, tubeAwaySelect, 7);
                seq.idle(10);

                final AABB bb = new AABB(sensorPos).inflate(0, 0, 2);
                seq.addInstruction(new AirflowAABBInstruction(PonderPalette.WHITE, bb,
                        40, Direction.NORTH, 4, 2));
                seq.idle(40);
                final Vec3 valueBoxBack = util.vector().centerOf(sensorPos).add(0, 0, -0.25);
                seq.overlay().showText(60)
                        .text("The value box can configure the speed of maximum signal")
                        .placeNearTarget()
                        .pointAt(valueBoxBack);
                seq.addLazyKeyframe();
                seq.idle(70);
                seq.overlay().showControls(valueBoxBack.add(0, 0.25, 0), Pointing.DOWN, 70).rightClick();
                seq.overlay().showScrollInput(valueBoxBack, Direction.NORTH, 70);
                setVelocitySensor(seq, sensorSelect, 4, 64);
                seq.effects().indicateRedstone(tubeAwayPos);
                setVeloNixie(seq, tubeAwaySelect, 3);
                seq.idle(50);

                setVelocitySensor(seq, sensorSelect, 4, 4);
                seq.effects().indicateRedstone(tubeAwayPos);
                setVeloNixie(seq, tubeAwaySelect, 15);
                seq.idle(50);

                seq.rotateCameraY(-60);
                seq.idle(20);
                seq.overlay().showText(70)
                        .text("It can also change the output side to be towards the direction of motion")
                        .placeNearTarget()
                        .pointAt(util.vector().blockSurface(sensorPos, Direction.SOUTH));
                seq.addLazyKeyframe();
                seq.idle(80);

                seq.world().modifyBlock(sensorPos, state -> state.setValue(VelocitySensorBlock.POWERED, 2), false);
                seq.world().modifyBlockEntityNBT(sensorSelect, VelocitySensorBlockEntity.class, tag -> {
                    tag.putInt("ScrollValueTowards", 1);
                });
                seq.effects().indicateRedstone(tubeTowardsPos);
                setVeloNixie(seq, tubeAwaySelect, 0);
                setVeloNixie(seq, tubeTowardsSelect, 15);
                seq.addInstruction(new AirflowAABBInstruction(PonderPalette.WHITE, bb,
                        40, Direction.NORTH, 4, 2));
                seq.idle(60);

                final ElementLink<WorldSectionElement> puck = seq.world().makeSectionIndependent(
                        util.select().fromTo(1, 2, 3, 3, 2, 5));
                seq.world().moveSection(puck, new Vec3(0, 1, 0), 10);
                seq.idle(15);
                seq.world().rotateSection(puck, 0, 90, 0, 30);
                for (int i = 14; i >= 0; i--) {
                    setVelocitySensor(seq, sensorSelect, i, 15);
                    setVeloNixie(seq, tubeTowardsSelect, i);
                    seq.idle(2);
                }
                seq.idle(5);
                seq.world().moveSection(puck, new Vec3(0, -1, 0), 10);
                seq.idle(30);
                seq.overlay().showText(70)
                        .text("Velocity is only measured through the facing axis")
                        .colored(PonderPalette.RED)
                        .placeNearTarget()
                        .pointAt(util.vector().blockSurface(sensorPos, Direction.WEST));
                seq.addLazyKeyframe();
                seq.idle(80);

                final AABB bb2 = new AABB(sensorPos);//inflate(2, 0, 0);
                seq.addInstruction(new AirflowAABBInstruction(PonderPalette.RED, bb2,
                        20, Direction.EAST, 1, 2));
                seq.idle(30);
                seq.addInstruction(new AirflowAABBInstruction(PonderPalette.RED, bb2,
                        20, Direction.WEST, 1, 2));
                seq.idle(30);

                seq.world().moveSection(puck, new Vec3(0, 1, 0), 10);
                seq.idle(15);
                seq.world().rotateSection(puck, 0, -90, 0, 30);
                for (int i = 0; i <= 15; i++) {
                    setVelocitySensor(seq, sensorSelect, i, 15);
                    setVeloNixie(seq, tubeTowardsSelect, i);
                    seq.idle(2);
                }
                seq.idle(5);
                seq.world().moveSection(puck, new Vec3(0, -1, 0), 10);
            }
            scheduler.run(false);
            scene.idle(18);

            /* TW: calculus
               t < 0 : x(t) = 4t
               t > 0 : x(t) = s * (t / T) * (2 - t / T)
               x'(0-) = 4
               x'(0+) -> 4
               x'(0+) = s * (2 / T - 2 * t / T^2)
                      = s * 2 / T
               4      = 20 * 2 / T
               -> T = 20 * 2 / 4 = 10 seconds
             */
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(
                    loopingPlatform1, new Vec3(0, 0, -20), 200, SmoothMovementUtils.quadraticRiseOut()));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(
                    loopingPlatform2, new Vec3(0, 0, -20), 200, SmoothMovementUtils.quadraticRiseOut()));
            for (int i = 0; i < 3; i++) {
                scene.world().setBlock(new BlockPos(1 + i, 0, 42),
                        (i == 1 ? Blocks.WHITE_CONCRETE : Blocks.SNOW_BLOCK).defaultBlockState(), false);
            }

            final ParrotElement dangerParrot = ExpandedParrotElement.create(new Vec3(2.5, 1, 26.5), ParrotPose.DancePose::new);
            final ElementLink<ParrotElement> dangerParrotLink = new ElementLinkImpl<>(ParrotElement.class);
            scene.addInstruction(new CreateParrotInstruction(0, Direction.DOWN, dangerParrot));
            scene.addInstruction(s -> s.linkElement(dangerParrot, dangerParrotLink));
            scene.addInstruction(CustomAnimateParrotInstruction.move(
                    dangerParrotLink, new Vec3(0, 0, -20), 200, SmoothMovementUtils.quadraticRiseOut())
            );

            for (int i = 14; i >= 0; i--) {
                setVelocitySensor(scene, sensorSelect, i, 15);
                setVeloNixie(scene, tubeTowardsSelect, i);
                final int prevTicks = ((14 - i) * 200) / 15;
                final int ticks = ((15 - i) * 200) / 15;
                scene.idle(ticks - prevTicks);
            }
        }
    }
    //
}
