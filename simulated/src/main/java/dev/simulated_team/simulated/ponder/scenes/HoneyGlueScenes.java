package dev.simulated_team.simulated.ponder.scenes;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock;
import com.zurrtum.create.content.contraptions.piston.PistonExtensionPoleBlock;
import com.zurrtum.create.content.kinetics.transmission.ClutchBlock;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import dev.simulated_team.simulated.index.SimItems;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.elements.KeybindWindowElement;
import dev.simulated_team.simulated.ponder.instructions.CustomAnimateWorldSectionInstruction;
import dev.simulated_team.simulated.ponder.instructions.PullTheAssemblerKronkInstruction;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.scene.*;
import com.zurrtum.create.client.ponder.foundation.instruction.AnimateWorldSectionInstruction;
import com.zurrtum.create.client.ponder.foundation.instruction.FadeOutOfSceneInstruction;
import com.zurrtum.create.client.ponder.foundation.instruction.ShowInputInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HoneyGlueScenes {

    private static KeybindWindowElement.Builder showControls(final SceneBuilder builder, final Vec3 sceneSpace, final Pointing direction, final int duration) {
        final KeybindWindowElement inputWindowElement = new KeybindWindowElement(sceneSpace, direction);
        builder.addInstruction(new ShowInputInstruction(inputWindowElement, duration));
        return inputWindowElement.builder();
    }

    public static void honeyGlueIntro(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final SelectionUtil select = util.select();
        final OverlayInstructions overlay = scene.overlay();
        final CreateSceneBuilder.EffectInstructions effects = scene.effects();
        final VectorUtil vector = util.vector();
        final PositionUtil grid = util.grid();

        scene.title("honey_glue_intro", "Attaching blocks using Honey Glue");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        final BlockPos assembler = grid.at(3, 2, 1);
        final Selection structure1 = select.fromTo(3, 1, 1, 3, 1, 3);
        final Selection structure2 = select.fromTo(1, 2, 3, 3, 3, 3);
        final Selection assembledStructure = select.fromTo(3, 1, 1, 1, 3, 3);


        final BlockPos largeCog = grid.at(2, 0, 5);
        final Selection kineticShafts = select.fromTo(1, 1, 2, 1, 1, 5);

        final BlockPos pistonBase = grid.at(1, 1, 2);
        final BlockPos movedBlock = grid.at(3, 1, 2);
        final BlockPos movedBlockElement = grid.at(3, 2, 2);
        final Selection pistonPole = select.fromTo(1, 2, 2, 3, 2, 2);

        world.showSection(structure1, Direction.DOWN);
        world.showSection(structure2, Direction.DOWN);

        scene.idle(15);
        overlay.showText(70)
                .attachKeyFrame()
                .text("Honey Glue is a convenient alternative to Super Glue for Simulated Contraptions")
                .placeNearTarget()
                .pointAt(vector.blockSurface(new BlockPos(3, 2, 2), Direction.WEST));
        scene.idle(80);
        overlay.showText(65)
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .text("Clicking two endpoints creates a new 'glued' area...")
                .placeNearTarget()
                .pointAt(vector.blockSurface(new BlockPos(3, 2, 2), Direction.WEST));
        overlay.showControls(vector.centerOf(3, 1, 1), Pointing.RIGHT, 20).withItem(SimItems.HONEY_GLUE.asStack()).rightClick();
        scene.idle(6);
        effects.emitParticles(new Vec3(3, 1, 1), effects.particleEmitterWithinBlockSpace(new DustParticleOptions(new Color(255, 232, 142).getRGB(), 1.0F), Vec3.ZERO), 10.0f, 2);
        scene.idle(24);
        overlay.showControls(vector.centerOf(3, 3, 3), Pointing.RIGHT, 20).withItem(SimItems.HONEY_GLUE.asStack()).rightClick();
        scene.idle(6);
        final AABB bb = AABB.unitCubeFromLowerCorner(new Vec3(3, 1, 1));
        overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb, bb, 0);
        overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb, bb.expandTowards(0, 2, 2), 100);
        scene.idle(40);
        overlay.showControls(vector.centerOf(2, 2, 2), Pointing.LEFT, 20).withItem(SimItems.HONEY_GLUE.asStack()).scroll().whileCTRL();
        scene.idle(10);
        overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb, bb.expandTowards(-1, 2, 2), 15);
        scene.idle(15);
        overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb, bb.expandTowards(-2, 2, 2), 190);
        scene.idle(20);
        overlay.showText(50)
                .colored(PonderPalette.OUTPUT)
                .text("...which can be expanded or retracted via Ctrl-Scrolling")
                .placeNearTarget()
                .pointAt(new Vec3(2, 3, 2));
        scene.idle(70);
        world.hideSection(structure2, Direction.UP);
        scene.idle(20);
        scene.addKeyframe();
        showControls(builder, vector.blockSurface(new BlockPos(1, 3, 3), Direction.NORTH), Pointing.RIGHT, 80).withItem(SimItems.HONEY_GLUE.asStack()).rightClick().keybind("simulated.key.alt");
        final AABB whitebb = new AABB(new Vec3(1.05, 3.95, 3.05), new Vec3(1.05, 3.95, 3.05));
        overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, whitebb, whitebb, 0);
        overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, whitebb, whitebb.expandTowards(0.9, -0.9, 0.9), 80);
        overlay.showText(80)
                .colored(PonderPalette.WHITE)
                .text("Alternatively, hold ALT to place an endpoint midair")
                .placeNearTarget()
                .pointAt(vector.blockSurface(new BlockPos(1, 3, 3), Direction.WEST));
        scene.idle(100);
        world.showSection(structure2, Direction.DOWN);
        world.showSection(select.position(largeCog), Direction.DOWN);
        world.showSection(select.position(new BlockPos(2, 1, 2)), Direction.DOWN);
        world.showSection(kineticShafts, Direction.DOWN);
        final ElementLink<WorldSectionElement> pistonPoleElement =
                world.showIndependentSection(pistonPole, Direction.DOWN);
        world.moveSection(pistonPoleElement, vector.of(0, -1, 0), 0);
        scene.idle(20);
        overlay.showText(40)
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .text("Unlike Super Glue...")
                .placeNearTarget()
                .pointAt(vector.topOf(3, 1, 2));
        scene.idle(60);
        world.setBlock(movedBlock, Blocks.BARRIER.defaultBlockState(), false);
        world.setBlock(movedBlockElement, Blocks.SPRUCE_PLANKS.defaultBlockState(), false);

        world.modifyBlock(pistonBase, s -> s.setValue(MechanicalPistonBlock.STATE, MechanicalPistonBlock.PistonState.MOVING), false);
        world.setBlock(new BlockPos(1, 2, 2), AllBlocks.PISTON_EXTENSION_POLE.defaultBlockState().setValue(PistonExtensionPoleBlock.FACING, Direction.WEST), false);

        world.setKineticSpeed(kineticShafts, 32);
        world.setKineticSpeed(select.position(largeCog), -16);

        world.moveSection(pistonPoleElement, vector.of(-1, 0, 0), 20);

        scene.idle(10);
        overlay.showText(70)
                .colored(PonderPalette.OUTPUT)
                .text("...Honey Glue does not attach to animated Contraptions")
                .placeNearTarget()
                .pointAt(vector.topOf(1, 1, 2));
        scene.idle(70);
        world.setKineticSpeed(kineticShafts, -32);
        world.setKineticSpeed(select.position(largeCog), 16);
        world.moveSection(pistonPoleElement, vector.of(1, 0, 0), 20);
        scene.idle(20);
        world.setBlock(movedBlock, Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
        world.setBlock(movedBlockElement, Blocks.AIR.defaultBlockState(), false);
        world.setBlock(new BlockPos(1, 2, 2), Blocks.AIR.defaultBlockState(), false);
        world.modifyBlock(pistonBase, s -> s.setValue(MechanicalPistonBlock.STATE, MechanicalPistonBlock.PistonState.EXTENDED), false);
        scene.idle(20);
        world.hideIndependentSection(pistonPoleElement, Direction.UP);
        world.hideSection(kineticShafts, Direction.UP);
        world.hideSection(select.position(largeCog), Direction.UP);
        scene.idle(20);
        world.showSection(select.position(assembler), Direction.DOWN);
        scene.idle(20);
        overlay.showText(50)
                .attachKeyFrame()
                .text("Instead, it exclusively attaches to Simulated Contraptions")
                .placeNearTarget()
                .pointAt(vector.centerOf(assembler));
        scene.idle(60);
        overlay.showControls(vector.topOf(assembler), Pointing.DOWN, 25).rightClick();
        scene.idle(5);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(assembler, true,false));
        scene.idle(5);
        world.setBlock(new BlockPos(2, 2, 2), Blocks.AIR.defaultBlockState(), false);
        world.setBlocks(kineticShafts, Blocks.AIR.defaultBlockState(), false);
        final ElementLink<WorldSectionElement> assembledElement = world.makeSectionIndependent(assembledStructure);
        world.configureCenterOfRotation(assembledElement, new Vec3(3, 1, 2));
        scene.idle(5);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(assembledElement, new Vec3(0, 0, 45), 30, SmoothMovementUtils.cubicRise()));
        scene.idle(30);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(assembledElement, new Vec3(0, 0, -3), 7, SmoothMovementUtils.quadraticJump()));

    }

    public static void honeyGlueSuperGlue(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final SelectionUtil select = util.select();
        final OverlayInstructions overlay = scene.overlay();
        final CreateSceneBuilder.EffectInstructions effects = scene.effects();
        final VectorUtil vector = util.vector();
        final PositionUtil grid = util.grid();

        scene.title("honey_glue_super_glue", "Using Honey Glue with Super Glue");
        scene.configureBasePlate(0, 0, 9);
        scene.setSceneOffsetY(-1f);
        scene.scaleSceneView(0.7f);
        scene.showBasePlate();

        final BlockPos carAssembler = grid.at(4, 4, 2);
        final BlockPos carLever = grid.at(4, 5, 5);
        final BlockPos carPortable = grid.at(4, 4, 6);
        final BlockPos carClutch = grid.at(4, 4, 5);
        final BlockPos carGearbox = grid.at(4, 4, 4);

        final Selection carChassis = select.fromTo(3, 3, 1, 5, 5, 7);

        final BoundingBox carFull = new BoundingBox(2, 2, 1, 6, 5, 7);
        final BlockPos carMinCorner = new BlockPos(carFull.minX(), carFull.minY(), carFull.minZ());
        final BlockPos carMaxCorner = new BlockPos(carFull.maxX(), carFull.maxY() - 1, carFull.maxZ());

        final BoundingBox wheel1 = new BoundingBox(2, 2, 1, 2, 4, 3);
        final BoundingBox wheel2 = new BoundingBox(2, 2, 5, 2, 4, 7);
        final BoundingBox wheel3 = new BoundingBox(6, 2, 1, 6, 4, 3);
        final BoundingBox wheel4 = new BoundingBox(6, 2, 5, 6, 4, 7);

        final Selection platformLeft = select.fromTo(5, 6, 2, 7, 7, 4);
        final Selection platformLeftStatic = select.fromTo(5, 6, 5, 7, 6, 6);
        final Selection platformLeftFull = select.fromTo(5, 6, 2, 7, 7, 6);
        final Selection platformRight = select.fromTo(1, 6, 2, 3, 7, 6);

        world.setKineticSpeed(select.position(carPortable), -32);

        world.showSection(util.select().fromTo(carFull.minX(), carFull.minY(), carFull.minZ(), carFull.maxX(), carFull.maxY(), carFull.maxZ()), Direction.DOWN);

        scene.idle(10);

        final BoundingBox[] wheels = {wheel1, wheel2, wheel3, wheel4};
        for (int i = 0; i < wheels.length; i++) {
            final BoundingBox wheel = wheels[i];
            final AABB bb = new AABB(new BlockPos(wheel.minX(), wheel.minY(), wheel.minZ()));
            overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb, 5);
            overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb.expandTowards(0, 2, 2), 105 - i * 5);
            scene.idle(5);
        }
        scene.idle(10);
        overlay.showControls(vector.centerOf(carMinCorner), Pointing.RIGHT, 10).withItem(AllItems.SUPER_GLUE.getDefaultInstance()).rightClick();
        effects.indicateSuccess(carMinCorner);
        scene.idle(15);
        overlay.showControls(vector.centerOf(carMaxCorner), Pointing.RIGHT, 10).withItem(AllItems.SUPER_GLUE.getDefaultInstance()).rightClick();
        final AABB carGlueCorner = new AABB(vector.centerOf(carMinCorner).subtract(0.55, 0.55, 0.55), vector.centerOf(carMinCorner).add(0.55, 0.55, 0.55));
        final AABB carGlueFull = new AABB(vector.centerOf(carMinCorner).subtract(0.5, 0.5, 0.5), vector.centerOf(carMaxCorner).add(0.5, 0.5, 0.5));
        overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, carGlueCorner, carGlueCorner, 5);
        overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, carGlueCorner, carGlueCorner.expandTowards(4, 2, 6), 60);
        scene.idle(20);
        overlay.showText(50)
                .attachKeyFrame()
                .text("For more complex Simulated Contraptions...")
                .placeNearTarget()
                .pointAt(carFull.getCenter().getCenter());
        scene.idle(40);
        overlay.showControls(vector.centerOf(carAssembler), Pointing.DOWN, 10).rightClick();
        scene.idle(5);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(carAssembler, true,false));
        scene.idle(5);
        final ElementLink<WorldSectionElement> failureCar = world.makeSectionIndependent(util.select().fromTo(carFull.minX(), carFull.minY(), carFull.minZ(), carFull.maxX(), carFull.maxY(), carFull.maxZ()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(failureCar, new Vec3(0, -1, 0), 15, SmoothMovementUtils.cubicRise()));
        scene.idle(15);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(failureCar, new Vec3(0, 0.05, 0), 4, SmoothMovementUtils.quadraticJump()));
        scene.idle(10);
        overlay.showControls(vector.centerOf(new BlockPos(4, 4, 5)), Pointing.DOWN, 10).rightClick();
        scene.idle(6);
        effects.indicateRedstone(new BlockPos(4, 3, 5));
        world.modifyBlock(carLever, s -> s.setValue(LeverBlock.POWERED, Boolean.FALSE), false);
        world.modifyBlock(carClutch, s -> s.setValue(ClutchBlock.POWERED, Boolean.FALSE), false);
        world.setKineticSpeed(select.position(carGearbox), -32);
        final Vec3[] bearings = {new Vec3(3, 2, 2), new Vec3(5, 2, 2), new Vec3(3, 2, 6), new Vec3(5, 2, 6)};
        for (final Vec3 bearing : bearings) {
            effects.emitParticles(bearing, effects.particleEmitterWithinBlockSpace(ParticleTypes.CLOUD, new Vec3(0, 0, 0)), 10, 1);
            final AABB bb = new AABB(bearing, bearing.add(new Vec3(1, 1, 1)));
            overlay.chaseBoundingBoxOutline(PonderPalette.RED, bb, bb, 80);
        }
        scene.idle(20);
        overlay.showText(60)
                .attachKeyFrame()
                .text("...Super Glue may form undesired connections")
                .placeNearTarget()
                .pointAt(vector.blockSurface(new BlockPos(5, 2, 2), Direction.SOUTH));
        scene.idle(70);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(failureCar, new Vec3(0, 1, 0), 10, SmoothMovementUtils.linear()));
        world.modifyBlock(carLever, s -> s.setValue(LeverBlock.POWERED, Boolean.TRUE), false);
        world.modifyBlock(carClutch, s -> s.setValue(ClutchBlock.POWERED, Boolean.TRUE), false);
        world.setKineticSpeed(select.position(carGearbox), 0);

        scene.addInstruction(new PullTheAssemblerKronkInstruction(carAssembler, false,true));

        scene.idle(10);
        for (final BoundingBox wheel : wheels) {
            final AABB bb = new AABB(new BlockPos(wheel.minX(), wheel.minY(), wheel.minZ()));
            overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb.expandTowards(0, 2, 2), 125);
        }
        overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, carGlueFull, carGlueFull, 16);
        scene.idle(10);
        overlay.showControls(carFull.getCenter().getCenter(), Pointing.DOWN, 10).withItem(AllItems.SUPER_GLUE.getDefaultInstance()).leftClick();
        scene.idle(30);
        overlay.showText(75)
                .attachKeyFrame()
                .text("Honey Glue may be preferable in this scenario")
                .placeNearTarget()
                .pointAt(carFull.getCenter().getCenter());
        scene.idle(20);
        overlay.showControls(vector.centerOf(carMinCorner), Pointing.RIGHT, 10).withItem(SimItems.HONEY_GLUE.asStack()).rightClick();
        effects.emitParticles(vector.centerOf(carMinCorner), effects.particleEmitterWithinBlockSpace(new DustParticleOptions(new Color(255, 232, 142).getRGB(), 1.0F), Vec3.ZERO), 10.0f, 2);
        scene.idle(25);
        overlay.showControls(vector.centerOf(carMaxCorner), Pointing.RIGHT, 10).withItem(SimItems.HONEY_GLUE.asStack()).rightClick();
        overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, carGlueCorner, carGlueCorner, 5);
        overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, carGlueCorner, carGlueCorner.expandTowards(4, 2, 6), 40);
        scene.idle(45);
        overlay.showControls(vector.centerOf(carAssembler), Pointing.DOWN, 10).rightClick();
        scene.idle(5);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(carAssembler, true,false));
        scene.idle(5);
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, null, failureCar));

        final ElementLink<WorldSectionElement> assembledChassis = world.showIndependentSectionImmediately(carChassis);
        final ElementLink<WorldSectionElement> assembledWheel1 = world.showIndependentSectionImmediately(select.fromTo(wheel1.minX(), wheel1.minY(), wheel1.minZ(), wheel1.maxX(), wheel1.maxY(), wheel1.maxZ()));
        final ElementLink<WorldSectionElement> assembledWheel2 = world.showIndependentSectionImmediately(select.fromTo(wheel2.minX(), wheel2.minY(), wheel2.minZ(), wheel2.maxX(), wheel2.maxY(), wheel2.maxZ()));
        final ElementLink<WorldSectionElement> assembledWheel3 = world.showIndependentSectionImmediately(select.fromTo(wheel3.minX(), wheel3.minY(), wheel3.minZ(), wheel3.maxX(), wheel3.maxY(), wheel3.maxZ()));
        final ElementLink<WorldSectionElement> assembledWheel4 = world.showIndependentSectionImmediately(select.fromTo(wheel4.minX(), wheel4.minY(), wheel4.minZ(), wheel4.maxX(), wheel4.maxY(), wheel4.maxZ()));

        final ElementLink[] assembledWheels = {assembledWheel1, assembledWheel2, assembledWheel3, assembledWheel4};
        final ElementLink[] assembledCarParts = {assembledChassis, assembledWheel1, assembledWheel2, assembledWheel3, assembledWheel4};

        for (final ElementLink carPart : assembledCarParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(carPart, new Vec3(0, -1, 0), 15, SmoothMovementUtils.cubicRise()));
        }
        scene.idle(15);
        for (final ElementLink carPart : assembledCarParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(carPart, new Vec3(0, 0.05, 0), 4, SmoothMovementUtils.quadraticJump()));
            scene.addInstruction(AnimateWorldSectionInstruction.move(carPart, new Vec3(-0.2, 0.25, -0.28), 0));
        }
        scene.idle(10);
        overlay.showControls(vector.centerOf(new BlockPos(4, 4, 5)), Pointing.DOWN, 10).rightClick();
        scene.idle(6);
        effects.indicateRedstone(new BlockPos(4, 3, 5));
        world.modifyBlock(carLever, s -> s.setValue(LeverBlock.POWERED, Boolean.FALSE), false);
        world.modifyBlock(carClutch, s -> s.setValue(ClutchBlock.POWERED, Boolean.FALSE), false);
        world.setKineticSpeed(select.position(carGearbox), -32);
        for (final Vec3 bearing : bearings) {
            world.rotateBearing(new BlockPos((int) bearing.x, (int) bearing.y + 1, (int) bearing.z), -360, 75);
        }
        for (final ElementLink wheel : assembledWheels) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(wheel, new Vec3(-360, 0, 0), 75, SmoothMovementUtils.linear()));
        }
        for (final ElementLink carPart : assembledCarParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(carPart, new Vec3(0, 0, -4), 30, SmoothMovementUtils.quadraticRise()));
        }
        scene.idle(30);
        final float n = 1 / 2f;
        final FloatUnaryOperator angleFunctionDown = t -> (float) Math.sin(Math.PI * n * SmoothMovementUtils.cubicRise().apply(t));
        final FloatUnaryOperator angleFunctionHorizontal = t -> (float) Math.sin(Math.PI * n * SmoothMovementUtils.linear().apply(t));

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(assembledChassis, new Vec3(0, 0, -3), 30, angleFunctionHorizontal));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(assembledChassis, new Vec3(0, -3, 0), 30, angleFunctionDown));

        for (final ElementLink carPart : assembledCarParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(carPart, new Vec3(0, 0, -4), 30, SmoothMovementUtils.linear()));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(carPart, new Vec3(-45, 0, 0), 30, SmoothMovementUtils.quadraticRise()));
        }

        final ElementLink[] frontWheels = {assembledWheel1, assembledWheel3};
        final ElementLink[] backWheels = {assembledWheel2, assembledWheel4};
        for (final ElementLink wheel : frontWheels) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(wheel, new Vec3(0, 0, -2.65), 30, angleFunctionHorizontal));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(wheel, new Vec3(0, -4.3, 0), 30, angleFunctionDown));
        }
        for (final ElementLink wheel : backWheels) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(wheel, new Vec3(0, 0, -2.75), 30, angleFunctionHorizontal));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(wheel, new Vec3(0, -1.45, 0), 30, angleFunctionDown));
        }
        scene.idle(5);
        for (final ElementLink carPart : assembledCarParts) {
            world.hideIndependentSection(carPart, Direction.DOWN);
        }
        scene.idle(30);
        final ElementLink<WorldSectionElement> assembledPlatformLeftFull = world.showIndependentSection(platformLeftFull, Direction.DOWN);
        final ElementLink<WorldSectionElement> assembledPlatformRight = world.showIndependentSection(platformRight, Direction.DOWN);
        scene.addInstruction(AnimateWorldSectionInstruction.move(assembledPlatformRight, new Vec3(0, -3, 0), 0));
        scene.addInstruction(AnimateWorldSectionInstruction.move(assembledPlatformLeftFull, new Vec3(0, -3, 0), 0));
        scene.idle(20);
        overlay.showText(75)
                .attachKeyFrame()
                .text("When assembled, Honey Glue will attach to overlapping Super Glue")
                .placeNearTarget()
                .pointAt(vector.topOf(new BlockPos(2, 3, 4)));
        scene.idle(40);
        final AABB honey1 = new AABB(3, 3, 6, 4, 4, 7);
        final AABB slime1 = new AABB(3, 3, 4, 4, 4, 5);
        final AABB honey2 = new AABB(7, 3, 4, 8, 4, 5);
        final AABB slime2 = new AABB(7, 3, 6, 8, 4, 7);
        overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, honey1, honey1, 0);
        overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, honey1, honey1.expandTowards(-2, 0, -2), 40);
        scene.idle(20);
        overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, slime1, slime1, 0);
        overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, slime1, slime1.expandTowards(-2, 0, -2), 20);
        scene.idle(20);
        overlay.showControls(vector.topOf(2, 4, 3), Pointing.DOWN, 10).rightClick();
        scene.idle(5);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(new BlockPos(2, 7, 3), true,false));
        scene.idle(5);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(assembledPlatformRight, new Vec3(0, -2, 0), 20, SmoothMovementUtils.quadraticRise()));
        scene.idle(20);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(assembledPlatformRight, new Vec3(0, 0.05, 0), 4, SmoothMovementUtils.quadraticJump()));
        scene.idle(30);
        overlay.showText(75)
                .colored(PonderPalette.RED)
                .text("However, Super Glue will not attach to overlapping Honey Glue")
                .placeNearTarget()
                .pointAt(vector.topOf(new BlockPos(6, 3, 4)));
        scene.idle(40);
        overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, slime2, slime2, 0);
        overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, slime2, slime2.expandTowards(-2, 0, -2), 40);
        scene.idle(20);
        overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, honey2, honey2, 0);
        overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, honey2, honey2.expandTowards(-2, 0, -2), 20);
        scene.idle(20);
        overlay.showControls(vector.topOf(6, 4, 3), Pointing.DOWN, 10).rightClick();
        scene.idle(5);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(new BlockPos(6, 7, 3), true,false));
        scene.idle(5);
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, null, assembledPlatformLeftFull));
        final ElementLink<WorldSectionElement> assembledPlatformLeft = world.showIndependentSectionImmediately(platformLeft);
        final ElementLink<WorldSectionElement> staticPlatformLeft = world.showIndependentSectionImmediately(platformLeftStatic);
        scene.addInstruction(AnimateWorldSectionInstruction.move(assembledPlatformLeft, new Vec3(0, -3, 0), 0));
        scene.addInstruction(AnimateWorldSectionInstruction.move(staticPlatformLeft, new Vec3(0, -3, 0), 0));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(assembledPlatformLeft, new Vec3(0, -2, 0), 20, SmoothMovementUtils.quadraticRise()));
        scene.idle(20);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(assembledPlatformLeft, new Vec3(0, 0.05, 0), 4, SmoothMovementUtils.quadraticJump()));
    }
}
