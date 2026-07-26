package dev.simulated_team.simulated.ponder.scenes;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.zurrtum.create.content.kinetics.press.PressingBehaviour;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import dev.simulated_team.simulated.client.BlockPropertiesTooltip;
import dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerBlockEntity;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.index.SimItems;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.instructions.*;
import dev.simulated_team.simulated.service.SimConfigService;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.ParrotElement;
import com.zurrtum.create.client.ponder.api.element.ParrotPose;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.scene.*;
import com.zurrtum.create.client.ponder.foundation.instruction.FadeOutOfSceneInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PhysicsAssemblerScenes {

    public static void physicsAssemblerIntro(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();
        final VectorUtil vector = util.vector();
        final EffectInstructions effects = scene.effects();

        scene.title("physics_assembler_intro", "Assembling Simulated Contraptions");
        scene.configureBasePlate(0, 0, 5);
        scene.setSceneOffsetY(-1);
        final ElementLink<WorldSectionElement> baseplate = world.showIndependentSection(select.layer(0), Direction.UP);

        final BlockPos assembler1 = new BlockPos(3, 2, 1);
        final BlockPos assembler2 = new BlockPos(1, 2, 2);

        final Selection structure = select.fromTo(3, 1, 1, 1, 4, 3);

        world.modifyBlockEntity(assembler2, PhysicsAssemblerBlockEntity.class, be -> {
            be.clientFlickLeverTo(true);
            be.jerkLever();
        });

        scene.idle(5);
        final ElementLink<WorldSectionElement> structureIntro1 = world.showIndependentSection(select.fromTo(3, 1, 1, 3, 1, 3), Direction.DOWN);
        scene.idle(5);
        final ElementLink<WorldSectionElement> structureIntro2 = world.showIndependentSection(select.fromTo(3, 2, 3, 3, 3, 3), Direction.DOWN);
        scene.idle(5);
        final ElementLink<WorldSectionElement> structureIntro3 = world.showIndependentSection(select.fromTo(1, 2, 3, 2, 3, 3).add(select.position(3, 4, 3)), Direction.DOWN);
        scene.idle(10);
        final ElementLink<WorldSectionElement> structureIntro4 = world.showIndependentSection(select.position(assembler1), Direction.DOWN);
        scene.idle(15);

        // evil structure gets assembled into a single one now so i don't hate myself as much later

        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, null, structureIntro1));
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, null, structureIntro2));
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, null, structureIntro3));
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, null, structureIntro4));

        final ElementLink<WorldSectionElement> assembledStructure = world.showIndependentSectionImmediately(structure.substract(select.position(assembler2)));

        scene.idle(5);

        overlay.showText(80)
                .text("The Physics Assembler assembles blocks into Simulated Contraptions")
                .pointAt(vector.centerOf(assembler1))
                .placeNearTarget();

        scene.idle(100);

        scene.addKeyframe();

        overlay.showControls(vector.centerOf(3, 3, 2), Pointing.RIGHT, 20).withItem(SimItems.HONEY_GLUE.asStack()).rightClick();

        scene.overlay().showOutline(PonderPalette.OUTPUT, "honey_glue", select.fromTo(3, 1, 1, 3, 1, 3)
                .add(select.fromTo(3, 2, 3, 3, 4, 3))
                .add(select.position(2, 3, 3))
                .add(select.position(1, 2, 3)), 100);

        scene.idle(20);

        overlay.showText(80)
                .text("Use Super Glue or Honey Glue to select a group of blocks...")
                .placeNearTarget()
                .colored(PonderPalette.OUTPUT)
                .pointAt(vector.blockSurface(new BlockPos(2, 3, 3), Direction.WEST));

        scene.idle(100);

        overlay.showControls(vector.centerOf(3, 2, 1), Pointing.DOWN, 20).rightClick();

        overlay.showText(90)
                .text("...then hold right-click and pull the lever to assemble")
                .placeNearTarget()
                .colored(PonderPalette.OUTPUT)
                .attachKeyFrame()
                .pointAt(vector.blockSurface(new BlockPos(3, 2, 1), Direction.WEST));

        world.configureCenterOfRotation(assembledStructure, new Vec3(3, 1, 3));
        scene.idle(3);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(assembler1, true, false));
        scene.idle(10);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(assembledStructure, new Vec3(0, 0, 27), 20, SmoothMovementUtils.cubicRise()));
        scene.idle(20);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(assembledStructure, new Vec3(0, 0, -3), 7, SmoothMovementUtils.quadraticJump()));
        scene.idle(7);

        scene.idle(80);

        overlay.showLine(PonderPalette.INPUT, new Vec3(1.1, 2.4, 3), new Vec3(1.1, 2.4, 4), 80);
