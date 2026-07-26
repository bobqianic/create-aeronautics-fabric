package dev.eriksonn.aeronautics.content.ponder.scenes;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.gearbox.GearboxBlockEntity;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlock;
import dev.eriksonn.aeronautics.content.ponder.instructions.*;
import dev.eriksonn.aeronautics.content.ponder.instructions.RedstoneSignalInstruction;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.simulated_team.simulated.ponder.SceneScheduler;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.instructions.*;
import dev.eriksonn.aeronautics.content.ponder.instructions.CustomGyroBearingTiltInstruction;
import dev.eriksonn.aeronautics.content.ponder.instructions.PropellerParticleSpawningInstruction;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.*;
import com.zurrtum.create.client.ponder.api.scene.*;
import com.zurrtum.create.client.ponder.foundation.instruction.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PropellerScenes {

    public static void propellerBearingSize(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final CreateSceneBuilder.SpecialInstructions special = scene.special();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();
        final VectorUtil vector = util.vector();

        scene.title("propeller_bearing_size", "Constructing Propellers using Propeller Bearings");
        scene.configureBasePlate(1, 1, 5);
        scene.setSceneOffsetY(-3f);
        scene.scaleSceneView(0.8f);
        world.showSection(select.layer(0), Direction.UP);
        scene.idle(10);

        final BlockPos propellerPos = new BlockPos(3, 2, 3);
        final Selection shafts1 = select.fromTo(6, 1, 3, 3, 1, 3);

        world.showSection(shafts1, Direction.DOWN);
        scene.idle(10);
        world.showSection(select.position(propellerPos), Direction.DOWN);
        scene.idle(5);
        final ElementLink<WorldSectionElement> propellerCenterSection = world.showIndependentSection(select.position(propellerPos.above()), Direction.DOWN);
        scene.idle(5);
        final List<ElementLink<WorldSectionElement>> sails = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            final Direction dir = Direction.fromYRot(i * 90);
            final Vec3i pos = dir.getUnitVec3i().offset(0, 2, 0);
            final ElementLink<WorldSectionElement> currentSail = world.showIndependentSection(select.position(propellerPos.offset(pos)), dir.getOpposite());
            sails.add(currentSail);

            world.moveSection(currentSail, new Vec3(0, -1, 0), 0);
            world.configureCenterOfRotation(currentSail, Vec3.atCenterOf(propellerPos));
            scene.idle(2);
        }
        scene.idle(5);
        overlay.showOutlineWithText(select.position(propellerPos.above()), 60)
                .colored(PonderPalette.GREEN)
                .pointAt(vector.blockSurface(propellerPos.above(), Direction.UP))
                .placeNearTarget()
                .attachKeyFrame()
                .text("Propeller Bearings attach to the block in front of them");

        scene.idle(65);

        PropellerRotateInstruction propellerRotation = new PropellerRotateInstruction(propellerPos,propellerCenterSection,Direction.UP,-32,4);

        scene.addInstruction(propellerRotation);
        for (final ElementLink<WorldSectionElement> sail : sails)
            scene.addInstruction(new ChangePropellerRotateInstruction.AddSection(propellerRotation,sail));
        scene.addInstruction(new ChangePropellerRotateInstruction.SetParticles(propellerRotation,null,1.5f,-5,1.5f,false));

        scene.idle(10);
        final ElementLink<ParrotElement> flappyBirb = special.createBirb(vector.topOf(propellerPos.above()), ParrotPose.FlappyPose::new);
        scene.idle(2);

        TickingInstruction[] parrotInstructions = new TickingInstruction[]{
                AnimateParrotInstruction.rotate(flappyBirb, new Vec3(0, -4300, 0), 1000),
                new CustomParrotFlappingInstruction(flappyBirb, 1f, 1000)
        };
        for (int i = 0; i < 2; i++)
            scene.addInstruction(parrotInstructions[i]);

        special.moveParrot(flappyBirb, vector.of(0, 1.0, 0), 30);

        final Vec3 propellerCenter = new Vec3(3.5, 3.5, 3.5);

        scene.idle(50);
        int i = 0;
        for (final ElementLink<WorldSectionElement> sail : sails) {
            final Direction dir = Direction.fromYRot(i * 90);
            world.hideIndependentSection(sail, dir);
            i++;
        }
        scene.idle(15);
        sails.clear();
        for (i = 0; i < 4; i++) {
            final Direction dir = Direction.fromYRot(i * 90);
            final BlockPos base = propellerPos.offset(0, 1, 0);
            final Vec3i pos1 = dir.getUnitVec3i();
            final Vec3i pos2 = dir.getUnitVec3i().cross(new Vec3i(0, -1, 0));
            final ElementLink<WorldSectionElement> currentSupport = world.showIndependentSection(select.fromTo(
                            base.offset(pos1),
                            base.offset(pos1.multiply(2))),
                    dir.getOpposite());

            final ElementLink<WorldSectionElement> currentSail = world.showIndependentSection(select.fromTo(
                            base.offset(pos1.offset(pos2)),
                            base.offset(pos1.multiply(3).offset(pos2))),
                    dir.getOpposite());
            sails.add(currentSail);

            world.configureCenterOfRotation(currentSail, Vec3.atCenterOf(propellerPos));
            world.configureCenterOfRotation(currentSupport, Vec3.atCenterOf(propellerPos));

            scene.addInstruction(new ChangePropellerRotateInstruction.AddSection(propellerRotation,currentSail));
            scene.addInstruction(new ChangePropellerRotateInstruction.AddSection(propellerRotation,currentSupport));
        }
        scene.idle(3);
        scene.addInstruction(new ChangePropellerRotateInstruction.SetParticles(propellerRotation,null,5f,-5,3.5f,false));
        special.moveParrot(flappyBirb, vector.of(0, 2.0, 0), 30);
        scene.idle(5);


        AABB bb = new AABB(propellerCenter, propellerCenter).inflate(3.5, 0, 3.5);
        overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, bb, bb, 3);
        scene.idle(3);

        overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, bb, bb.expandTowards(0, 6, 0), 70);
        scene.addInstruction(new AirflowAABBInstruction(PonderPalette.WHITE, bb.expandTowards(0, 6, 0), 70, Direction.UP, 5, 3));

        overlay.showText(65)
                .pointAt(new Vec3(0, 4, 2.5))
                .attachKeyFrame()
                .placeNearTarget()
                .text("Propellers with more sails are more efficient at producing Thrust");

        scene.idle(75);

        i = 0;
        for (final ElementLink<WorldSectionElement> sail : sails) {
            final Direction dir = Direction.fromYRot(i * 90 + 180);
            world.hideIndependentSection(sail, dir);
            i++;
        }

        scene.idle(18);
        sails.clear();
        for (i = 0; i < 4; i++) {
            final Direction dir = Direction.fromYRot(i * 90);
            final BlockPos base = propellerPos.offset(0, 2, 0);
            final Vec3i pos1 = dir.getUnitVec3i();
            final Vec3i pos2 = dir.getUnitVec3i().cross(new Vec3i(0, -1, 0));


            final ElementLink<WorldSectionElement> currentSail = world.showIndependentSection(select.fromTo(
                            base.offset(pos1.offset(pos2)),
                            base.offset(pos1.multiply(3).offset(pos2))),
                    dir.getClockWise());
            sails.add(currentSail);


            world.moveSection(currentSail, new Vec3(0, -1, 0), 0);
            world.configureCenterOfRotation(currentSail, Vec3.atCenterOf(propellerPos));
            scene.addInstruction(new ChangePropellerRotateInstruction.AddSection(propellerRotation,currentSail));
        }
        scene.idle(10);
        overlay.showText(80)
                .pointAt(new Vec3(0, 4, 2.5))
                .attachKeyFrame()
                .placeNearTarget()
                .colored(PonderPalette.BLUE)
                .text("Any structure can count as a valid Propeller, as long as it has at least 2 valid sail-like blocks");

        scene.idle(100);

        scene.addInstruction(new ChangePropellerRotateInstruction.StopRotation(propellerRotation,30));
        scene.idle(10);
        for (i = 0; i < 2; i++)
            scene.addInstruction(new TickingStoppingInstruction(parrotInstructions[i]));
        special.moveParrot(flappyBirb, vector.of(0, -3.0, 0), 30);
    }

    public static void propellerBearingThrust(final SceneBuilder builder, final SceneBuildingUtil util,final boolean smolPropeller) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final CreateSceneBuilder.SpecialInstructions special = scene.special();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();

        scene.title("propeller_bearing_thrust", "Moving Simulated Contraptions using Propellers");
        scene.configureBasePlate(0, 0, 12);
        world.multiplyKineticSpeed(select.everywhere(), 1);
        scene.setSceneOffsetY(-2f);
        scene.scaleSceneView(0.6f);
        final ElementLink<WorldSectionElement> ground = world.showIndependentSection(select.fromTo(0, 0, 0, 11, 0, 11), Direction.UP);

        scene.idle(10);

        final ElementLink<WorldSectionElement> airship = world.showIndependentSection(select.fromTo(3, 3, 7, 10, 3, 11), Direction.DOWN);
        scene.idle(2);
        final ElementLink<WorldSectionElement> airshipSmall = world.showIndependentSection(select.fromTo(4, 4, 2, 6, 4, 2), Direction.DOWN);

        SceneScheduler sceneScheduler = new SceneScheduler(scene);
        SceneScheduler.Sequence seq1 = sceneScheduler.get(0);
        SceneScheduler.Sequence seq2 = sceneScheduler.get(1);
        Supplier<WorldSectionElement> airshipSupplier = () -> scene.getScene().resolve(airship);
        Supplier<WorldSectionElement> smallAirshipSupplier = () -> scene.getScene().resolve(airshipSmall);

        for (int x = 8; x >= 3; x--) {
            seq1.addInstruction(new DisplayWorldSectionInstruction(15, Direction.DOWN,select.position(x, 4, 9) , airshipSupplier));
            seq1.idle(2);
        }
        seq1.idle(3);
        seq1.addInstruction(new DisplayWorldSectionInstruction(15, Direction.DOWN,select.fromTo(4, 4, 7, 9, 4, 8) , airshipSupplier));
        seq1.addInstruction(new DisplayWorldSectionInstruction(15, Direction.DOWN,select.fromTo(4, 4, 10, 9, 4, 11) , airshipSupplier));
        seq1.addInstruction(new DisplayWorldSectionInstruction(15, Direction.DOWN,select.position(9, 4, 9) , airshipSupplier));
        seq1.idle(4);
        seq1.addInstruction(new DisplayWorldSectionInstruction(15, Direction.DOWN,select.fromTo(4, 5, 7, 8, 7, 7) , airshipSupplier));
        seq1.addInstruction(new DisplayWorldSectionInstruction(15, Direction.DOWN,select.fromTo(4, 5, 11, 8, 7, 11) , airshipSupplier));
        scene.idle(4);
        seq1.addInstruction(new DisplayWorldSectionInstruction(15, Direction.DOWN,select.fromTo(4, 7, 8, 7, 7, 10) , airshipSupplier));
        seq1.addInstruction(new DisplayWorldSectionInstruction(15, Direction.DOWN,select.fromTo(3, 8, 7, 9, 11, 11) , airshipSupplier));


        seq2.addInstruction(new DisplayWorldSectionInstruction(15, Direction.WEST,select.fromTo(7, 4, 1, 9, 4, 3) , smallAirshipSupplier));
        seq2.addInstruction(new DisplayWorldSectionInstruction(15, Direction.WEST,select.fromTo(6, 4, 1,5, 4, 1) , smallAirshipSupplier));
        seq2.addInstruction(new DisplayWorldSectionInstruction(15, Direction.WEST,select.fromTo(6, 4, 3,5, 4, 3) , smallAirshipSupplier));
        seq2.idle(4);
        seq2.addInstruction(new DisplayWorldSectionInstruction(15, Direction.EAST,select.fromTo(2, 4, 1,3, 4, 3) , smallAirshipSupplier));
        seq2.idle(4);
        seq2.addInstruction(new DisplayWorldSectionInstruction(15, Direction.SOUTH,select.fromTo(3, 4, 0,7, 4, 0) , smallAirshipSupplier));
        seq2.addInstruction(new DisplayWorldSectionInstruction(15, Direction.NORTH,select.fromTo(3, 4, 4,7, 4, 4) , smallAirshipSupplier));
        seq2.idle(4);
        seq2.addInstruction(new DisplayWorldSectionInstruction(15, Direction.DOWN,select.fromTo(3, 5, 1,7, 5, 3) , smallAirshipSupplier));
        seq2.addInstruction(new DisplayWorldSectionInstruction(15, Direction.UP,select.fromTo(3, 3, 1,7, 3, 3) , smallAirshipSupplier));

        sceneScheduler.run();
        scene.idle(2);
        final ElementLink<WorldSectionElement> propeller = world.showIndependentSection(select.fromTo(2, 2, 7, 2, 6, 11), Direction.EAST);
        world.showSectionAndMerge(select.position(2,4,0).add(select.position(2,4,4)),Direction.EAST,airshipSmall);

        ElementLink<WorldSectionElement>[] shipPieces = smolPropeller?new ElementLink[]{airshipSmall}:new ElementLink[]{airship,propeller};

        scene.idle(5);
        final ElementLink<ParrotElement> flappyBird = special.createBirb(new Vec3(7.5, 4.5, 9.5), ParrotPose.FacePointOfInterestPose::new);
        special.movePointOfInterest(new Vec3(1000, 0, 6));
        scene.addInstruction(new CustomParrotSectionLockInstruction(shipPieces[0], flappyBird, new Vec3(9.5, 4.7, 12.5), 800));

        final BlockPos propellerPos = new BlockPos(3, 4, 9);

        scene.addInstruction(new SetPropellerSailsInstruction(propellerPos,8f));

        PropellerRotateInstruction propellerRotation = new PropellerRotateInstruction(propellerPos,propeller, Direction.WEST,-32,8);

        scene.addInstruction(propellerRotation);
        scene.addInstruction(new ChangePropellerRotateInstruction.SetParticles(propellerRotation,airship, 2f,-6f,2f,false));
        scene.idle(10);

        final AABB[] bbs = {
                new AABB(new Vec3(2.5, 4.5, 0.5), new Vec3(2.5, 4.5, 0.5)).inflate(0, 1.0, 1.0),
                new AABB(new Vec3(2.5, 4.5, 4.5), new Vec3(2.5, 4.5, 4.5)).inflate(0, 1.0, 1.0),
                new AABB(new Vec3(2.5, 4.5, 9.5), new Vec3(2.5, 4.5, 9.5)).inflate(0, 2.5, 2.5)

        };
        final AABB[] bbs2 = {
                new AABB(new Vec3(2.5, 4.5, 4.0), new Vec3(2.5, 4.5, 4.0)).inflate(0, 1.0, 1.0),
                new AABB(new Vec3(2.5, 4.5, 8.0), new Vec3(2.5, 4.5, 8.0)).inflate(0, 1.0, 1.0),
                new AABB(new Vec3(2.5, 4.5, 6.0), new Vec3(2.5, 4.5, 6.0)).inflate(0, 2.5, 2.5)

        };
        final Vec3[] bbInfo = {//length,speed,spacing

                new Vec3(2, 4, 1.5),
                new Vec3(2, 4, 1.5),
                new Vec3(6, 5, 2)
        };


        for (int i = 0; i < bbs.length; i++) {
            final AABB bb = bbs[i];
            scene.addInstruction(new AirflowAABBInstruction(PonderPalette.WHITE, bb.expandTowards(-bbInfo[i].x, 0, 0), 90, Direction.WEST, (float) bbInfo[i].y, (float) bbInfo[i].z));
        }
        TickingInstruction propParticles1 = new PropellerParticleSpawningInstruction(airshipSmall,new BlockPos(2,4,0),Direction.WEST,1000,1,5,1,false);
        scene.addInstruction(propParticles1);
        TickingInstruction propParticles2 = new PropellerParticleSpawningInstruction(airshipSmall,new BlockPos(2,4,4),Direction.WEST,1000,1,5,1,false);
        scene.addInstruction(propParticles2);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(propellerPos.east(5),true,false));
        scene.addInstruction(new RedstoneSignalInstruction(util.select().fromTo(propellerPos.east(2), propellerPos.east(3)), 8));
        scene.idle(15);
        overlay.showText(60)
                .pointAt(new Vec3(1.5, 4, 8))
                .attachKeyFrame()
                .placeNearTarget()
                .text("Several types of propellers can produce Thrust");
        scene.idle(70);

        if(smolPropeller) {
            world.hideIndependentSection(airship, Direction.UP);
            world.hideIndependentSection(propeller, Direction.UP);
            special.hideElement(flappyBird,Direction.UP);
            scene.addInstruction(new ChangePropellerRotateInstruction.StopRotation(propellerRotation,20));
        }else {
            world.hideIndependentSection(airshipSmall, Direction.UP);
            scene.addInstruction(new TickingStoppingInstruction(propParticles1));
            scene.addInstruction(new TickingStoppingInstruction(propParticles2));
        }

        scene.idle(10);
        for (ElementLink<WorldSectionElement> shipPiece : shipPieces) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(shipPiece, new Vec3(0, 0,  smolPropeller?3.5:-3.5), 20, SmoothMovementUtils.cubicSmoothing()));
        }
        Vec3 pointingPos = smolPropeller ? new Vec3(2.5, 5, 8) : new Vec3(3.5, 5, 6);
        scene.idle(25);
        overlay.showText(60)
                .pointAt(pointingPos)
                .attachKeyFrame()
                .placeNearTarget()
                .text("Thrust can move simulated contraptions");

        scene.idle(15);

        for (ElementLink<WorldSectionElement> shipPiece : shipPieces) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(shipPiece, new Vec3(4, 0, 0), 70, SmoothMovementUtils.cubicSmoothing()));
        }
        scene.idle(60);

        scene.world().multiplyKineticSpeed(select.everywhere(),-1);
        if(!smolPropeller)
            scene.addInstruction(new ChangePropellerRotateInstruction.SetRotationRate(propellerRotation,32));
        else
        {
            scene.addInstruction(new TickingStoppingInstruction(propParticles1));
            propParticles1 = new PropellerParticleSpawningInstruction(airshipSmall,new BlockPos(2,4,0),Direction.EAST,1000,1,5,1,false);
            scene.addInstruction(propParticles1);
            scene.addInstruction(new TickingStoppingInstruction(propParticles2));
            propParticles2 = new PropellerParticleSpawningInstruction(airshipSmall,new BlockPos(2,4,4),Direction.EAST,1000,1,5,1,false);
            scene.addInstruction(propParticles2);
        }

        scene.idle(10);


        overlay.showText(60)
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(pointingPos.add(4,0,0))
                .text("Reversing the rotation direction reverses the Thrust");
        int[] currentBBSelection = smolPropeller? new int[]{0, 1} :new int[]{2};
        for (int i : currentBBSelection) {
            scene.addInstruction(new AirflowAABBInstruction(PonderPalette.WHITE, bbs2[i].move(4,0,0).expandTowards(bbInfo[i].x, 0, 0), 60, Direction.EAST, (float) bbInfo[i].y, (float) bbInfo[i].z,true,true));
        }

        scene.idle(10);
        for (ElementLink<WorldSectionElement> shipPiece : shipPieces) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(shipPiece, new Vec3(-3, 0, 0), 70, SmoothMovementUtils.cubicSmoothing()));
        }

        scene.idle(80);
        pointingPos = pointingPos.add(1,0,0);
        if(smolPropeller)
        {
            Vec3 pos = pointingPos;
            overlay.showText(60)
                    .placeNearTarget()
                    .pointAt(pos)
                    .attachKeyFrame()
                    .text("The Thrust can also be reversed using a wrench");
            scene.idle(20);
            overlay.showControls(pos.add(-0.5,-0.5,-4), Pointing.LEFT, 20).withItem(AllItems.WRENCH.getDefaultInstance());
            scene.idle(10);
            scene.addInstruction(new TickingStoppingInstruction(propParticles1));
            propParticles1 = new PropellerParticleSpawningInstruction(airshipSmall,new BlockPos(2,4,0),Direction.WEST,1000,1,5,1,false);
            scene.addInstruction(propParticles1);
            scene.addInstruction(new TickingStoppingInstruction(propParticles2));
            propParticles2 = new PropellerParticleSpawningInstruction(airshipSmall,new BlockPos(2,4,4),Direction.WEST,1000,1,5,1,false);
            scene.addInstruction(propParticles2);
            scene.world().modifyBlocks(select.fromTo(2,4,0,2,4,4),state -> state.hasProperty(BasePropellerBlock.REVERSED)?state.setValue(BasePropellerBlock.REVERSED,true):state,false);
        }else {

            final Vec3 configBox = pointingPos.add(0.1,0,0);
            AABB bb = new AABB(configBox.x - 0.1, configBox.y, configBox.z - 0.1, configBox.x + 0.1, configBox.y, configBox.z + 0.1);
            overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, bb, bb, 60);
            overlay.showText(60)
                    .placeNearTarget()
                    .pointAt(configBox)
                    .attachKeyFrame()
                    .text("The Thrust can also be reversed using the value box");
            scene.idle(20);
            overlay.showControls(configBox, Pointing.DOWN, 20).rightClick();
            scene.idle(10);
        }

        scene.addInstruction(new ChangePropellerRotateInstruction.SetParticles(propellerRotation,airship, 2f,6f,2f,false));
        for (int i : currentBBSelection) {
            scene.addInstruction(new AirflowAABBInstruction(PonderPalette.WHITE, bbs2[i].move(1,0,0).expandTowards(-bbInfo[i].x, 0, 0), 60, Direction.WEST, (float) bbInfo[i].y, (float) bbInfo[i].z,true,true));
        }

        scene.idle(30);

        final float movementDistance = 53;
        int movementDuration = (int) (movementDistance * (20 / 3.0));
        scene.addInstruction(new CustomToggleBaseShadowInstruction());
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(ground, new Vec3(-movementDistance, 0, 0), movementDuration, SmoothMovementUtils.asymptoticAcceleration(15f)));

        final ElementLink<WorldSectionElement> ground2 = world.showIndependentSection(select.fromTo(12, 0, 2, 55, 1, 9), Direction.WEST);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(ground2, new Vec3(-movementDistance, 0, 0), movementDuration, SmoothMovementUtils.asymptoticAcceleration(15f)));
        scene.idle(40);
        movementDuration -= 40;

        scene.rotateCameraY(35);
        scene.idle(20);
        movementDuration -= 20;
        world.hideIndependentSection(ground, null);
        for (int i : currentBBSelection) {
            AABB bb = bbs2[i].move(1, 0, 0);

            overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, bb, bb, 3);
        }
        scene.idle(3);
        for (int i : currentBBSelection) {
            AABB bb = bbs2[i].move(1, 0, 0);
            overlay.chaseBoundingBoxOutline(PonderPalette.WHITE, bb, bb.expandTowards(-6, 0, 0), 210);
            scene.addInstruction(new AirflowAABBInstruction(PonderPalette.WHITE, bb.expandTowards(-6, 0, 0), 120, Direction.WEST, 5, 2, true, true));
        }
        scene.idle(10);
        movementDuration -= 13;

        final AABB bb1 = new AABB(new Vec3(12, 1.5, 3), new Vec3(12, 1.5, 10.5));
        final AABB bb2 = new AABB(new Vec3(12, 1.5, 10.5), new Vec3(12, 8, 10.5));

        overlay.chaseBoundingBoxOutline(PonderPalette.BLUE, bb1, bb1, 1);
        overlay.chaseBoundingBoxOutline(PonderPalette.BLUE, bb2, bb2, 1);
        scene.idle(1);
        overlay.chaseBoundingBoxOutline(PonderPalette.BLUE, bb1, bb1.expandTowards(-15, 0, 0), 175);
        overlay.chaseBoundingBoxOutline(PonderPalette.BLUE, bb2, bb2.expandTowards(-15, 0, 0), 175);
        scene.idle(5);
        scene.addInstruction(new AirflowAABBInstruction(PonderPalette.BLUE, bb1.expandTowards(-15, 0, 0), 170, Direction.WEST, 3.15f, 3, false, false));
        scene.addInstruction(new AirflowAABBInstruction(PonderPalette.BLUE, bb2.expandTowards(-15, 0, 0), 170, Direction.WEST, 3.15f, 3, false, false));
        scene.idle(10);
        movementDuration -= 16;

        Vec3 pointingPos2 = smolPropeller? pointingPos.add(0,0.75,0) : pointingPos.add(-1,2,0);
        overlay.showText(75)
                .pointAt(pointingPos2)
                .attachKeyFrame()
                .placeNearTarget()
                .text("If the Propeller is moving through the air, it cannot push as hard, as the air is already moving");
        scene.idle(50);
        scene.rotateCameraY(-35);
        scene.idle(35);
        movementDuration -= 85;

        overlay.showText(60)
                .pointAt(pointingPos2)
                .attachKeyFrame()
                .placeNearTarget()
                .colored(PonderPalette.RED)
                .text("...and hence its Thrust output decreases");
        scene.idle(5);
        for (int i : currentBBSelection) {
            AABB bb = bbs2[i].move(1, 0, 0);
            scene.addInstruction(new AirflowAABBInstruction(PonderPalette.RED, bb.expandTowards(-6, 0, 0), 90, Direction.WEST, 5, 4, true, false));
        }
        scene.idle(65);
        movementDuration -= 70;


        overlay.showControls(pointingPos, Pointing.DOWN, 60).withItem(AllItems.GOGGLES.getDefaultInstance());
        scene.idle(6);
        movementDuration -= 6;
        overlay.showText(80)
                .text("The Propeller's Thrust and Airflow can be inspected with Engineer's Goggles")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(pointingPos)
                .placeNearTarget();


        scene.idle(movementDuration - 40);

        final ElementLink<WorldSectionElement> ground3 = world.showIndependentSection(select.fromTo(0, 0, 0, 11, 0, 11), null);
        world.moveSection(ground3, new Vec3(3+6.4, 0, 0), 0);
        world.moveSection(ground3, new Vec3(-6.4, 0, 0), 40);

        scene.idle(20);

        world.multiplyKineticSpeed(select.layer(4),0);
        if(smolPropeller) {
            scene.addInstruction(new TickingStoppingInstruction(propParticles1));
            scene.addInstruction(new TickingStoppingInstruction(propParticles2));
        }
        else
            scene.addInstruction(new ChangePropellerRotateInstruction.StopRotation(propellerRotation,30));
        world.hideIndependentSection(ground2, null);

        final float slowdownSpeed = 3;
        final float slowdownDistance = 3;
        final int slowdownTime = (int) (2f * 20f * slowdownDistance / slowdownSpeed);//scaled by 2 due to quadratic slowdown

        for (ElementLink<WorldSectionElement> shipPiece : shipPieces) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(shipPiece, new Vec3(-2, 0, 0), slowdownTime+20, SmoothMovementUtils.cubicSmoothing()));
        }
        scene.idle(20);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(ground3, new Vec3(-slowdownDistance, 0, 0), slowdownTime, SmoothMovementUtils.quadraticRiseDual()));

        scene.idle(slowdownTime - 15);
        scene.addInstruction(new CustomToggleBaseShadowInstruction());

    }

    public static void gyroBearingStabilize(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();
        final CreateSceneBuilder.EffectInstructions effects = scene.effects();

        scene.title("gyroscopic_propeller_bearing_stabilize", "Stabilizing helicopters using Gyroscopic Propeller Bearings");
        scene.configureBasePlate(0, 0, 9);
        scene.setSceneOffsetY(-2f);
        scene.scaleSceneView(0.8f);
        world.showSection(select.layer(0), Direction.UP);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(new BlockPos(4, 3, 3), true, true));

        world.multiplyKineticSpeed(select.everywhere(), 1);
        ElementLink<WorldSectionElement> ship = world.showIndependentSection(select.fromTo(2, 1, 2, 6, 2, 6), Direction.DOWN);
        scene.idle(5);
        world.showSectionAndMerge(select.fromTo(3, 3, 3, 4, 3, 3), Direction.DOWN, ship);
        scene.idle(3);

        world.showSectionAndMerge(select.fromTo(3, 3, 4, 4, 3, 4), Direction.DOWN, ship);

        scene.idle(3);

        world.showSectionAndMerge(select.position(3, 3, 5), Direction.DOWN, ship);
        world.showSectionAndMerge(select.position(4, 3, 5), Direction.DOWN, ship);
        scene.idle(3);

        BlockPos propellerPos = new BlockPos(4, 4, 4);
        world.showSectionAndMerge(select.position(propellerPos), Direction.DOWN, ship);

        scene.idle(3);

        ElementLink<WorldSectionElement> propellerSection = world.showIndependentSection(select.fromTo(1, 5, 1, 7, 5, 7), Direction.DOWN);
        scene.idle(5);


        overlay.showOutlineWithText(select.position(propellerPos), 70)
                .text("Normal Propeller Bearings may be unsuitable for helicopters")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(Vec3.atCenterOf(propellerPos).add(0, 0, 0.5))
                .placeNearTarget();

        scene.idle(60);

        float revolutions = 2.5f;
        int duration = (int) (36 * revolutions);
        //world.rotateSection(propellerSection, 0, revolutions * 360, 0, duration);
        //world.rotateBearing(propellerPos, revolutions * 360, duration);

        PropellerRotateInstruction propellerRotate = new PropellerRotateInstruction(propellerPos,propellerSection,Direction.UP,32,8);
        scene.addInstruction(propellerRotate);
        scene.addInstruction(new ChangePropellerRotateInstruction.SetParticles(propellerRotate,propellerSection,4f, -5, 3.0f,false));

        world.configureCenterOfRotation(propellerSection, new Vec3(4.5f, 3, 4.5f));
        world.configureCenterOfRotation(ship, new Vec3(4.5f, 3, 4.5f));

        //scene.addInstruction(new PropellerParticleSpawningInstruction(ship, propellerPos.above(), Direction.UP, duration - 15, 4f, -5, 3.0f));

        scene.idle(10);

        //scene.world().rotateSection(propellerSection,0,0,90,30);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(ship, new Vec3(0, 0, 160), 70, SmoothMovementUtils.quadraticRise()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(propellerSection, new Vec3(0, 0, 160), 70, SmoothMovementUtils.quadraticRise()));

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(ship, new Vec3(0, 2.5, 0), 40, SmoothMovementUtils.cubicSmoothing()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(propellerSection, new Vec3(0, 2.5, 0), 40, SmoothMovementUtils.cubicSmoothing()));

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(ship, new Vec3(-12, 0, 0), 70, SmoothMovementUtils.cubicRise()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(propellerSection, new Vec3(-12, 0, 0), 70, SmoothMovementUtils.cubicRise()));
        scene.idle(40);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(ship, new Vec3(0, -3, 0), 30, SmoothMovementUtils.quadraticRise()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(propellerSection, new Vec3(0, -3, 0), 30, SmoothMovementUtils.quadraticRise()));
        scene.idle(10);
        //noinspection DataFlowIssue
        world.hideIndependentSection(ship, null);
        //noinspection DataFlowIssue
        world.hideIndependentSection(propellerSection, null);
        scene.addInstruction(new ChangePropellerRotateInstruction.StopRotation(propellerRotate,30));

        scene.idle(30);

        ship = world.showIndependentSection(
                select.fromTo(2, 1, 2, 6, 2, 6)
                        .add(select.fromTo(3, 3, 3, 4, 3, 4))
                , Direction.DOWN);

        world.showSection(select.position(propellerPos), Direction.DOWN);

        ElementLink<WorldSectionElement> chainSection = world.showIndependentSection(select.fromTo(3, 3, 5, 4, 3, 5), Direction.DOWN);

        propellerSection = world.showIndependentSection(select.fromTo(1, 5, 1, 7, 5, 7), Direction.DOWN);
        scene.idle(10);
        world.moveSection(propellerSection, new Vec3(0, 1.25, 0), 15);

        scene.idle(10);
        world.hideSection(select.position(propellerPos), Direction.UP);
        scene.idle(20);
        propellerPos = propellerPos.east();
        //scene.world().showSectionAndMerge(util.select().position(propellerPos),Direction.DOWN,ship);

        final ElementLink<WorldSectionElement> bearingSection = world.showIndependentSection(select.position(propellerPos), Direction.DOWN);
        world.moveSection(bearingSection, new Vec3(-1, 0, 0), 0);
        scene.idle(15);
        world.moveSection(propellerSection, new Vec3(0, -1.25, 0), 15);
        scene.idle(10);

        overlay.showOutlineWithText(select.position(propellerPos.west()), 70)
                .text("Gyroscopic Propeller Bearings should be used in this situation")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(Vec3.atCenterOf(propellerPos.west()).add(0, 0, 0.5))
                .placeNearTarget();

        scene.idle(60);

        revolutions = 7f;
        duration = (int) (36 * revolutions);

        propellerRotate = new PropellerRotateInstruction(propellerPos,propellerSection,Direction.UP,32,8);
        scene.addInstruction(propellerRotate);
        scene.addInstruction(new ChangePropellerRotateInstruction.SetParticles(propellerRotate,propellerPos.west().above(),propellerSection,4f, -5, 3.0f,false));

        scene.idle(10);
        final ElementLink<WorldSectionElement>[] allSections = new ElementLink[]{ship, bearingSection, propellerSection, chainSection};
        final ElementLink<WorldSectionElement>[] nonPropellerSections = new ElementLink[]{ship, bearingSection, chainSection};
        scene.idle(30);
        duration -= 40;
        final Vec3 totalMotion = new Vec3(-0.5, 1.5, 0);
        for (final ElementLink<WorldSectionElement> section : allSections) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(section, totalMotion, 40, SmoothMovementUtils.cubicSmoothing()));
        }
        final Vec3 pivotPoint = Vec3.atCenterOf(propellerPos).add(-1, 0.25, 0);

        for (final ElementLink<WorldSectionElement> section : nonPropellerSections) {
            world.configureCenterOfRotation(section, pivotPoint);
            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(section, new Vec3(0, 0, 10), 40, SmoothMovementUtils.cubicSmoothing()));
        }
        scene.addInstruction(new CustomGyroBearingTiltInstruction(ship, propellerPos, 50, true));
        world.configureCenterOfRotation(bearingSection, Vec3.atCenterOf(propellerPos).add(0, 0.25, 0));


        scene.idle(45);
        duration -= 45;

        final Vec3 propellerCenter = pivotPoint.add(totalMotion).add(0, 0.75, 0);
        final AABB bb = new AABB(propellerCenter, propellerCenter).inflate(0.5, 0.1, 0.5);
        overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb, 3);
        scene.idle(3);
        duration -= 3;
        overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb.inflate(3, 0, 3), 100);


        final double cos = Math.cos(Math.toRadians(12));
        final double sin = Math.sin(Math.toRadians(12));
        final Matrix3d m = new Matrix3d(
                cos, sin, 0,
                -sin, cos, 0,
                0, 0, 1
        );//coordinate system rotated by 12 degrees around z

        final double s = 1.5;
        final double h1 = -0.25;
        final double h2 = h1 - 0.1;
        final double h3 = -2.5;
        final Vector3d[] lines = new Vector3d[]{
                new Vector3d(-s, h1, -s), new Vector3d(-s, h1, s),
                new Vector3d(-s, h1, s), new Vector3d(s, h1, s),
                new Vector3d(s, h1, s), new Vector3d(s, h1, -s),
                new Vector3d(s, h1, -s), new Vector3d(-s, h1, -s),
                new Vector3d(-s, h2, -s), new Vector3d(-s, h3, -s),
                new Vector3d(-s, h2, s), new Vector3d(-s, h3, s),
                new Vector3d(s, h2, -s), new Vector3d(s, h3, -s),
                new Vector3d(s, h2, s), new Vector3d(s, h3, s)
        };
        for (final Vector3d line : lines) {
            m.transform(line.add(-1 / 16f, 0, 0)).add(pivotPoint.x, pivotPoint.y, pivotPoint.z).add(-0.5, 1.5, 0);
        }

        for (int i = 0; i < lines.length; i += 2) {
            overlay.showBigLine(PonderPalette.RED,
                    JOMLConversion.toMojang(lines[i]),
                    JOMLConversion.toMojang(lines[i + 1]),
                    100);
        }

        scene.idle(5);
        scene.addInstruction(new RotateSceneInstruction(35, 35, true));
        scene.idle(20);
        duration -= 25;

        overlay.showText(80)
                .text("The Gyroscopic Propeller Bearing attempts to keep the propeller upright")
                .attachKeyFrame()
                .colored(PonderPalette.WHITE)
                .pointAt(pivotPoint.add(totalMotion).add(-0.501, 0.75, 0))
                .placeNearTarget();
        scene.idle(60);
        scene.addInstruction(new RotateSceneInstruction(-35, -35, true));
        scene.idle(50);
        duration -= 90;

        //scene.idle(duration);
        scene.addInstruction(new ChangePropellerRotateInstruction.StopRotation(propellerRotate,30));
        for (final ElementLink<WorldSectionElement> section : allSections) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(section, totalMotion.scale(-1), 40, SmoothMovementUtils.cubicSmoothing()));
        }
        for (final ElementLink<WorldSectionElement> section : nonPropellerSections) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(section, new Vec3(0, 0, -10), 40, SmoothMovementUtils.cubicSmoothing()));
        }
        scene.addInstruction(new CustomGyroBearingTiltInstruction(ship, propellerPos, 50, true));
    }

    public static void gyroBearingIsland(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final CreateSceneBuilder.SpecialInstructions special = scene.special();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();

        scene.title("gyroscopic_propeller_bearing_island", "Stabilizing using upside down Gyroscopic Propeller Bearings");
        scene.configureBasePlate(0, 0, 9);
        scene.setSceneOffsetY(-3f);
        scene.scaleSceneView(0.8f);
        world.showSection(select.layer(0), Direction.UP);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(new BlockPos(2, 8, 3), true, true));
        scene.idle(5);
        world.multiplyKineticSpeed(select.everywhere(), 1);
        final ElementLink<WorldSectionElement> propellerSection = world.showIndependentSection(select.layer(3), Direction.DOWN);
        world.moveSection(propellerSection, new Vec3(0, -1, 0), 0);
        final BlockPos bearingPos = new BlockPos(4, 4, 4);
        scene.idle(4);

        final Selection engineSection = select.position(3, 5, 5)
                .add(select.fromTo(2, 6, 5, 6, 6, 6))
                .add(select.fromTo(3, 6, 3, 4, 6, 4))
                .add(select.fromTo(5, 6, 2, 6, 6, 7))
                .add(select.fromTo(2, 6, 7, 2, 7, 7));
        final Selection carpet = select.fromTo(1, 6, 2, 2, 6, 4)
                .add(select.fromTo(2, 6, 1, 3, 6, 2));
        final Selection house = select.fromTo(5, 8, 1, 5, 9, 1)
                .add(select.fromTo(4, 8, 2, 6, 9, 6)
                        .add(select.fromTo(2, 8, 4, 3, 9, 7)));
        world.setKineticSpeed(select.position(bearingPos), 0);
        final ElementLink<WorldSectionElement> islandSection = world.showIndependentSection(select.position(bearingPos), Direction.DOWN);
        world.moveSection(islandSection, new Vec3(0, -1, 0), 0);
        scene.idle(5);
        overlay.showText(60)
                .text("When Gyroscopic Propeller Bearings are facing downwards...")
                .attachKeyFrame()
                .colored(PonderPalette.WHITE)
                .pointAt(Vec3.atCenterOf(bearingPos.below()))
                .placeNearTarget();
        scene.idle(65);
        world.showSectionAndMerge(select.layers(4, 2).substract(select.position(bearingPos)).substract(engineSection).add(carpet), Direction.DOWN, islandSection);
        scene.idle(5);
        world.showSectionAndMerge(engineSection, Direction.DOWN, islandSection);
        world.multiplyKineticSpeed(engineSection,1f);
        scene.idle(3);

        world.configureCenterOfRotation(propellerSection, new Vec3(4.5f, 4.25, 4.5f));
        world.configureCenterOfRotation(islandSection, new Vec3(4.5f, 4.25, 4.5f));
        PropellerRotateInstruction propellerRotate = new PropellerRotateInstruction(bearingPos,propellerSection,Direction.DOWN,32,16);
        scene.addInstruction(propellerRotate);
        scene.addInstruction(new ChangePropellerRotateInstruction.SetParticles(propellerRotate, bearingPos.below(),propellerSection, 4f, 5, 4.0f, true));

        scene.idle(3);
        world.showSectionAndMerge(select.layer(6).substract(engineSection).substract(carpet), Direction.DOWN, islandSection);
        scene.idle(5);
        world.showSectionAndMerge(select.layers(7, 3).substract(house), Direction.DOWN, islandSection);
        scene.idle(5);
        world.showSectionAndMerge(select.layer(10).add(house), Direction.DOWN, islandSection);
        final ElementLink<ParrotElement> flappyBird = special.createBirb(new Vec3(1.5, 8.6, 1.5), ParrotPose.FacePointOfInterestPose::new);
        special.movePointOfInterest(new Vec3(1000, 0, 6));
        scene.addInstruction(new CustomParrotSectionLockInstruction(islandSection, flappyBird, new Vec3(1.5, 8.6, 1.5), 200));
        scene.idle(15);
        scene.addInstruction(new RotateSceneInstruction(35, 35, true));
        scene.idle(40);
        scene.addInstruction(new CustomGyroBearingTiltInstruction(islandSection, bearingPos, 200, true));
        special.movePointOfInterest(Vec3.atCenterOf(bearingPos));

        final float n = 2f;
        final FloatUnaryOperator angleFunction = t -> (float) Math.sin(Math.PI * n * SmoothMovementUtils.cubicSmoothing().apply(t));

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(islandSection, new Vec3(0, 0, 4), 150, angleFunction));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(propellerSection, new Vec3(0, 0, 10), 150, angleFunction));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(islandSection, new Vec3(0.2, 0, 0), 150, angleFunction));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(propellerSection, new Vec3(0.2, 0, 0), 150, angleFunction));
        scene.idle(20);
        overlay.showText(60)
                .text("...they can stabilize a top-heavy Structure")
                .attachKeyFrame()
                .colored(PonderPalette.WHITE)
                .pointAt(Vec3.atCenterOf(bearingPos.below()).add(0.001, 0.001, 0.001))
                .placeNearTarget();
        scene.idle(90);
        special.movePointOfInterest(new Vec3(1000, 0, 6));
        scene.addInstruction(new RotateSceneInstruction(-35, -35, true));
        scene.idle(20);
        scene.markAsFinished();
    }

    public static void gyroBearingRedstone(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final CreateSceneBuilder.EffectInstructions effects = scene.effects();
        final OverlayInstructions overlay = scene.overlay();

        scene.title("gyroscopic_propeller_bearing_redstone", "Controlling Gyroscopic Propeller Bearings with Redstone");
        scene.configureBasePlate(0, 0, 9);
        scene.setSceneOffsetY(-2.5f);
        scene.scaleSceneView(0.8f);
        world.showSection(util.select().layer(0), Direction.UP);
        world.multiplyKineticSpeed(util.select().everywhere(), 1);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(new BlockPos(4, 2, 5), true, true));
        scene.idle(5);
        final BlockPos bearingPos = new BlockPos(4, 3, 4);

        final ElementLink<WorldSectionElement> airship = world.showIndependentSection(
                util.select().fromTo(2, 1, 1, 7, 1, 7)
                        .substract(util.select().position(3, 1, 4)),
                Direction.DOWN);
        scene.idle(6);
        world.showSectionAndMerge(util.select().fromTo(7, 2, 4, 6, 2, 4), Direction.DOWN, airship);
        scene.idle(2);
        world.showSectionAndMerge(util.select().position(5, 2, 4), Direction.DOWN, airship);
        scene.idle(2);
        world.showSectionAndMerge(util.select().position(4, 2, 5), Direction.DOWN, airship);
        final ElementLink<WorldSectionElement> valve = world.showIndependentSection(util.select().position(4, 2, 3),Direction.DOWN);
        scene.idle(4);
        world.showSectionAndMerge(util.select().fromTo(2, 2, 1, 6, 2, 7)
                        .substract(util.select().fromTo(3, 2, 2, 5, 2, 6))
                        .substract(util.select().position(6, 2, 4))
                , Direction.DOWN, airship);
        scene.idle(4);
        final ElementLink<WorldSectionElement> pivotSection = world.showIndependentSection(
                util.select().fromTo(3, 1, 4, 3, 3, 4)
                        .add(util.select().position(4, 2, 4)),
                Direction.DOWN);
        scene.idle(4);
        final ElementLink<ParrotElement> flappyBird = scene.special().createBirb(new Vec3(5.5, 2.6, 4.5), ParrotPose.FacePointOfInterestPose::new);
        scene.special().movePointOfInterest(new Vec3(30, 4, 4));
        scene.addInstruction(new CustomParrotSectionLockInstruction(airship, flappyBird, new Vec3(5.5, 2.6, 4.5), 200));
        world.showSectionAndMerge(util.select().position(bearingPos), Direction.DOWN, pivotSection);
        scene.idle(4);
        final ElementLink<WorldSectionElement> propellerSection = world.showIndependentSection(util.select().layer(4), Direction.DOWN);

        final ElementLink<WorldSectionElement>[] allSections = new ElementLink[]{airship,valve, pivotSection, propellerSection};
        scene.idle(20);

        PropellerRotateInstruction propellerRotate = new PropellerRotateInstruction(bearingPos,propellerSection,Direction.UP,32,8);
        scene.addInstruction(propellerRotate);
        scene.addInstruction(new ChangePropellerRotateInstruction.SetParticles(propellerRotate,propellerSection,3f, -4, 2.5f,false));

        scene.idle(5);
        for (final ElementLink<WorldSectionElement> section : allSections) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(section, new Vec3(0, 2, 0), 40, SmoothMovementUtils.cubicSmoothing()));
        }
        scene.idle(35);
        scene.special().movePointOfInterest(new Vec3(4.5, 4.5, 3.5));
        scene.idle(15);
        world.configureCenterOfRotation(propellerSection, new Vec3(4.5, 1.5, 4.5));
        world.configureCenterOfRotation(pivotSection, new Vec3(3.5, 1.5, 4.5));


        world.setKineticSpeed(util.select().fromTo(4, 1, 3, 4, 1, 5), -16);
        world.setKineticSpeed(util.select().position(3, 1, 5), 16);
        world.modifyBlockEntityNBT(util.select().position(3, 1, 5), KineticBlockEntity.class, nbt -> nbt.getCompound("SwivelCog").ifPresent(tag -> tag.putFloat("Speed", 16)));

        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(valve,new Vec3(0,90,0),30,SmoothMovementUtils.cubicSmoothing()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(propellerSection, new Vec3(0, 0, 90), 30, SmoothMovementUtils.cubicSmoothing()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(propellerSection, new Vec3(-1, 1, 0), 30, SmoothMovementUtils.cubicSmoothing()));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(pivotSection, new Vec3(0, 0, 90), 30, SmoothMovementUtils.cubicSmoothing()));

        scene.idle(30);
        world.setKineticSpeed(util.select().position(4, 2, 3), 0);
        world.setKineticSpeed(util.select().fromTo(4, 1, 3, 4, 1, 5), 0);
        world.setKineticSpeed(util.select().position(3, 1, 5), 0);
        world.modifyBlockEntityNBT(util.select().position(3, 1, 5), KineticBlockEntity.class, nbt -> nbt.getCompound("SwivelCog").ifPresent(tag -> tag.putFloat("Speed", 0)));

        scene.idle(20);

        scene.special().movePointOfInterest(new Vec3(1.5, 4.5, 4.5));
        world.multiplyKineticSpeed(util.select().everywhere(), -1);

        scene.addInstruction(new ChangePropellerRotateInstruction.SetRotationRate(propellerRotate,-32));

        world.configureCenterOfRotation(propellerSection, new Vec3(4.5, 3.75, 4.5));
        world.moveSection(propellerSection, new Vec3(-2.25, -2.25, 0), 0);
        scene.addInstruction(new CustomGyroBearingTiltInstruction(pivotSection, bearingPos, 20, false));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(propellerSection, new Vec3(0, 0, -12), 20, SmoothMovementUtils.linear()));
        scene.idle(10);
        scene.addInstruction(new RotateSceneInstruction(35, 35, true));
        scene.idle(10);
        overlay.showBigLine(PonderPalette.RED,
                new Vec3(1.25, 4.5, 4.5),
                new Vec3(1.25, 4.5, 4.5).add(new Vec3(-Math.cos(Math.toRadians(12)), Math.sin(Math.toRadians(12)), 0).scale(3.5)), 135);
        scene.idle(10);
        overlay.showText(60)
                .text("Sometimes the upwards Tilt can be undesired")
                .attachKeyFrame()
                .colored(PonderPalette.WHITE)
                .pointAt(new Vec3(1.251, 4.51, 4.51))
                .placeNearTarget();
        scene.idle(50);
        scene.addInstruction(new RotateSceneInstruction(-35, -35, true));
        scene.idle(30);
        world.showSectionAndMerge(util.select().fromTo(1, 1, 2, 1, 2, 3), Direction.DOWN, airship);
        scene.special().movePointOfInterest(new Vec3(1.5, 3.5, 1.5));
        scene.idle(25);
        world.toggleRedstonePower(util.select().fromTo(1, 2, 2, 1, 2, 3));
        effects.indicateRedstone(new BlockPos(1, 4, 2));
        scene.addInstruction(new CustomGyroBearingTiltInstruction(pivotSection, bearingPos, 20, false, true));
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(propellerSection, new Vec3(0, 0, 12), 20, SmoothMovementUtils.linear()));
        scene.idle(20);
        overlay.showText(60)
                .text("Applying a Redstone signal will reset the Tilt to zero")
                .attachKeyFrame()
                .colored(PonderPalette.WHITE)
                .pointAt(new Vec3(1.25, 4.5, 4.5))
                .placeNearTarget();

        overlay.showBigLine(PonderPalette.GREEN,
                new Vec3(1.25, 4.5, 4.5).add(new Vec3(-3.5, 0, 0)),
                new Vec3(1.25, 4.5, 4.5),
                60);

        scene.idle(30);
    }
}
