package dev.ryanhcode.offroad.content.ponder.scenes;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import dev.simulated_team.simulated.content.blocks.analog_transmission.AnalogTransmissionBlockEntity;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.instructions.CustomAnimateWorldSectionInstruction;
import dev.simulated_team.simulated.ponder.instructions.OffsetBreakParticlesInstruction;
import dev.simulated_team.simulated.ponder.instructions.PullTheAssemblerKronkInstruction;
import dev.simulated_team.simulated.ponder.instructions.TranslateYSceneInstruction;
import dev.ryanhcode.offroad.config.OffroadConfig;
import dev.ryanhcode.offroad.content.blocks.borehead_bearing.BoreheadBearingBlockEntity;
import dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel.RockCuttingWheelBlockEntity;
import dev.ryanhcode.offroad.content.ponder.instructions.ChangeBoreheadAndContraptionSpeedInstruction;
import dev.ryanhcode.offroad.content.ponder.instructions.StopBoreheadBearingAndContraptionInstruction;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.scene.*;
import com.zurrtum.create.client.ponder.foundation.instruction.FadeOutOfSceneInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static dev.ryanhcode.offroad.content.ponder.instructions.ChangeBoreheadAndContraptionSpeedInstruction.RotationAxis.Z;

public class BoreheadBearingScenes {