//        overlay.showLine(PonderPalette.INPUT, new Vec3(1.5,3.75,3), new Vec3(1.5,3.75,4), 80);

        overlay.showText(80)
                .text("Note that blocks along diagonal edges are included")
                .colored(PonderPalette.INPUT)
                .placeNearTarget()
                .pointAt(new Vec3(1.1, 2.4, 3.5));

        scene.idle(110);

        world.setBlock(assembler1, Blocks.AIR.defaultBlockState(), false);

        scene.addInstruction(new OffsetBreakParticlesInstruction(AABB.unitCubeFromLowerCorner(new Vec3(2.5, 2, 1)), SimBlocks.PHYSICS_ASSEMBLER.getDefaultState()));

        scene.idle(20);

        overlay.showText(80)
                .text("Once assembled, the original Physics Assembler is no longer needed...")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(vector.blockSurface(new BlockPos(3, 2, 1), Direction.WEST));

        scene.idle(100);

        final ElementLink<WorldSectionElement> assembler2Section = world.showIndependentSection(select.position(assembler2), Direction.DOWN);
        world.configureCenterOfRotation(assembler2Section, new Vec3(3, 1, 3));
        world.rotateSection(assembler2Section, 0, 0, 27, 0);


        overlay.showText(80)
                .text("...and any Physics Assembler can be used for disassembly")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(vector.centerOf(new BlockPos(0, 1, 2)));

        scene.idle(40);

        overlay.showControls(new Vec3(1, 2, 2.5), Pointing.DOWN, 20).rightClick();
        scene.idle(3);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(assembler2, false, false));
        scene.idle(5);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(assembledStructure, new Vec3(0, 0, -27), 25, SmoothMovementUtils.quadraticRiseDual()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(assembler2Section, new Vec3(0, 0, -27), 25, SmoothMovementUtils.quadraticRiseDual()));

        scene.idle(25);

        scene.markAsFinished();

        scene.idle(100);

        overlay.showControls(vector.centerOf(4, 0, 0), Pointing.RIGHT, 10).withItem(SimItems.HONEY_GLUE.asStack()).rightClick();
        effects.emitParticles(vector.centerOf(4, 0, 0), effects.particleEmitterWithinBlockSpace(new DustParticleOptions(new Color(255, 232, 142).getRGB(), 1.0F), Vec3.ZERO), 10.0f, 2);
        final AABB funnyGlue = new AABB(new BlockPos(4, 0, 0));
        overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, funnyGlue, funnyGlue, 20);
        scene.idle(20);
        overlay.showControls(vector.blockSurface(new BlockPos(0, 4, 4), Direction.WEST), Pointing.LEFT, 10).withItem(SimItems.HONEY_GLUE.asStack()).rightClick();
        overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, funnyGlue, funnyGlue.expandTowards(-4, 4, 4), 30);

        scene.idle(30);

        overlay.showControls(vector.centerOf(1, 2, 2), Pointing.DOWN, 10).rightClick();

        scene.idle(3);

        scene.addInstruction(new PullTheAssemblerKronkInstruction(assembler2, true, false));
        scene.idle(10);

        scene.idle(5);

        scene.addInstruction(CustomMoveBaseShadowInstruction.delta(new Vec3(0, -20, 0), 55, SmoothMovementUtils.quadraticRise()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(assembledStructure, new Vec3(0, -20, 0), 55, SmoothMovementUtils.quadraticRise()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(assembler2Section, new Vec3(0, -20, 0), 55, SmoothMovementUtils.quadraticRise()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(baseplate, new Vec3(0, -20, 0), 55, SmoothMovementUtils.quadraticRise()));
    }

    public static void physicsAssemblerSimulatedContraptions(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();
        final VectorUtil vector = util.vector();
        final EffectInstructions effects = scene.effects();

        scene.title("physics_assembler_simulated_contraptions", "Interacting with Simulated Contraptions");
        scene.configureBasePlate(0, 0, 15);
        scene.scaleSceneView(0.5f);
        scene.showBasePlate();

        // World
        final BlockPos[] depot = {new BlockPos(10, 1, 4), new BlockPos(7, 1, 4), new BlockPos(4, 1, 4)};

        // Ice Car
        final BlockPos iceCarAssembler = new BlockPos(6, 2, 8);
        final BlockPos iceCarPortable = new BlockPos(7, 3, 8);

        final Selection iceCarLeftRedstone = select.fromTo(9, 2, 7, 9, 3, 7);
        final Selection iceCarRightRedstone = select.fromTo(9, 2, 9, 9, 3, 9);
        final Selection iceCarLeftKinetics = select.fromTo(9, 2, 7, 10, 2, 7);
        final Selection iceCarRightKinetics = select.fromTo(9, 2, 9, 10, 2, 9);

        final Selection iceCarSelection = select.fromTo(5, 1, 6, 10, 3, 10);

        // Car
        final BlockPos carAssembler = new BlockPos(7, 6, 6);
        final BlockPos carPortable = new BlockPos(7, 6, 10);

        final BlockPos[] carBearing = {
                new BlockPos(6, 5, 6),
                new BlockPos(8, 5, 6),
                new BlockPos(6, 5, 10),
                new BlockPos(8, 5, 10)
        };

        final Selection[] carWheelSelection = {
                select.fromTo(5, 4, 5, 5, 6, 7),
                select.fromTo(9, 4, 5, 9, 6, 7),
                select.fromTo(5, 4, 9, 5, 6, 11),
                select.fromTo(9, 4, 9, 9, 6, 11)
        };

        final Selection carBodySelection = select.fromTo(6, 5, 6, 8, 7, 10);

        // Pressmobile
        final BlockPos pressmobileAssembler = new BlockPos(8, 9, 9);
        final BlockPos pressmobilePortable = new BlockPos(8, 10, 8);
        final BlockPos pressmobilePress = new BlockPos(8, 10, 3);

        final Selection pressmobileSelection = select.fromTo(4, 8, 3, 10, 10, 10);

        // Glider

        final BlockPos gliderAssembler = new BlockPos(6, 12, 7);
        final BlockPos gliderPortable = new BlockPos(9, 12, 7);
        final Selection gliderRedstone = select.fromTo(10, 12, 6, 10, 12, 7);

        final Selection gliderSelection = select.fromTo(3, 11, 4, 12, 13, 10);

        // Misc Setup

        world.setKineticSpeed(select.position(iceCarPortable), 32);
        world.setKineticSpeed(select.position(carPortable), -32);
        world.setKineticSpeed(select.position(pressmobilePortable), -32);
        world.setKineticSpeed(select.position(gliderPortable), -32);

        scene.addInstruction(new PullTheAssemblerKronkInstruction(iceCarAssembler, true, true));
        scene.addInstruction(new PullTheAssemblerKronkInstruction(carAssembler, true, true));
        scene.addInstruction(new PullTheAssemblerKronkInstruction(pressmobileAssembler, true, true));
        scene.addInstruction(new PullTheAssemblerKronkInstruction(gliderAssembler, true, true));

        scene.idle(10);

        final ElementLink<WorldSectionElement> iceCar = world.showIndependentSection(iceCarSelection, Direction.WEST);
        world.rotateSection(iceCar, 0, -60, 0, 0);
        world.moveSection(iceCar, vector.of(5, 0, 3), 0);


        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(iceCar, new Vec3(-3, 0, 0), 60, SmoothMovementUtils.quadraticRiseDual()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(iceCar, new Vec3(-10, 0, 0), 60, SmoothMovementUtils.quadraticRiseInOut()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(iceCar, new Vec3(5, 0, 0), 75, SmoothMovementUtils.quadraticRise()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(iceCar, new Vec3(0, 0, -9), 45, SmoothMovementUtils.quadraticRiseDual()));

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(iceCar, new Vec3(0, 200, 0), 45, SmoothMovementUtils.cubicSmoothing()));

        scene.idle(10);

        world.toggleRedstonePower(iceCarRightRedstone);
        world.setKineticSpeed(iceCarRightKinetics, -128);
        effects.indicateRedstone(new BlockPos(12, 2, 9));

        scene.idle(20);

        world.toggleRedstonePower(iceCarRightRedstone);
        world.setKineticSpeed(iceCarRightKinetics, 128);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(iceCar, new Vec3(0, 0, 15), 45, SmoothMovementUtils.quadraticRise()));

        scene.idle(15);

        effects.indicateRedstone(new BlockPos(1, 3, 4));
        world.toggleRedstonePower(iceCarLeftRedstone);
        world.setKineticSpeed(iceCarLeftKinetics, -128);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(iceCar, new Vec3(0, -45, 0), 45, SmoothMovementUtils.cubicSmoothing()));

        scene.idle(10);

        world.toggleRedstonePower(iceCarLeftRedstone);
        world.setKineticSpeed(iceCarLeftKinetics, 128);

        world.hideIndependentSection(iceCar, Direction.SOUTH);

        final ElementLink<WorldSectionElement> carBody = world.showIndependentSection(carBodySelection, Direction.DOWN);

        final List<ElementLink<WorldSectionElement>> carWheel = new ArrayList<>(List.of());
        for (final Selection wheel : carWheelSelection) {
            carWheel.add(world.showIndependentSection(wheel, Direction.DOWN));
        }

        final List<ElementLink<WorldSectionElement>> carParts = new ArrayList<>(List.of(carBody));
        carParts.addAll(carWheel);

        for (final ElementLink<WorldSectionElement> part : carParts) {
            world.moveSection(part, vector.of(2, -2, 2.5), 0);
        }

        for (final BlockPos bearing : carBearing) {
            world.rotateBearing(bearing, -360, 75);
        }

        for (final ElementLink wheel : carWheel) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(wheel, new Vec3(-360, 0, 0), 75, SmoothMovementUtils.linear()));
        }

        for (final ElementLink carPart : carParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(carPart, new Vec3(0, 0, -4), 30, SmoothMovementUtils.quadraticRise()));
        }

        scene.idle(30);

        for (final ElementLink<WorldSectionElement> part : carParts) {
            world.moveSection(part, new Vec3(0, 0, -6), 25);
        }

        scene.idle(15);

        final ElementLink<WorldSectionElement> glider = world.showIndependentSection(gliderSelection, Direction.EAST);

        world.moveSection(glider, vector.of(-10, -3, 4), 0);
        world.rotateSection(glider, 0, 180, -10, 0);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(glider, new Vec3(4, -2, 0), 15, SmoothMovementUtils.quadraticRise()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(glider, new Vec3(0, 0, -5), 15, SmoothMovementUtils.quadraticRiseInOut()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(glider, new Vec3(0, 0, 10), 60, SmoothMovementUtils.quadraticRiseInOut()));

        scene.idle(10);

        // Car falling stuff start

        final float n = 1 / 2f;
        final FloatUnaryOperator angleFunctionDown = t -> (float) Math.sin(Math.PI * n * SmoothMovementUtils.cubicRise().apply(t));
        final FloatUnaryOperator angleFunctionHorizontal = t -> (float) Math.sin(Math.PI * n * SmoothMovementUtils.linear().apply(t));

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(carBody, new Vec3(0, 0, -3), 30, angleFunctionHorizontal));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(carBody, new Vec3(0, -3, 0), 30, angleFunctionDown));

        for (final ElementLink carPart : carParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(carPart, new Vec3(0, 0, -4), 30, SmoothMovementUtils.linear()));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(carPart, new Vec3(-45, 0, 0), 30, SmoothMovementUtils.quadraticRise()));
        }

        final ElementLink[] frontWheels = {carWheel.get(0), carWheel.get(1)};
        final ElementLink[] backWheels = {carWheel.get(2), carWheel.get(3)};
        for (final ElementLink wheel : frontWheels) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(wheel, new Vec3(0, 0, -2.65), 30, angleFunctionHorizontal));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(wheel, new Vec3(0, -4.3, 0), 30, angleFunctionDown));
        }
        for (final ElementLink wheel : backWheels) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(wheel, new Vec3(0, 0, -2.75), 30, angleFunctionHorizontal));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(wheel, new Vec3(0, -1.45, 0), 30, angleFunctionDown));
        }

        scene.idle(5);

        for (final ElementLink carPart : carParts) {
            world.hideIndependentSection(carPart, Direction.DOWN);
        }

        // Car falling stuff end


        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(glider, new Vec3(25, -1, 0), 80, SmoothMovementUtils.quadraticRiseDual()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(glider, new Vec3(0, -3, 0), 20, SmoothMovementUtils.quadraticRiseDual()));

        scene.idle(10);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(glider, new Vec3(0, 0, -10), 80, SmoothMovementUtils.quadraticRise()));

        scene.idle(10);

        world.hideIndependentSection(glider, Direction.EAST);

        scene.idle(10);

        // Commented bits over pressmobile segment are for 3 depots instead of one. Changed for pacing

        final ElementLink<WorldSectionElement> pressmobile = world.showIndependentSection(pressmobileSelection, Direction.DOWN);
