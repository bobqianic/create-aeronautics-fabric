package dev.simulated_team.simulated.ponder.scenes;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.simulated_team.simulated.content.blocks.analog_transmission.AnalogTransmissionBlockEntity;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlockEntity;
import dev.simulated_team.simulated.content.blocks.symmetric_sail.SymmetricSailBlock;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.mixin_interface.ponder.TextWindowElementExtension;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.instructions.*;
import dev.simulated_team.simulated.ponder.records.PonderLineRecord;
import dev.simulated_team.simulated.ponder.records.ScrollingSceneRecord;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.ParrotElement;
import com.zurrtum.create.client.ponder.api.element.ParrotPose;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.scene.*;
import com.zurrtum.create.client.ponder.foundation.element.TextWindowElement;
import com.zurrtum.create.client.ponder.foundation.instruction.RotateSceneInstruction;
import com.zurrtum.create.client.ponder.foundation.instruction.TextInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class SymmetricSailScenes {
    public static void symmetricSailMain(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();
        final VectorUtil vector = util.vector();
        final EffectInstructions effects = scene.effects();

        scene.title("symmetric_sail", "Using Sails and Symmetric Sails on Simulated Contraptions");
        final int offset = 87;

        scene.configureBasePlate(offset, 0, 9);
        scene.addInstruction(new TranslateYSceneInstruction(-1, 0));
        scene.addInstruction(new ScaleSceneInstruction(0.66f, 1));
        scene.removeShadow();

        final BlockPos assembler = new BlockPos(1 + offset, 3, 4);
        final BlockPos portableEngine = new BlockPos(5 + offset, 3, 4);

        final BlockPos steeringWheelPos = new BlockPos(7 + offset, 4, 4);
        final Selection steeringWheel = select.position(steeringWheelPos);
        final BlockPos bearing = new BlockPos(7 + offset, 3, 4);

        final Selection planeRudderSelection = select.fromTo(8 + offset, 2, 4, 9 + offset, 4, 4).add(select.position(bearing.below()));
        final Selection planeBodySelection = select.fromTo(offset - 1, 1, 0, 10 + offset, 4, 8).substract(planeRudderSelection).substract(select.fromTo(8 + offset, 2, 3, 8 + offset, 2, 4));

        final Selection baseplateStart = select.fromTo(offset, 0, 0, 8 + offset, 0, 8);
        final Selection baseplateLong = select.fromTo(0, 0, 0, offset - 1, 0, 8);

        final Selection baseplate2Long = select.fromTo(1, 1, 0, offset - 7, 1, 7);

        final ElementLink<WorldSectionElement> baseplate = scene.world().showIndependentSection(baseplateStart, Direction.UP);

        scene.addInstruction(new PullTheAssemblerKronkInstruction(assembler, true, true));

        scene.idle(10);

        final ElementLink<WorldSectionElement> planeBody = world.showIndependentSection(planeBodySelection, Direction.DOWN);
        final ElementLink<WorldSectionElement> planeRudder = world.showIndependentSection(planeRudderSelection, Direction.DOWN);

        scene.special().movePointOfInterest(new BlockPos(0, 3, 4));
        final ElementLink<ParrotElement> seatBirb = scene.special().createBirb(vector.of(offset - 0.5, 2.5, 4.5), ParrotPose.FacePointOfInterestPose::new);

        final List<ElementLink<WorldSectionElement>> planeParts = new ArrayList<>(List.of(planeBody, planeRudder));

        for (final ElementLink<WorldSectionElement> planePart : planeParts) {
            world.configureCenterOfRotation(planePart, vector.centerOf(bearing));
        }

        // takes a tick for it to realize the entity exists
        scene.idle(1);
        scene.addInstruction(new CustomParrotFlappingInstruction(seatBirb));
        scene.idle(19);

        scene.overlay().showControls(vector.of(9 + offset, 3.5, 4.5), Pointing.UP, 70)
                .withItem(SimBlocks.WHITE_SYMMETRIC_SAIL.asStack());

        scene.idle(2);

        overlay.showText(65)
                .text("Symmetric Sail")
                .pointAt(vector.of(9 + offset, 3.5, 4.5))
                .colored(PonderPalette.MEDIUM)
                .placeNearTarget();

        scene.idle(20);

        scene.overlay().showControls(vector.centerOf(5 + offset, 2, 2), Pointing.DOWN, 48)
                .withItem(AllBlocks.SAIL.asItem().getDefaultInstance());

        scene.idle(2);

        overlay.showText(43)
                .text("Regular Sail")
                .pointAt(vector.centerOf(5 + offset, 2, 2))
                .colored(PonderPalette.MEDIUM)
                .placeNearTarget();

        scene.idle(73);

        overlay.showText(90)
                .text("When moving on a Simulated Contraption, Regular Sails provide Lift")
                .pointAt(vector.topOf(4 + offset, 2, 4))
                .attachKeyFrame()
                .placeNearTarget();

        scene.idle(60);

        final ElementLink<WorldSectionElement> groundClose = scene.world().showIndependentSection(baseplateLong, Direction.UP);

        scene.idle(12);

        scene.overlay().showControls(vector.topOf(portableEngine), Pointing.DOWN, 20)
                .withItem(AllItems.BLAZE_CAKE.getDefaultInstance());
        scene.world().modifyBlockEntity(portableEngine, PortableEngineBlockEntity.class, be -> {
            be.openHatchOverride = true;
            be.hatchOpenTime = 1.0f;
            be.lastHatchOpenTime = 1.0f;
        });

        scene.idle(5);

        scene.world().modifyBlockEntity(portableEngine, PortableEngineBlockEntity.class, be -> {
            be.openHatchOverride = false;
            be.setCurrentBurnTime(1337);
            be.setSuperHeated(true);
        });
        world.cycleBlockProperty(portableEngine, AbstractFurnaceBlock.LIT);
        effects.emitParticles(vector.of(4.9 + offset, 3.2, 4.5), effects.simpleParticleEmitter(ParticleTypes.LAVA, Vec3.ZERO), 3, 1);

        setSymSailKinetics(scene, util, 64);

        scene.idle(3);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(baseplate, new Vec3(5, 0, 0), 60, SmoothMovementUtils.quadraticRise()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(groundClose, new Vec3(5, 0, 0), 60, SmoothMovementUtils.quadraticRise()));

        scene.idle(20);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(baseplate, new Vec3(15, 0, 0), 40, SmoothMovementUtils.quadraticRise()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(groundClose, new Vec3(15, 0, 0), 40, SmoothMovementUtils.quadraticRise()));

        scene.idle(40);

        final ElementLink<WorldSectionElement> groundFar = scene.world().showIndependentSectionImmediately(baseplateLong.add(baseplateStart));
        final List<ElementLink<WorldSectionElement>> groundParts = new ArrayList<>(List.of(groundClose, groundFar));

        world.moveSection(groundFar, new Vec3(-76, 0, 0), 0);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(baseplate, new Vec3(96, 0, 0), 120, SmoothMovementUtils.linear()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(groundClose, new Vec3(96, 0, 0), 120, SmoothMovementUtils.linear()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(groundFar, new Vec3(96, 0, 0), 120, SmoothMovementUtils.linear()));

        for (final ElementLink<WorldSectionElement> planePart : planeParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(planePart, new Vec3(0, 0, -12), 40, SmoothMovementUtils.quadraticRiseInOut()));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(planePart, new Vec3(0.5, 0.5, 0), 120, SmoothMovementUtils.quadraticRiseInOut()));
        }

        scene.addInstruction(CustomAnimateParrotInstruction.move(seatBirb, new Vec3(0, 1.75, 0), 40, SmoothMovementUtils.quadraticRiseInOut()));

        for (final ElementLink<WorldSectionElement> groundPart : groundParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(groundPart, new Vec3(0, -1, 0), 80, SmoothMovementUtils.quadraticRiseInOut()));
        }

        scene.idle(20);

        scene.addInstruction(new RotateSceneInstruction(35, 35, true));
        scene.addInstruction(new TranslateYSceneInstruction(-2, 20));

        scene.addInstruction(CustomAnimateParrotInstruction.move(seatBirb, new Vec3(0.4, 0, 0), 80, SmoothMovementUtils.cubicSmoothing()));

        scene.idle(20);

        scene.addInstruction(CustomAnimateParrotInstruction.move(seatBirb, new Vec3(0.1, -0.4, 0), 80, SmoothMovementUtils.quadraticRiseInOut()));

        for (final ElementLink<WorldSectionElement> planePart : planeParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(planePart, new Vec3(0, 0, 5), 80, SmoothMovementUtils.quadraticRiseInOut()));
        }

        scene.idle(60);

        overlay.showText(210)
                .text("Lift is generated relative to the speed of the Sail, and is applied perpendicular to its surface")
                .independent()
                .attachKeyFrame();

        scene.idle(20);

        world.hideIndependentSection(baseplate, Direction.DOWN);
        world.showSectionAndMerge(baseplateStart, Direction.UP, groundClose);

        final ScrollingSceneRecord scrollingScene = new ScrollingSceneRecord(scene, groundClose, groundFar, Direction.EAST, 96, 140);

        scene.addInstruction(new ScrollingSceneInstruction(scrollingScene, true));

        scene.idle(60);

        final int arrowHoldTicks = 200;
        final Vec3 arrowBase = vector.of(4, 3.5, 0);

        final PonderLineRecord[] thrustArrow = createArrow(arrowBase, 8, 7);
        final int thrustArrowColor = ForceGroups.PROPULSION.get().color();
        drawArrow(scene, thrustArrow, thrustArrowColor, 3, 20, arrowHoldTicks);
        linelessTextbox(scene,
                "Thrust",
                vector.of(1, 3.25, 3),
                PonderPalette.INPUT,
                120);

        scene.idle(20);

        final PonderLineRecord[] liftArrow = createArrow(arrowBase, 3, 97);
        final int liftArrowColor = ForceGroups.LIFT.get().color();
        drawArrow(scene, liftArrow, liftArrowColor, 3, 20, arrowHoldTicks - 30);
        linelessTextbox(scene,
                "Lift",
                vector.of(3.1, 5.25, 3),
                PonderPalette.INPUT,
                90);

        scene.idle(20);

        final PonderLineRecord[] gravityArrow = createArrow(arrowBase, 3, -90);
        final int gravityArrowColor = ForceGroups.GRAVITY.get().color();
        drawArrow(scene, gravityArrow, gravityArrowColor, 3, 20, arrowHoldTicks - 60);

        linelessTextbox(scene,
                "Gravity",
                vector.of(6, 2.5, 3),
                PonderPalette.GREEN,
                60);

        scene.idle(10);

        scene.addInstruction(new ScrollingSceneInstruction(scrollingScene, false));

        scene.idle(60);

        overlay.showText(60)
                .text("When enough Lift is generated...")
                .placeNearTarget()
                .colored(PonderPalette.GREEN)
                .pointAt(arrowBase.add(offset, 0, 0))
                .attachKeyFrame();

        scene.idle(60);

        final PonderLineRecord[] netForceArrow = createArrow(arrowBase, 7, 0);

        lerpArrow(scene, liftArrow, netForceArrow, liftArrowColor, 20);
        lerpArrow(scene, thrustArrow, netForceArrow, thrustArrowColor, 20);
        lerpArrow(scene, gravityArrow, netForceArrow, gravityArrowColor, 20);

        scene.idle(15);

        drawArrow(scene, netForceArrow, PonderPalette.INPUT.getColor(), 5, 10, 95);
        linelessTextbox(scene,
                "Direction of Travel",
                vector.of(2.5, 3, 3),
                PonderPalette.INPUT,
                100);

        scene.addInstruction(new ScrollingSceneInstruction(scrollingScene, true));

        scene.idle(20);

        linelessTextbox(scene,
                "...Flight can be achieved",
                vector.of(3.05, 5, 3),
                PonderPalette.GREEN,
                80);

        scene.idle(90);

        scene.addInstruction(new CustomParrotFlappingInstruction(seatBirb, 40, 40));
        scene.addInstruction(CustomAnimateParrotInstruction.move(seatBirb, vector.of(-3, 2, 0), 40, SmoothMovementUtils.quadraticRise()));

        scene.idle(20);

        scene.addInstruction(CustomAnimateParrotInstruction.move(seatBirb, vector.of(0, 0.5, 0), 10, SmoothMovementUtils.quadraticRise()));

        scene.idle(10);

        scene.addInstruction(CustomAnimateParrotInstruction.move(seatBirb, vector.of(0, 50, 0), 0, SmoothMovementUtils.quadraticRise()));

        scene.addInstruction(new RotateSceneInstruction(-35, 35, true));
        scene.addInstruction(new TranslateYSceneInstruction(-1, 20));

        scene.addInstruction(new ScrollingSceneInstruction(scrollingScene, false));

        scene.idle(20);

        final AABB bb1 = new AABB(8.5 + offset, 2.35, 4.35, 10.5 + offset, 5.35, 4.65);
        scene.addInstruction(new OBBOutlineInstruction(bb1, new Vec3(0, 0, 7), false, PonderPalette.RED, "rudderOutline", 80));

        overlay.showText(80)
                .text("Unlike Regular Sails, Symmetric Sails only produce Drag")
                .placeNearTarget()
                .colored(PonderPalette.RED)
                .pointAt(vector.centerOf(9 + offset, 4, 4))
                .attachKeyFrame();

        scene.idle(100);

        overlay.showText(80)
                .text("Note that when moving parallel to the direction of motion, they have no effect")
                .colored(PonderPalette.BLUE)
                .placeNearTarget()
                .pointAt(vector.centerOf(8 + offset, 4, 4));

        final AABB bb2 = new AABB(7.5, 3, 3, 8.5, 6, 5);
        Vec3 windDir = vector.of(3, 0, 0);
        for (int i = 0; i < 20; i++) {
            scene.addInstruction(new WindstreamInstruction(bb2, windDir, 1, PonderPalette.BLUE, String.valueOf(i), 10, 0));

            if (i == 5) {
                scene.addInstruction(new ScrollingSceneInstruction(scrollingScene, true));
            }

            scene.idle(4);
        }

        scene.idle(30);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(planeRudder, new Vec3(0, 30, 0), 50, SmoothMovementUtils.linear()));
        world.rotateBearing(bearing, 30, 50);
        world.setKineticSpeed(steeringWheel, 8);
        scene.addInstruction(SimAnimateBEInstruction.steeringWheel(steeringWheelPos, -30, 50));


        for (final ElementLink<WorldSectionElement> planePart : planeParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(planePart, new Vec3(0, -35, 7), 80, SmoothMovementUtils.quadraticRiseInOut()));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(planePart, new Vec3(-0.5, 0, 0.5), 80, SmoothMovementUtils.quadraticRiseInOut()));
        }

        for (final ElementLink<WorldSectionElement> groundPart : groundParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(groundPart, new Vec3(0, 0, 40), 180, SmoothMovementUtils.quadraticRiseInOut()));
        }

        scene.idle(50);

        scene.addInstruction(new ScrollingSceneInstruction(scrollingScene, false));

        world.setKineticSpeed(steeringWheel, 0);

        final AABB bbWindBackground = new AABB(20, 0, 20, -20, -5, -20);
        AABB bbWindDrag = new AABB(8, 3.5, 3, 9, 5.5, 5);
        Vec3 angledWindDir = vector.of(2, 0, 1.4);
        Matrix3d windRotationMatrix = new Matrix3d().rotateY(-Math.PI / 90);
        PonderPalette dragColor = PonderPalette.RED;

        final ElementLink<WorldSectionElement> ground2Close = scene.world().showIndependentSection(baseplate2Long, Direction.UP);
        final ElementLink<WorldSectionElement> ground2Far = scene.world().showIndependentSection(baseplate2Long, Direction.UP);

        world.moveSection(ground2Close, vector.of(30, -2, -40), 0);
        world.moveSection(ground2Far, vector.of(100, -2, -40), 0);

        final ScrollingSceneRecord scrollingScene2 = new ScrollingSceneRecord(scene, ground2Close, ground2Far, Direction.EAST, 70, 100);

        final List<ElementLink<WorldSectionElement>> ground2Parts = new ArrayList<>(List.of(ground2Close, ground2Far));

        for (int i = 0; i < 270; i++) {
            if (i % 2 == 0) {
                scene.addInstruction(new WindstreamInstruction(bbWindBackground, angledWindDir.scale(5), 1, PonderPalette.INPUT, String.valueOf(i), 10, 0));
            }

            if (i == 10) {
                scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(planeRudder, new Vec3(0, -30, 0), 20, SmoothMovementUtils.linear()));
                world.rotateBearing(bearing, -30, 20);
                world.setKineticSpeed(steeringWheel, -16);
                scene.addInstruction(SimAnimateBEInstruction.steeringWheel(steeringWheelPos, 30, 20));
            }

            if (i == 20) {
                world.setKineticSpeed(steeringWheel, 0);
                world.hideIndependentSection(groundClose, Direction.UP);
                world.hideIndependentSection(groundFar, Direction.UP);
            }

            if (i == 30) {
                scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(planeRudder, new Vec3(0, 30, 0), 20, SmoothMovementUtils.linear()));
                world.rotateBearing(bearing, 30, 20);
                world.setKineticSpeed(steeringWheel, 16);
                scene.addInstruction(SimAnimateBEInstruction.steeringWheel(steeringWheelPos, -30, 20));
            }

            if (((i > 30 && i < 100) || (i > 150 && i < 210)) && i % 2 == 0) {
                scene.addInstruction(new WindstreamInstruction(bbWindDrag, windDir, 1, dragColor, i + "b", 10, 0));
            }

            if (i == 40) {
                world.setKineticSpeed(steeringWheel, 0);

                overlay.showText(120)
                        .text("When angled away from the direction of motion, Drag is applied")
                        .colored(PonderPalette.RED)
                        .placeNearTarget()
                        .attachKeyFrame()
                        .pointAt(vector.centerOf(8 + offset, 4, 4));
            }

            if (i == 100) {
                for (final ElementLink<WorldSectionElement> planePart : planeParts) {
                    scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(planePart, new Vec3(0, -55, 0), 110, SmoothMovementUtils.quadraticRiseDual()));
                    scene.addInstruction(CustomAnimateWorldSectionInstruction.move(planePart, new Vec3(-2, 0, 0), 80, SmoothMovementUtils.cubicSmoothing()));
                }
            }

            if (145 > i && i >= 100) {
                windDir = JOMLConversion.toMojang(windRotationMatrix.transform(JOMLConversion.toJOML(windDir)));
                bbWindDrag = bbWindDrag.move(vector.of(-0.05, 0, 0));
            }

            if (i == 115) {
                dragColor = PonderPalette.BLUE;
            }

            if (128 > i && i >= 100) {
                angledWindDir = JOMLConversion.toMojang(windRotationMatrix.transform(JOMLConversion.toJOML(angledWindDir)));
            }

            if (i == 140) {
                scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(planeRudder, new Vec3(0, -30, 0), 20, SmoothMovementUtils.linear()));
                world.rotateBearing(bearing, -30, 20);
                world.setKineticSpeed(steeringWheel, -16);
                scene.addInstruction(SimAnimateBEInstruction.steeringWheel(steeringWheelPos, 30, 20));
            }

            if (i == 150) {
                world.setKineticSpeed(steeringWheel, 0);
            }

            if (i == 160) {
                overlay.showText(100)
                        .text("This can be used for turning or stabilization surfaces")
                        .colored(PonderPalette.BLUE)
                        .placeNearTarget()
                        .attachKeyFrame()
                        .pointAt(vector.centerOf(5 + offset, 4, 6));
            }

            if (i >= 215 && i <= 260) {
                angledWindDir = JOMLConversion.toMojang(windRotationMatrix.transform(JOMLConversion.toJOML(angledWindDir)));
            }

            if (i == 220) {
                windRotationMatrix = new Matrix3d().rotateY(Math.PI / 60);

                scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(planeRudder, new Vec3(0, -30, 0), 20, SmoothMovementUtils.linear()));
                world.rotateBearing(bearing, -30, 20);
                world.setKineticSpeed(steeringWheel, -16);
                scene.addInstruction(SimAnimateBEInstruction.steeringWheel(steeringWheelPos, 30, 20));

                for (final ElementLink<WorldSectionElement> planePart : planeParts) {
                    scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(planePart, new Vec3(0, 90, -7), 110, SmoothMovementUtils.cubicSmoothing()));
                    scene.addInstruction(CustomAnimateWorldSectionInstruction.move(planePart, new Vec3(2.5, 0, -0.5), 110, SmoothMovementUtils.cubicSmoothing()));
                }

                for (final ElementLink<WorldSectionElement> groundPart : ground2Parts) {
                    scene.addInstruction(CustomAnimateWorldSectionInstruction.move(groundPart, new Vec3(0, 0, 40), 140, SmoothMovementUtils.quadraticRiseInOut()));
                }

                scene.addInstruction(new ScrollingSceneInstruction(scrollingScene2, false));
            }

            if (i == 230) {
                world.setKineticSpeed(steeringWheel, 0);
            }

            if (i == 260) {
                scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(planeRudder, new Vec3(0, 30, 0), 20, SmoothMovementUtils.linear()));
                world.rotateBearing(bearing, 30, 20);
                world.setKineticSpeed(steeringWheel, 16);
                scene.addInstruction(SimAnimateBEInstruction.steeringWheel(steeringWheelPos, -30, 20));
            }

            scene.idle(2);
        }

        scene.addInstruction(new ScrollingSceneInstruction(scrollingScene2, true));
        world.setKineticSpeed(steeringWheel, 0);

        scene.addInstruction(new RotateSceneInstruction(0, -70, true));

        world.cycleBlockProperty(portableEngine, AbstractFurnaceBlock.LIT);
        setSymSailKinetics(scene, util, 0);

        scene.idle(30);

        for (final ElementLink<WorldSectionElement> planePart : planeParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(planePart, new Vec3(0, 0, -7), 50, SmoothMovementUtils.quadraticRiseInOut()));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(planePart, new Vec3(0, -2, 0), 80, SmoothMovementUtils.quadraticRise()));
        }

        scene.idle(70);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(ground2Far, new Vec3(140, 0, 0), 0, SmoothMovementUtils.linear()));

        for (final ElementLink<WorldSectionElement> groundPart : ground2Parts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(groundPart, new Vec3(55.5, 0, 0), 160, SmoothMovementUtils.quadraticRiseDual()));
        }

        for (final ElementLink<WorldSectionElement> planePart : planeParts) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(planePart, new Vec3(0, 0, 14), 30, SmoothMovementUtils.quadraticRiseDual()));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(planePart, new Vec3(0, 0.5, 0), 30, SmoothMovementUtils.quadraticRiseDual()));
        }

        scene.special().movePointOfInterest(new BlockPos(86, 2, -5));

        scene.idle(160);

        scene.markAsFinished();

        scene.addInstruction(new CustomParrotFlappingInstruction(seatBirb, 5, 60));

        scene.addInstruction(CustomAnimateParrotInstruction.move(seatBirb, vector.of(3, -50, 15), 0, SmoothMovementUtils.linear()));
        scene.addInstruction(CustomAnimateParrotInstruction.move(seatBirb, vector.of(0, -5, -15), 60, SmoothMovementUtils.quadraticRiseDual()));

        scene.idle(60);

        scene.addInstruction(new CustomParrotFlappingInstruction(seatBirb));
    }

    public static void setSymSailKinetics(final CreateSceneBuilder scene, final SceneBuildingUtil util, final float rpm) {
        for (int i = 0; i < 2; i++) {
            scene.world().modifyBlockEntityNBT(util.select().position(93, 2, 3 + (2 * i)), AnalogTransmissionBlockEntity.class, nbt -> {
                nbt.getCompoundOrEmpty("ExtraCogwheel").putFloat("Speed", rpm);
            });
        }

        final Selection inverseKinetics = util.select().position(93, 2, 4);
        final Selection kinetics = util.select().fromTo(92, 3, 4, 93, 3, 4).add(util.select().fromTo(93, 2, 5, 94, 2, 7)).substract(inverseKinetics);

        scene.world().setKineticSpeed(kinetics, rpm);
        scene.world().setKineticSpeed(inverseKinetics, -rpm);
    }

    public static PonderLineRecord[] createArrow(final Vec3 origin, final float length, final float angle) {
        final Vector3d jomlOrigin = JOMLConversion.toJOML(origin.add(87, 0, 0));

        final double radAngle = Math.toRadians(180 - angle);
        final Matrix3d rotMatrix = new Matrix3d().rotateZ(radAngle);

        final Vector3d shaftStart = new Vector3d().add(jomlOrigin);
        final Vector3d shaftEnd = rotMatrix.transform(new Vector3d(length, 0, 0)).add(jomlOrigin);

        final Vector3d pointStart = rotMatrix.transform(new Vector3d(length + 0.2, 0, 0)).add(jomlOrigin);
        final Vector3d p1End = rotMatrix.transform(new Vector3d(length - 0.3, 0.5, 0)).add(jomlOrigin);
        final Vector3d p2End = rotMatrix.transform(new Vector3d(length - 0.3, -0.5, 0)).add(jomlOrigin);

        return new PonderLineRecord[]{new PonderLineRecord(JOMLConversion.toMojang(shaftStart), JOMLConversion.toMojang(shaftEnd)),
                new PonderLineRecord(JOMLConversion.toMojang(pointStart), JOMLConversion.toMojang(p1End)),
                new PonderLineRecord(JOMLConversion.toMojang(pointStart), JOMLConversion.toMojang(p2End))};
    }

    public static void drawArrow(final CreateSceneBuilder scene, final PonderLineRecord[] arrowSet, final int color, final int size, final int lerpTicks, final int holdTicks) {
        final int lerpEach = lerpTicks / 2;
        scene.addInstruction(new ChasingLineInstruction(arrowSet[0], size, color, arrowSet[0].toString(), lerpEach, holdTicks + lerpEach, SmoothMovementUtils.quadraticRise()));
        scene.idle(lerpEach);
        scene.addInstruction(new ChasingLineInstruction(arrowSet[1], size, color, arrowSet[1].toString(), lerpEach, holdTicks, SmoothMovementUtils.quadraticRiseDual()));
        scene.addInstruction(new ChasingLineInstruction(arrowSet[2], size, color, arrowSet[2].toString(), lerpEach, holdTicks, SmoothMovementUtils.quadraticRiseDual()));
    }

    public static void lerpArrow(final CreateSceneBuilder scene, final PonderLineRecord[] arrowSetOld, final PonderLineRecord[] arrowSetNew, final int color, final int lerpTicks) {
        for (int i = 0; i < 3; i++) {
            scene.addInstruction(new ChasingLineInstruction(arrowSetOld[i], arrowSetNew[i], 3, color, arrowSetOld[i].toString(), lerpTicks, 0, SmoothMovementUtils.cubicSmoothing()));
        }
    }

    public static void linelessTextbox(final CreateSceneBuilder scene, final String text, final Vec3 position, final PonderPalette color, final int ticks) {
        final TextWindowElement textWindowElement = new TextWindowElement();

        scene.addInstruction(new TextInstruction(textWindowElement, ticks));

        ((TextWindowElementExtension) textWindowElement).simulated$hidePointer();

        textWindowElement.builder(scene.getScene())
                .text(text)
                .colored(color)
                .pointAt(position.add(87, 0, 0))
                .placeNearTarget();
    }

    // Scene 2, mostly copied from Create

    public static void symmetricSailWindmill(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("symmetric_sail_windmill", "Assembling Windmills using Symmetric Sails");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.9f);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);

        final BlockPos bearingPos = util.grid().at(2, 1, 2);
        scene.world().showSection(util.select().position(bearingPos), Direction.DOWN);
        scene.idle(5);
        final ElementLink<WorldSectionElement> plank =
                scene.world().showIndependentSection(util.select().position(bearingPos.above()), Direction.DOWN);
        scene.idle(10);

        for (int i = 0; i < 3; i++) {
            for (final Direction d : Iterate.horizontalDirections) {
                final BlockPos location = bearingPos.above(i + 1)
                        .relative(d);
                scene.world().showSectionAndMerge(util.select().position(location), d.getOpposite(), plank);
                scene.idle(2);
            }
        }

        scene.overlay().showText(70)
                .text("Symmetric Sails are handy blocks to create Windmills with")
                .pointAt(util.vector().blockSurface(util.grid().at(1, 3, 2), Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showOutlineWithText(util.select().position(bearingPos.above()), 80)
                .colored(PonderPalette.GREEN)
                .text("They will attach to blocks and each other without the need of Super Glue or Chassis Blocks")
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(40);
        scene.world().configureCenterOfRotation(plank, util.vector().centerOf(bearingPos));

        scene.world().rotateBearing(bearingPos, 180, 75);
        scene.world().rotateSection(plank, 0, 180, 0, 75);
        scene.idle(76);
        scene.rotateCameraY(-30);
        scene.idle(10);
        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(2, 3, 1), Direction.NORTH), Pointing.RIGHT, 30)
                .withItem(new ItemStack(Items.BLUE_DYE));
        scene.idle(7);
        scene.world().setBlock(util.grid().at(2, 3, 3), SimBlocks.DYED_SYMMETRIC_SAILS.get(DyeColor.BLUE)
                .defaultBlockState()
                .setValue(SymmetricSailBlock.AXIS, Direction.Axis.X), false);
        scene.idle(10);
        scene.overlay().showText(40)
                .colored(PonderPalette.BLUE)
                .text("Right-Click with Dye to paint them")
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(util.grid().at(2, 3, 1), Direction.WEST))
                .placeNearTarget();
        scene.idle(20);
        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(2, 3, 1), Direction.NORTH), Pointing.RIGHT, 30)
                .withItem(new ItemStack(Items.BLUE_DYE));
        scene.idle(7);
        scene.world().replaceBlocks(util.select().fromTo(2, 2, 3, 2, 4, 3), SimBlocks.DYED_SYMMETRIC_SAILS.get(DyeColor.BLUE)
                .defaultBlockState()
                .setValue(SymmetricSailBlock.AXIS, Direction.Axis.X), false);

        scene.idle(20);

        scene.world().rotateBearing(bearingPos, 720, 300);
        scene.world().rotateSection(plank, 0, 720, 0, 300);

    }
}