    public static void boreheadIntro(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();
        final VectorUtil vector = util.vector();
        final CreateSceneBuilder.EffectInstructions effects = scene.effects();

        scene.title("borehead_bearing_intro", "Using Borehead Bearings and Rock Cutting Wheels");

        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        final Selection borehead = select.fromTo(util.grid().at(1, 2, 2), util.grid().at(3, 2, 2));
        final Selection boreheadCenter = select.position(util.grid().at(2, 2, 2));

        final BlockPos boreheadBearing = util.grid().at(2, 2, 3);
        final BlockPos funnel = util.grid().at(2, 2, 4);

        final Selection positiveKinetics = select.fromTo(util.grid().at(0, 2, 3), util.grid().at(1, 4, 3));
        final Selection negativeKinetics = select.fromTo(util.grid().at(5, 0, 3), util.grid().at(3, 4, 3));

        final Selection gearboxKinetics = select.fromTo(util.grid().at(0, 4, 3), util.grid().at(4, 4, 3));

        final Selection wallLocation = select.fromTo(util.grid().at(1, 1, 1), util.grid().at(3, 3, 1));

        world.showSection(select.position(boreheadBearing.below()), Direction.UP);
        scene.idle(15);
        world.showSection(select.position(boreheadBearing), Direction.DOWN);
        scene.idle(5);
        world.showSection(select.position(util.grid().at(3, 2, 3)), Direction.DOWN);
        scene.idle(3);
        world.showSection(select.position(util.grid().at(4, 2, 3)), Direction.DOWN);
        scene.idle(3);
        final ElementLink<WorldSectionElement> belt = world.showIndependentSection(select.fromTo(util.grid().at(5, 1, 3), util.grid().at(5, 4, 3)), Direction.DOWN);
        world.moveSection(belt, vector.of(0, -2, 0), 0);

        scene.idle(20);

        scene.overlay().showOutlineWithText(boreheadCenter, 60)
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().blockSurface(boreheadBearing, Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget()
                .text("Borehead Bearings attach to the block in front of them");
        scene.idle(50);

        final ElementLink<WorldSectionElement> boreheadLink = world.showIndependentSection(boreheadCenter, Direction.SOUTH);
        scene.idle(10);
        world.showSectionAndMerge(borehead.substract(boreheadCenter), Direction.SOUTH, boreheadLink);

        scene.idle(5);
        scene.effects().superGlue(boreheadBearing.north(), Direction.SOUTH, true);
        scene.idle(25);

        scene.overlay().showText(100)
                .pointAt(boreheadCenter.getCenter().add(0, 0.5, 0))
                .attachKeyFrame()
                .placeNearTarget()
                .text("When a Storage Container and Rock Cutting Wheels are present, it can be used as a Drill");

        scene.idle(80);

        world.setKineticSpeed(positiveKinetics, 64);
        world.setKineticSpeed(negativeKinetics, -64);

        final ChangeBoreheadAndContraptionSpeedInstruction vehicle32Instr = new ChangeBoreheadAndContraptionSpeedInstruction(boreheadBearing, boreheadLink, Z, -64);
        scene.addInstruction(vehicle32Instr);

        spinRockCutters(scene, util, util.grid().at(1, 2, 2), util.grid().at(3, 2, 2), 5, 1000);

        scene.idle(40);

        scene.overlay().showText(100)
                .pointAt(boreheadCenter.getCenter().add(-1, 0, 0))
                .colored(PonderPalette.GREEN)
                .placeNearTarget()
                .text("Rock Cutting Wheels will automatically attach to blocks and each other without need of Super Glue");

        scene.idle(120);

        scene.rotateCameraY(-90);

        scene.idle(20);

        world.hideSection(select.fromTo(util.grid().at(3, 2, 3), util.grid().at(4, 2, 3)), Direction.DOWN);
        scene.idle(10);

        final ElementLink<WorldSectionElement> gearboxKineticsLink = world.showIndependentSection(gearboxKinetics, Direction.DOWN);
        world.moveSection(gearboxKineticsLink, vector.of(0, -2, 0), 0);

        for (final BlockPos rotatingKinetics : select.fromTo(0, 2, 3, 4, 2, 3).substract(select.position(boreheadBearing))) {
            effects.rotationDirectionIndicator(rotatingKinetics);
        }

        scene.idle(20);

        scene.overlay().showText(80)
                .pointAt(vector.blockSurface(boreheadBearing, Direction.SOUTH))
                .attachKeyFrame()
                .placeNearTarget()
                .colored(PonderPalette.INPUT)
                .text("The Borehead Bearing reverses the direction of Rotation...");

        scene.idle(100);

        world.setKineticSpeed(positiveKinetics, 128);
        world.setKineticSpeed(negativeKinetics.add(select.position(boreheadBearing)), -128);
        spinRockCutters(scene, util, util.grid().at(1, 2, 2), util.grid().at(3, 2, 2), 15, 1000);

        for (final BlockPos rotatingKinetics : select.fromTo(0, 2, 3, 4, 2, 3).substract(select.position(boreheadBearing))) {
            effects.rotationDirectionIndicator(rotatingKinetics);
        }

        scene.idle(20);

        scene.overlay().showText(80)
                .pointAt(vector.topOf(boreheadBearing).add(0, 0, -0.5))
                .attachKeyFrame()
                .placeNearTarget()
                .colored(PonderPalette.OUTPUT)
                .text("...and spins at 0.25x the provided Rotational Speed");

        scene.idle(100);

        scene.rotateCameraY(90);

        scene.idle(40);

        scene.overlay().showText(120)
                .pointAt(vector.centerOf(util.grid().at(2, 2, 1)))
                .attachKeyFrame()
                .placeNearTarget()
                .text("The mining speed depends on the Rotational Input...");

        for (int i = 0; i < 4; i++) {
            if (i == 2) {
                scene.overlay().showText(100)
                        .pointAt(vector.centerOf(util.grid().at(2, 2, 1)))
                        .attachKeyFrame()
                        .placeNearTarget()
                        .text("...and drops are collected into the drill Storage automatically");
            }

            final Selection stoneWall = select.fromTo(util.grid().at(1, 1 + 4 * i, 1), util.grid().at(3, 3 + 4 * i, 1));
            final ElementLink<WorldSectionElement> stoneWallLink = world.showIndependentSection(stoneWall, Direction.SOUTH);
            world.moveSection(stoneWallLink, vector.of(0, -4 * i, 0), 0);

            scene.idle(5);

            for (int j = 0; j < ((i < 3) ? 9 : 6); j++) {
                scene.idle(5);
                for (final BlockPos stoneBlock : stoneWall) {
                    world.incrementBlockBreakingProgress(stoneBlock.immutable());
                }
            }
            scene.idle(5);

            if (i < 3) {
                world.replaceBlocks(stoneWall, Blocks.AIR.defaultBlockState(), false);
                for (final BlockPos blockPos : wallLocation) {
                    scene.addInstruction(new OffsetBreakParticlesInstruction(new AABB(blockPos), Blocks.STONE.defaultBlockState()));
                }
                scene.idle(20);
            }
        }

        spinRockCutters(scene, util, util.grid().at(1, 2, 2), util.grid().at(3, 2, 2), 0, 0);
        world.modifyBlockEntity(boreheadBearing, BoreheadBearingBlockEntity.class, be -> {
            be.setStalled(true);
        });

        for (final BlockPos wallBlock : wallLocation) {
            effects.emitParticles(Vec3.atLowerCornerOf(wallBlock), effects.particleEmitterWithinBlockSpace(ParticleTypes.CLOUD, new Vec3(0.0, 0.0, 0.0)), 5.0F, 2);
        }
        overlay.showOutline(PonderPalette.RED, "full", wallLocation, 60);

        scene.idle(20);

        scene.overlay().showText(100)
                .pointAt(vector.centerOf(util.grid().at(2, 2, 1)))
                .colored(PonderPalette.RED)
                .attachKeyFrame()
                .placeNearTarget()
                .text("When the drill Storage is full, it will be unable to continue breaking blocks");

        scene.idle(120);

        scene.rotateCameraY(-90);

        scene.idle(20);

        world.showSection(select.position(funnel), Direction.NORTH);

        scene.idle(10);

        final Item[] collectedItems = {Items.COBBLESTONE, Items.COBBLESTONE, AllItems.RAW_ZINC.asItem(), Items.RAW_GOLD, Items.DIAMOND};

        spinRockCutters(scene, util, util.grid().at(1, 2, 2), util.grid().at(3, 2, 2), 15, 1000);
        world.modifyBlockEntity(boreheadBearing, BoreheadBearingBlockEntity.class, be -> {
            be.setStalled(false);
        });

        scene.overlay().showText(100)
                .pointAt(vector.centerOf(funnel))
                .attachKeyFrame()
                .placeNearTarget()
                .text("To remedy this, items can be extracted directly from the Borehead Bearing");

        final Selection lastWall = select.fromTo(util.grid().at(1, 13, 1), util.grid().at(3, 15, 1));
        for (int i = 0; i < 5; i++) {
            if (i < 3) {
                for (final BlockPos stoneBlock : lastWall) {
                    world.incrementBlockBreakingProgress(stoneBlock.immutable());
                }
            }
            if (i == 3) {
                world.replaceBlocks(lastWall, Blocks.AIR.defaultBlockState(), false);
                for (final BlockPos blockPos : wallLocation) {
                    scene.addInstruction(new OffsetBreakParticlesInstruction(new AABB(blockPos), Blocks.STONE.defaultBlockState()));
                }
            }

            world.flapFunnel(funnel, true);
            scene.world().createItemEntity(Vec3.atBottomCenterOf(funnel), vector.of(0, 0, 0),
                    new ItemStack(collectedItems[i]).copyWithCount(32));
            scene.idle(10);
        }
    }

    public static void boreheadExcavating(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();
        final VectorUtil vector = util.vector();

        scene.title("borehead_bearing_excavating", "Excavating Using Rock Cutting Wheels");
        scene.setSceneOffsetY(-2);
        scene.scaleSceneView(0.75f);

        final BlockPos boreheadBearing = new BlockPos(4, 3, 9);
        final Selection lever = select.position(3, 2, 12);

        final Selection borehead = select.fromTo(3, 2, 8, 5, 4, 8);
        final Selection miningVehicle = select.fromTo(3, 1, 8, 5, 4, 12).substract(borehead);

        final Selection positiveRockWheels = select.position(3, 3, 8).add(select.position(4, 2, 8));
        final Selection negativeRockWheels = select.position(5, 3, 8).add(select.position(4, 5, 8));

        final Selection vehicleBelts = select.fromTo(3, 1, 8, 5, 1, 12);
        final Selection otherKinetics = select.fromTo(4, 1, 10, 4, 3, 12);
        final Selection transPos = select.position(4, 2, 9);

        final Selection tunnel = select.fromTo(0, 1, 6, 8, 8, 12).substract(miningVehicle).substract(borehead);
        final Selection minedBlocks = select.fromTo(3, 1, 7, 5, 5, 7).add(select.fromTo(6, 2, 7, 2, 4, 7));

        scene.configureBasePlate(0, 0, 9);
        world.showSection(tunnel.add(select.layer(0)), Direction.UP);

        scene.addInstruction(new PullTheAssemblerKronkInstruction(new BlockPos(4, 4, 10), true, true));
        world.toggleRedstonePower(lever);

        final ElementLink<WorldSectionElement> bodyLink = world.showIndependentSectionImmediately(miningVehicle);
        final ElementLink<WorldSectionElement> boreheadLink = world.showIndependentSectionImmediately(select.fromTo(3, 3, 8, 5, 3, 8));
        final ElementLink<WorldSectionElement>[] vehicleLink = new ElementLink[]{bodyLink, boreheadLink};

        world.rotateSection(boreheadLink, 0, 0, 180, 0);

        final ChangeBoreheadAndContraptionSpeedInstruction vehicle128Instr = new ChangeBoreheadAndContraptionSpeedInstruction(boreheadBearing, boreheadLink, Z, -128);
        scene.addInstruction(vehicle128Instr);

        scene.idle(20);

        for (int i = 0; i < 10; i++) {
            scene.idle(2);
            for (final BlockPos minedBlock : minedBlocks) {
                world.incrementBlockBreakingProgress(minedBlock.immutable());
            }
        }

        spinRockCutters(scene, positiveRockWheels, negativeRockWheels, 10, 48);

        for (final ElementLink part : vehicleLink) {
            world.moveSection(part, vector.of(0, 0, -3), 48);
        }

        world.setKineticSpeed(vehicleBelts, -32);
        world.setKineticSpeed(otherKinetics, 32);
        world.modifyBlockEntityNBT(transPos, AnalogTransmissionBlockEntity.class, (nbt) -> {
            nbt.getCompoundOrEmpty("ExtraCogwheel").putFloat("Speed", -32F);
        });
        scene.idle(48);
        world.setKineticSpeed(vehicleBelts, 0);

        scene.world().modifyBlockEntityNBT(select.position(3, 2, 10), AnalogLeverBlockEntity.class,
                nbt -> nbt.putInt("State", 15));

        world.toggleRedstonePower(lever);

        for (final ElementLink part : vehicleLink) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(part, new Vec3(0, 0, -0.25), 10, SmoothMovementUtils.quadraticRiseDual()));
        }