//        world.moveSection(pressmobile, vector.of(4,-7,1),0);
        world.moveSection(pressmobile, vector.of(2, -7, 1), 0);


        scene.special().movePointOfInterest(new BlockPos(0, 3, 8));
        final ElementLink<ParrotElement> seatBirb = scene.special().createBirb(vector.of(8, 3, 9), ParrotPose.FacePointOfInterestPose::new);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(pressmobile, new Vec3(-0.66, 0, 0), 20, SmoothMovementUtils.quadraticRise()));
        scene.addInstruction(CustomAnimateParrotInstruction.move(seatBirb, new Vec3(-0.66, 0, 0), 20, SmoothMovementUtils.quadraticRise()));

        final Class<MechanicalPressBlockEntity> type = MechanicalPressBlockEntity.class;
        final ItemStack iron = new ItemStack(Items.IRON_INGOT);
        final ItemStack sheet = AllItems.IRON_SHEET.getDefaultInstance();

//        for (int i = 0; i < 3; i++) {
//            world.showSection(util.select().position(depot[i]), Direction.DOWN);
//            scene.idle(10);
//            scene.world().createItemOnBeltLike(depot[i], Direction.NORTH, iron);
//        }
        world.showSection(util.select().position(depot[1]), Direction.DOWN);
        scene.world().createItemOnBeltLike(depot[1], Direction.NORTH, iron);

        scene.idle(20);

        scene.addKeyframe();

        world.moveSection(pressmobile, vector.of(-10, 0, 0), 160);
        scene.special().moveParrot(seatBirb, vector.of(-10, 0, 0), 160);

//        for (int i = 0; true; i++) {
//            int finalI = i;
//
//            scene.world().modifyBlockEntity(pressmobilePress, type, pte -> pte.getPressingBehaviour()
//                    .start(PressingBehaviour.Mode.BELT));
//
//            scene.idle(15);
//
//            scene.world().modifyBlockEntity(pressmobilePress, type, pte -> pte.getPressingBehaviour()
//                    .makePressingParticleEffect(vector.centerOf(depot[finalI]).add(0, 8 / 16f, 0), iron));
//
//            scene.world().removeItemsFromBelt(depot[i]);
//            scene.world().createItemOnBeltLike(depot[i], Direction.UP, sheet);
//
//            if (i == 2) break;
//
//            scene.idle(33);
//        }

        scene.idle(20);

        scene.world().modifyBlockEntity(pressmobilePress, type, pte -> pte.getPressingBehaviour()
                .start(PressingBehaviour.Mode.BELT));

        scene.idle(15);

        scene.world().modifyBlockEntity(pressmobilePress, type, pte -> pte.getPressingBehaviour()
                .makePressingParticleEffect(vector.centerOf(depot[1]).add(0, 8 / 16f, 0), iron));

        scene.world().removeItemsFromBelt(depot[1]);
        scene.world().createItemOnBeltLike(depot[1], Direction.UP, sheet);

        scene.idle(30);