        scene.addInstruction(new StopBoreheadBearingAndContraptionInstruction(boreheadBearing, vehicle128Instr, false));
        scene.idle(30);

        overlay.showText(100)
                .text("Unlike regular Drills, Rock Cutting Wheels break blocks in a range around themselves")
                .pointAt(new Vec3(4.5, 3.5, 5))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(120);
    }

    public static void boreheadEfficiency(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();
        final VectorUtil vector = util.vector();

        scene.title("borehead_bearing_efficiency", "Borehead Bearing Efficiency");
        scene.addInstruction(new TranslateYSceneInstruction(-1, 0));

        scene.scaleSceneView(0.75f);

        final Selection firstRock = select.fromTo(util.grid().at(1, 1, 3), util.grid().at(5, 5, 4));
        final Selection secondRock = select.fromTo(util.grid().at(1, 1, 0), util.grid().at(5, 5, 1));

        final Selection bigBore = select.fromTo(util.grid().at(0, 3, 6), util.grid().at(6, 5, 6));
        final Selection rightBore = select.fromTo(util.grid().at(0, 3, 6), util.grid().at(2, 5, 6));
        final Selection leftBore = select.fromTo(util.grid().at(4, 3, 6), util.grid().at(6, 5, 6));
        final Selection bigBoreCenter = select.position(util.grid().at(3, 4, 6));

        final Selection singleBelts = select.fromTo(util.grid().at(2, 6, 5), util.grid().at(4, 6, 7));
        final Selection doubleBelts = select.fromTo(util.grid().at(0, 6, 7), util.grid().at(6, 6, 9)).substract(singleBelts);

        final Selection positiveKinetics = select.fromTo(util.grid().at(2, 0, 7), util.grid().at(4, 3, 7))
                .add(select.fromTo(util.grid().at(5, 4, 9), util.grid().at(6, 4, 9)))
                .add(select.fromTo(util.grid().at(0, 0, 9), util.grid().at(0, 3, 9)))
                .add(select.fromTo(util.grid().at(0, 6, 7), util.grid().at(0, 6, 9)));

        final Selection negativeKinetics = select.fromTo(util.grid().at(6, 0, 9), util.grid().at(6, 3, 9))
                .add(select.fromTo(util.grid().at(0, 4, 7), util.grid().at(4, 4, 9)))
                .add(select.fromTo(util.grid().at(6, 6, 7), util.grid().at(6, 6, 9)));

        final Selection singleKinetics = select.fromTo(util.grid().at(2, 0, 7), util.grid().at(4, 4, 7));
        final Selection doubleKinetics = select.fromTo(util.grid().at(0, 0, 9), util.grid().at(6, 4, 9));

        final BlockPos singleBorehead = util.grid().at(3, 4, 7);
        final BlockPos[] doubleBorehead = {util.grid().at(5, 4, 9), util.grid().at(1, 4, 9)};

        final BlockPos[] rockCutters = new BlockPos[4];
        for (int i = 0; i < 4; i++) {
            rockCutters[i] = util.grid().at(2 * i, 4, 6);
        }

        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();

        scene.idle(10);

        final ElementLink<WorldSectionElement> singleKineticsLink = world.showIndependentSection(singleKinetics.substract(select.position(singleBorehead)), Direction.DOWN);
        final ElementLink<WorldSectionElement> singleBeltsLink = world.showIndependentSectionImmediately(singleBelts);
        world.moveSection(singleBeltsLink, vector.of(0, -7, 0), 0);
        scene.idle(7);
        world.showSectionAndMerge(select.position(singleBorehead), Direction.DOWN, singleKineticsLink);
        scene.idle(7);
        final ElementLink<WorldSectionElement> bigBearingLink = world.showIndependentSection(bigBore, Direction.SOUTH);
        scene.idle(10);

        world.setKineticSpeed(positiveKinetics, 64);
        world.setKineticSpeed(negativeKinetics, -64);

        final ChangeBoreheadAndContraptionSpeedInstruction singleBoreInstr = new ChangeBoreheadAndContraptionSpeedInstruction(singleBorehead, bigBearingLink, Z, -64);
        scene.addInstruction(singleBoreInstr);

        for (int i = 1; i <= 8; i++) {
            scene.idle(4);
            spinRockCutters(scene, select.position(rockCutters[0]).add(select.position(rockCutters[1])), select.position(rockCutters[2]).add(select.position(rockCutters[3])), i, 1000);
        }

        world.showSection(firstRock, Direction.SOUTH);

        for (int i = 0; i < 10; i++) {
            scene.idle(15);
            for (final BlockPos stoneBlock : firstRock) {
                world.incrementBlockBreakingProgress(stoneBlock.immutable());
            }
            if (i == 1) {
                overlay.showText(100)
                        .text("The more blocks a Borehead Bearing attempts to break, the slower it will operate")
                        .colored(PonderPalette.RED)
                        .pointAt(vector.centerOf(util.grid().at(3, 3, 4)))
                        .attachKeyFrame()
                        .placeNearTarget();
            }
        }

        scene.idle(7);

        world.setKineticSpeed(positiveKinetics, 0);
        world.setKineticSpeed(negativeKinetics, 0);
        spinRockCutters(scene, select.position(rockCutters[0]).add(select.position(rockCutters[1])), select.position(rockCutters[2]).add(select.position(rockCutters[3])), 0, 0);

        scene.idle(30);

        scene.rotateCameraY(-90);
        scene.idle(1);
        scene.addInstruction(new TranslateYSceneInstruction(1, 20, SmoothMovementUtils.quadraticRiseDual()));

        scene.addInstruction((new FadeOutOfSceneInstruction<>(0, Direction.SOUTH, bigBearingLink)));

        final ElementLink<WorldSectionElement> boreCenterLink = world.showIndependentSectionImmediately(bigBoreCenter);
        final ElementLink<WorldSectionElement> leftBoreLink = world.showIndependentSectionImmediately(leftBore);
        final ElementLink<WorldSectionElement> rightBoreLink = world.showIndependentSectionImmediately(rightBore);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(singleKineticsLink, new Vec3(0, -1, 0), 19, SmoothMovementUtils.quadraticRiseDual()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(boreCenterLink, new Vec3(0, -1, 0), 19, SmoothMovementUtils.quadraticRiseDual()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(leftBoreLink, new Vec3(0, -1, 0), 19, SmoothMovementUtils.quadraticRiseDual()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(rightBoreLink, new Vec3(0, -1, 0), 19, SmoothMovementUtils.quadraticRiseDual()));

        scene.idle(20);

        world.hideIndependentSection(singleBeltsLink, Direction.SOUTH);
        world.hideIndependentSection(singleKineticsLink, Direction.SOUTH);
        world.hideIndependentSection(boreCenterLink, Direction.SOUTH);

        scene.idle(20);

        final ElementLink<WorldSectionElement> doubleKineticsLink = world.showIndependentSection(doubleKinetics, Direction.NORTH);
        final ElementLink<WorldSectionElement> doubleBeltLink = world.showIndependentSection(doubleBelts, Direction.NORTH);

        world.moveSection(doubleKineticsLink, vector.of(0, -1, -2), 0);
        world.moveSection(doubleBeltLink, vector.of(0, -7, -2), 0);

        scene.idle(10);

        world.setKineticSpeed(positiveKinetics, 64);
        world.setKineticSpeed(negativeKinetics, -64);

        final ChangeBoreheadAndContraptionSpeedInstruction leftBoreInstr = new ChangeBoreheadAndContraptionSpeedInstruction(doubleBorehead[0], leftBoreLink, Z, 64);
        scene.addInstruction(leftBoreInstr);
        final ChangeBoreheadAndContraptionSpeedInstruction rightBoreInstr = new ChangeBoreheadAndContraptionSpeedInstruction(doubleBorehead[1], rightBoreLink, Z, -64);
        scene.addInstruction(rightBoreInstr);
        for (int i = 1; i <= 8; i++) {
            scene.idle(4);
            spinRockCutters(scene, select.position(rockCutters[1]).add(select.position(rockCutters[2])), select.position(rockCutters[0]).add(select.position(rockCutters[3])), -i, 1000);
        }

        scene.rotateCameraY(90);
        scene.idle(1);
        scene.addInstruction(new TranslateYSceneInstruction(-0.5f, 20, SmoothMovementUtils.quadraticRiseDual()));

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(doubleBeltLink, new Vec3(0.62, 0, 0.18), 19, SmoothMovementUtils.quadraticRiseDual()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(doubleKineticsLink, new Vec3(0.5, 0, 0.25), 19, SmoothMovementUtils.quadraticRiseDual()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(leftBoreLink, new Vec3(0.5, 0, 0.25), 19, SmoothMovementUtils.quadraticRiseDual()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(rightBoreLink, new Vec3(0.5, 0, 0.25), 19, SmoothMovementUtils.quadraticRiseDual()));

        scene.idle(30);

        final ElementLink<WorldSectionElement> secondRockLink = world.showIndependentSection(secondRock, Direction.SOUTH);
        world.moveSection(secondRockLink, vector.of(0, 0, 3), 0);

        for (int i = 0; i < 9; i++) {
            scene.idle(5);
            for (final BlockPos stoneBlock : secondRock) {
                world.incrementBlockBreakingProgress(stoneBlock.immutable());
            }
            if (i == 1) {
                overlay.showText(80)
                        .text("Using multiple Borehead Bearings can offset this")
                        .colored(PonderPalette.GREEN)
                        .pointAt(vector.centerOf(util.grid().at(3, 3, 4)))
                        .attachKeyFrame()
                        .placeNearTarget();
            }
        }

        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.SOUTH, secondRockLink));

        for (final BlockPos blockPos : firstRock) {
            scene.addInstruction(new OffsetBreakParticlesInstruction(new AABB(blockPos), Blocks.STONE.defaultBlockState()));
        }
    }

    public static void spinRockCutters(final CreateSceneBuilder scene, final SceneBuildingUtil util, final BlockPos
            positiveWheel, final BlockPos negativeWheel, final int speed, final int duration) {
        spinRockCutters(scene, util.select().position(positiveWheel), util.select().position(negativeWheel), speed, duration);
    }

    public static void spinRockCutters(final CreateSceneBuilder scene, final Selection positiveWheels, final Selection negativeWheels,
                                       final int speed, final int duration) {
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        for (final BlockPos rockWheel : positiveWheels) {
            world.modifyBlockEntity(rockWheel, RockCuttingWheelBlockEntity.class, be -> {
                be.setAnimatedSpeed(-speed);
                be.setMaxDuration(duration);
            });
        }
        for (final BlockPos rockWheel : negativeWheels) {
            world.modifyBlockEntity(rockWheel, RockCuttingWheelBlockEntity.class, be -> {
                be.setAnimatedSpeed(speed);
                be.setMaxDuration(duration);
            });
        }
    }
}