//        for (int j = 0; j < 3; j++) {
//            world.hideSection(util.select().position(depot[j]), Direction.UP);
//        }

        //scene.idle(20);

        final ElementLink<WorldSectionElement> glider2 = world.showIndependentSection(gliderSelection, Direction.EAST);

        world.moveSection(glider2, vector.of(15, -8, -10), 0);
        world.rotateSection(glider2, 0, 30, -5, 0);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(glider2, new Vec3(-15, -2, 11), 55, SmoothMovementUtils.quadraticRiseDual()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(glider2, new Vec3(0, 0, -5), 25, SmoothMovementUtils.quadraticRiseDual()));

        world.hideSection(util.select().position(depot[1]), Direction.UP);
        world.hideIndependentSection(pressmobile, Direction.UP);
        scene.special().hideElement(seatBirb, Direction.UP);


        scene.idle(25);


        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(glider2, new Vec3(0, 0, 10), 25, SmoothMovementUtils.quadraticRiseDual()));
        scene.idle(40);

        overlay.showControls(vector.of(10, 2.5, 7), Pointing.DOWN, 20).rightClick();

        scene.idle(5);

        effects.indicateRedstone(new BlockPos(6, 6, 2));
        world.toggleRedstonePower(gliderRedstone);

        scene.idle(20);

        overlay.showText(120)
                .text("Simulated Contraptions have physics, and remain completely interactable when assembled")
                .pointAt(vector.centerOf(7, 2, 7));

        scene.idle(60);
        scene.markAsFinished();
    }

    public static void physicsAssemblerBlockProperties(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();
        final VectorUtil vector = util.vector();
        final EffectInstructions effects = scene.effects();

        scene.title("physics_assembler_block_properties", "Physical properties of blocks");
        scene.configureBasePlate(0, 0, 5);
        scene.setSceneOffsetY(-0.5f);
        scene.showBasePlate();

        final BlockPos assemblerPos = new BlockPos(2, 3, 2);
        final BlockPos blockRightPos = new BlockPos(0, 3, 2);
        final BlockPos blockLeftPos = new BlockPos(4, 3, 2);

        final Selection balanceBar = select.fromTo(2, 1, 0, 2, 1, 4);
        final Selection scaleSelection = select.fromTo(0, 2, 2, 4, 2, 2);

        scene.idle(5);

        world.showSection(balanceBar, Direction.DOWN);

        scene.idle(5);

        final ElementLink<WorldSectionElement> scale = world.showIndependentSection(scaleSelection, Direction.DOWN);

        scene.idle(5);

        final ElementLink<WorldSectionElement> assembler = world.showIndependentSection(util.select().position(assemblerPos), Direction.DOWN);

        scene.idle(10);

        overlay.showControls(vector.topOf(assemblerPos), Pointing.DOWN, 20).rightClick();

        scene.idle(3);

        scene.addInstruction(new PullTheAssemblerKronkInstruction(assemblerPos, true, false));
        scene.idle(10);

        scene.idle(20);

        final ElementLink<WorldSectionElement> blockLeft = world.showIndependentSectionImmediately(util.select().position(blockLeftPos));
        final ElementLink<WorldSectionElement> blockRight = world.showIndependentSectionImmediately(util.select().position(blockRightPos));

        final List<ElementLink<WorldSectionElement>> scaleParts = List.of(scale, assembler, blockLeft, blockRight);

        world.moveSection(blockLeft, vector.of(0, 2, 0), 0);
        world.moveSection(blockRight, vector.of(0, 2, 0), 0);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(blockLeft, new Vec3(0, -2, 0), 15, SmoothMovementUtils.quadraticRise()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(blockRight, new Vec3(0, -2, 0), 15, SmoothMovementUtils.quadraticRise()));

        scene.idle(15);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(blockLeft, new Vec3(0, 0.05, 0), 4, SmoothMovementUtils.quadraticJump()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(blockRight, new Vec3(0, 0.05, 0), 4, SmoothMovementUtils.quadraticJump()));

        scene.idle(10);

        overlay.showText(65)
                .text("Some blocks may have different weights...")
                .pointAt(vector.centerOf(blockRightPos))
                .placeNearTarget();

        scene.idle(55);

        world.setBlock(blockLeftPos, Blocks.OAK_PLANKS.defaultBlockState(), true);
        scene.idle(2);
        world.setBlock(blockRightPos, Blocks.IRON_BLOCK.defaultBlockState(), true);

        for (final ElementLink<WorldSectionElement> scalePart : scaleParts) {
            world.configureCenterOfRotation(scalePart, vector.topOf(2, 1, 2));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(scalePart, new Vec3(0, 0, 23), 30, SmoothMovementUtils.quadraticRise()));
        }

        scene.idle(30);

        for (final ElementLink<WorldSectionElement> scalePart : scaleParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(scalePart, new Vec3(0, 0, -2), 15, SmoothMovementUtils.quadraticJump()));
        }

        scene.overlay().showText(50)
                .text("Light")
                .colored(PonderPalette.MEDIUM)
                .placeNearTarget()
                .pointAt(vector.of(3.9, 4, 2.5));

        scene.overlay().showText(50)
                .text("Super Heavy")
                .colored(PonderPalette.MEDIUM)
                .placeNearTarget()
                .pointAt(vector.of(0.2, 2.4, 2.5));

        scene.idle(70);

        world.hideIndependentSection(assembler, Direction.UP);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(blockLeft, new Vec3(-4, -1.7, 0), 60, SmoothMovementUtils.quadraticRise()));

        for (int i = 0; i < 5; i++) {
            world.setBlock(new BlockPos(4 - i, 2, 2), Blocks.ICE.defaultBlockState(), true);
            scene.idle(2);
        }

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(blockRight, new Vec3(-2, -1, 0), 40, SmoothMovementUtils.quadraticRise()));

        overlay.showText(110)
                .text("...or other special properties")
                .pointAt(vector.centerOf(1, 2, 2))
                .placeNearTarget();

        scene.idle(10);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(blockRight, new Vec3(0, 0, 45), 30, SmoothMovementUtils.cubicRise()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(blockRight, new Vec3(0, 1.5, 0), 30, SmoothMovementUtils.cubicRise()));
        scene.idle(15);
        world.hideIndependentSection(blockRight, Direction.DOWN);
        scene.idle(25);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(blockLeft, new Vec3(-1.1, -0.5, 0), 15, SmoothMovementUtils.quadraticRiseDual()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(blockLeft, new Vec3(0, 0, 45), 15, SmoothMovementUtils.cubicRise()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(blockLeft, new Vec3(0.5, -1.5, 0), 15, SmoothMovementUtils.cubicRise()));
        world.hideIndependentSection(blockLeft, Direction.DOWN);

        scene.overlay().showText(60)
                .text("Slippery")
                .colored(PonderPalette.MEDIUM)
                .placeNearTarget()
                .pointAt(vector.of(3.5, 3.5, 2.5));
        scene.idle(20);
        scene.overlay().showText(40)
                .text("Fragile")
                .colored(PonderPalette.MEDIUM)
                .placeNearTarget()
                .pointAt(vector.of(2.5, 3, 2.5));

        scene.idle(60);

        final BlockPropertiesTooltip.Condition configValue;
        if (SimConfigService.INSTANCE.clientLoaded()) {
            configValue = SimConfigService.INSTANCE.client().itemConfig.displayProperties.get();
        } else {
            configValue = null;
        }

        final String configText = switch (configValue) {
            case ALWAYS -> "property_tooltip_always";
            case SHIFT -> "property_tooltip_shift";
            case GOGGLES -> "property_tooltip_goggles";
            case SHIFT_GOGGLES -> "property_tooltip_shift_goggles";
            case NEVER -> "property_tooltip_never";
            case null, default -> "property_tooltip_how";
        };

        if (configValue == BlockPropertiesTooltip.Condition.GOGGLES || configValue == BlockPropertiesTooltip.Condition.SHIFT_GOGGLES) {
            overlay.showControls(vector.centerOf(2, 2, 2), Pointing.DOWN, 160).withItem(AllItems.GOGGLES.getDefaultInstance());
        }

        scene.overlay().showText(160)
                .sharedText(configText)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(vector.centerOf(2, 2, 2));

        scene.idle(60);

        scene.markAsFinished();
    }

    // To show you the power of slime balls, I sawed this boat in half!
    public static void physicsAssemblerSubLevelSplitting(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();
        final VectorUtil vector = util.vector();
        final EffectInstructions effects = scene.effects();

        scene.title("physics_assembler_sub_level_splitting", "Splitting and merging Simulated Contraptions");
        scene.configureBasePlate(0, 0, 9);
        scene.scaleSceneView(0.8f);
        scene.showBasePlate();

        final Selection baseplateHole = select.fromTo(1, 0, 1, 7, 0, 7);

        final Selection baseplateWalls = select.fromTo(1, 1, 8, 8, 8, 8).add(select.fromTo(8, 1, 1, 8, 8, 8));
        final Selection baseplateWater = select.fromTo(1, 7, 1, 8, 7, 8);

        final Selection raftFull = select.fromTo(2, 1, 2, 6, 3, 6);

        final Selection raftR = select.fromTo(2, 1, 2, 4, 3, 6);
        final Selection raftL = select.fromTo(5, 1, 2, 6, 3, 6);
        final Selection raftCenter = raftFull.substract(raftL).substract(raftR);

        final BlockPos assembler = new BlockPos(6, 3, 4);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(assembler, true, true));

        final ElementLink<WorldSectionElement> baseplateWallLink = scene.world().showIndependentSection(baseplateWalls, Direction.UP);
        world.moveSection(baseplateWallLink, new Vec3(0, -7, 0), 0);

        scene.idle(15);

        world.hideSection(baseplateHole, Direction.DOWN);

        scene.idle(25);

        final ElementLink<WorldSectionElement> baseplateWaterLink = scene.world().showIndependentSection(baseplateWater, Direction.UP);
        world.moveSection(baseplateWaterLink, new Vec3(0, -7, 0), 0);

        scene.idle(15);

        final ElementLink<WorldSectionElement> raftFullLink = scene.world().showIndependentSection(raftFull, Direction.NORTH);
        world.moveSection(raftFullLink, new Vec3(0, 2, 0), 0);
        world.rotateSection(raftFullLink, 15, 0, 0, 0);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(raftFullLink, new Vec3(0, -10, 0), 30, SmoothMovementUtils.quadraticRise()));

        scene.idle(15);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(raftFullLink, new Vec3(-25, 0, 10), 25, SmoothMovementUtils.quadraticRiseInOut()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(raftFullLink, new Vec3(0, 5.7, 0), 15, SmoothMovementUtils.quadraticRise()));

        scene.idle(10);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(raftFullLink, new Vec3(0, 1.25, 0), 28, SmoothMovementUtils.quadraticRiseInOut()));

        scene.idle(10);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(raftFullLink, new Vec3(15, 0, -15), 40, SmoothMovementUtils.quadraticRiseInOut()));

        scene.idle(20);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(raftFullLink, new Vec3(0, -0.75, 0), 50, SmoothMovementUtils.quadraticRiseInOut()));

        scene.idle(10);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(raftFullLink, new Vec3(-7.5, 0, 7.5), 40, SmoothMovementUtils.quadraticRiseInOut()));

        scene.idle(30);

        overlay.showText(80)
                .text("Disconnected sections of Simulated Contraptions will split off")
                .placeNearTarget()
                .pointAt(vector.centerOf(new BlockPos(4, 1, 4)));

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(raftFullLink, new Vec3(0, 0.25, 0), 40, SmoothMovementUtils.quadraticRiseInOut()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(raftFullLink, new Vec3(2.5, 0, -5), 40, SmoothMovementUtils.quadraticRiseInOut()));

        scene.idle(40);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(raftFullLink, new Vec3(0, -0.1, 0), 40, SmoothMovementUtils.quadraticRiseInOut()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(raftFullLink, new Vec3(0, 0, 1), 40, SmoothMovementUtils.quadraticRiseInOut()));

        scene.idle(10);

        for (int i = 0; i < 5; i++) {
            scene.idle(5);
            world.setBlock(new BlockPos(4, 2, 6 - i), Blocks.AIR.defaultBlockState(), false);
            scene.addInstruction(new OffsetBreakParticlesInstruction(AABB.unitCubeFromLowerCorner(new Vec3(4, 0.6, 6 - i)), Blocks.STRIPPED_OAK_LOG.defaultBlockState()));
        }

        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, null, raftFullLink));

        final ElementLink<WorldSectionElement> raftLLink = world.showIndependentSectionImmediately(raftL);
        final ElementLink<WorldSectionElement> raftRLink = world.showIndependentSectionImmediately(raftR);
        final List<ElementLink<WorldSectionElement>> raftParts = List.of(raftLLink, raftRLink);

        // magic numbers to align with final rotation after physics
        world.moveSection(raftLLink, new Vec3(0, -1.69, 0), 0);
        world.moveSection(raftRLink, new Vec3(0, -1.625, 0), 0);

        for (int i = 0; i < 2; i++) {
            final int side = i * 2 - 1;
            final ElementLink<WorldSectionElement> raftHalf = raftParts.get(i);

            world.rotateSection(raftHalf, 0, 0, -1.5, 0);

            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(raftHalf, new Vec3(0, 0, 5 * side), 40, SmoothMovementUtils.quadraticRiseInOut()));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(raftHalf, new Vec3(0, 0, 5 * side), 80, SmoothMovementUtils.cubicSmoothing()));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(raftHalf, new Vec3(-0.15 * side, -0.1, 0), 100, SmoothMovementUtils.cubicSmoothing()));
        }

        scene.idle(80);

        // let's see you take a crack at it wise guy
//        for (int i = 0; i < 2; i++) {
//            overlay.showLine(PonderPalette.INPUT, new Vec3(1.8, 1.1, 2.95 + 3 * i), new Vec3(2.75, 1.25, 2.95 + 3 * i), 80);
//            overlay.showLine(PonderPalette.INPUT, new Vec3(6.3, 1.25, 2.95 + 3 * i), new Vec3(7.2, 1.05, 2.95 + 3 * i), 80);
//            for (int j = 0; j < 2; j++) {
//                overlay.showLine(PonderPalette.INPUT, new Vec3(2.75 + 3.5 * j, 1.25, 2 + 4 * i), new Vec3(2.75 + 3.5 * j, 1.25, 3 + 4 * i), 80);
//            }
//        }
//
//        overlay.showText(75)
//                .text("Note that blocks along diagonal edges remain connected")
//                .colored(PonderPalette.INPUT)
//                .placeNearTarget()
//                .pointAt(vector.of(0.6, 2.4, 4.5));
//
//        scene.idle(110);

        overlay.showText(200)
                .text("A Slime Ball can be used to merge any two Simulated Contraptions")
                .colored(PonderPalette.GREEN)
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(vector.of(4.5, 1, 4.5));

        scene.idle(60);

        overlay.showControls(vector.of(5.2, 1.4, 4.5), Pointing.DOWN, 20).withItem(Items.SLIME_BALL.getDefaultInstance()).rightClick();
        scene.idle(6);
        scene.addInstruction(new OBBOutlineInstruction(new AABB(5, 0.2, 4.1, 5.5, 1.2, 4.8), vector.of(0, 0, 10), false, PonderPalette.GREEN, "slime1", 45));

        scene.idle(25);

        overlay.showControls(vector.of(3.8, 1.4, 4.5), Pointing.DOWN, 20).withItem(Items.SLIME_BALL.getDefaultInstance()).rightClick();
        scene.idle(6);
        scene.addInstruction(new OBBOutlineInstruction(new AABB(3.5, 0.2, 4.1, 4, 1.2, 4.8), vector.of(0, 0, -10), false, PonderPalette.GREEN, "slime2",15));

        scene.overlay().showLine(PonderPalette.GREEN, new Vec3(3.5,0.5,4.2), new Vec3(5.5,1.2,4.3), 30);
        scene.overlay().showBigLine(PonderPalette.GREEN, new Vec3(3.8,1.1,4.5), new Vec3(5.5,0.8,4.4), 30);
        scene.overlay().showLine(PonderPalette.GREEN, new Vec3(3.5,1.2,4.7), new Vec3(5.5,1.2,4.7), 30);

        scene.idle(10);

        for (int i = 0; i < 2; i++) {
            final int side = i * 2 - 1;
            final ElementLink<WorldSectionElement> raftHalf = raftParts.get(i);

            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(raftHalf, new Vec3(0, 0, -10 * side), 30, SmoothMovementUtils.quadraticRise()));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(raftHalf, new Vec3(0.65 * side, 0.1, 0), 30, SmoothMovementUtils.quadraticRise()));
        }
        // minor adjustments to get them to line up proper
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(raftRLink, new Vec3(0,-0.02,0), 30, SmoothMovementUtils.quadraticRise()));

        scene.idle(30);

        for (final ElementLink<WorldSectionElement> raftHalf : raftParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(raftHalf, new Vec3(0, -0.3, -0.01), 50, SmoothMovementUtils.quadraticRiseDual()));
        }

        scene.idle(40);

        scene.markAsFinished();

        for (final ElementLink<WorldSectionElement> raftHalf : raftParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(raftHalf, new Vec3(0, 0.2, 0), 80, SmoothMovementUtils.quadraticRiseInOut()));
        }
    }
}
