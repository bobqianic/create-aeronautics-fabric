package dev.simulated_team.simulated.ponder.scenes;

import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import com.zurrtum.create.client.infrastructure.ponder.scenes.highLogistics.FrogAndConveyorScenes;
import dev.simulated_team.simulated.content.blocks.rope.rope_connector.RopeConnectorBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchBlockEntity;
import dev.simulated_team.simulated.index.SimItems;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.elements.rope.RopeStrandElement;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RopeScenes {
    public static void ropeIntro(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();
        final VectorUtil vector = util.vector();
        final EffectInstructions effects = scene.effects();

        scene.title("rope_intro", "Connecting Simulated Contraptions Using Ropes");

        scene.configureBasePlate(1, 3, 5);
        scene.setSceneOffsetY(-1);
        scene.showBasePlate();

        final BlockPos winch = new BlockPos(4, 3, 5);

        final Selection structure = select.fromTo(4, 1, 5, 5, 3, 5);

        final Selection leftZipline = select.fromTo(6, 0, 0, 6, 15, 0);
        final Selection rightZipline = select.fromTo(0, 0, 10, 0, 12, 10);

        final BlockPos connector = new BlockPos(1, 1, 5);

        final Selection kinetics = select.fromTo(4,3,4,4,3,5);

        final double connectorRopeOffset = 4.0 / 16.0;

        scene.idle(10);
        world.showSection(structure, Direction.DOWN);
        scene.idle(5);
        world.showSection(select.position(connector), Direction.DOWN);
        scene.idle(15);

        overlay.showControls(vector.topOf(winch).subtract(0,0.25,0), Pointing.DOWN, 60).withItem(SimItems.ROPE_COUPLING.asStack());
        scene.idle(6);

        // Using chasingLine because nothing else has custom line width
        scene.addInstruction(new ChasingLineInstruction(
                vector.centerOf(winch),
                vector.centerOf(winch),
                7,
                PonderPalette.GREEN.getColor(),
                "winch",
                0,
                41,
                SmoothMovementUtils.linear()));

        scene.idle(20);

        overlay.showControls(vector.centerOf(connector), Pointing.DOWN, 40).withItem(SimItems.ROPE_COUPLING.asStack());

        scene.idle(6);

        scene.addInstruction(new ChasingLineInstruction(
                vector.centerOf(connector).subtract(0, connectorRopeOffset,0),
                vector.centerOf(connector).subtract(0, connectorRopeOffset,0),
                6,
                PonderPalette.GREEN.getColor(),
                "connector",
                0,
                5,
                SmoothMovementUtils.linear()));

        scene.addInstruction(new ChasingLineInstruction(
                vector.centerOf(winch),
                vector.centerOf(connector).subtract(0, connectorRopeOffset,0),
                1,
                PonderPalette.BLACK.getColor(),
                5,
                5,
                SmoothMovementUtils.quadraticRise()));

        scene.idle(15);

        final RopeStrandElement initialRope = new RopeStrandElement(winch.getCenter(), connector.getCenter().subtract(0, connectorRopeOffset,0), 4, 0, 1);
        scene.addInstruction(new CreateRopeStrandInstruction(initialRope));

        scene.world().modifyBlockEntity(winch, RopeWinchBlockEntity.class, be -> be.getRopeHolder().renderAttached = true);
        scene.world().modifyBlockEntity(connector, RopeConnectorBlockEntity.class, be -> be.getRopeHolder().renderAttached = true);

        initialRope.modify(30)
                .setSog(0.2)
                .setInterpolator(SmoothMovementUtils.elasticOut())
                .start(scene);

        scene.idle(30);

        overlay.showText(80)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(vector.centerOf(3,2,5))
                .text("Right-Clicking a Rope Winch and Connector with a Rope Coupling will attach them together");

        scene.idle(100);

        world.showSection(select.position(4,3,4), Direction.SOUTH);

        scene.idle(20);

        overlay.showText(120)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(vector.centerOf(winch))
                .text("When given Rotational Force, Rope Winches reel in and out the attached rope");

        scene.idle(20);

        world.setKineticSpeed(kinetics, 32);

        initialRope.modify(30)
                .setSog(1.4)
                .setInterpolator(SmoothMovementUtils.quadraticRiseOut())
                .setLength(6)
                .start(scene);

        scene.idle(20);

        world.setKineticSpeed(kinetics, 0);

        scene.idle(40);

        world.setKineticSpeed(kinetics, -32);

        initialRope.modify(50)
                .setSog(0.1)
                .setInterpolator(SmoothMovementUtils.softElasticOut())
                .setLength(4)
                .start(scene);

        scene.idle(30);

        world.setKineticSpeed(kinetics, 0);

        scene.idle(30);

        overlay.showText(90)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(vector.topOf(2,1,5))
                .text("Shears can be used to remove the Rope");

        scene.idle(40);

        overlay.showControls(vector.of(3,2.75,5.5), Pointing.DOWN, 30).withItem(Items.SHEARS.getDefaultInstance()).rightClick();

        scene.idle(5);

        for (int i = 0; i < 8; i++) {
            scene.addInstruction(new OffsetBreakParticlesInstruction(AABB.ofSize(vector.of(1.6 + ((double) i / 3),1.5 + ((double) i / 3.5),5.5), 0.5, 0.5,0.5), Blocks.BROWN_WOOL.defaultBlockState()));
        }
        scene.addInstruction(new RemoveRopeStrandInstruction(initialRope, scene));
        scene.world().modifyBlockEntity(winch, RopeWinchBlockEntity.class, be -> be.getRopeHolder().renderAttached = false);
        scene.world().modifyBlockEntity(connector, RopeConnectorBlockEntity.class, be -> be.getRopeHolder().renderAttached = false);

        scene.idle(65);

        world.hideSection(structure.add(select.position(connector)).add(select.position(4,3,4)), Direction.UP);

        scene.idle(20);

        final ElementLink<WorldSectionElement> leftZiplineSection = world.showIndependentSection(leftZipline, Direction.UP);
        final ElementLink<WorldSectionElement> rightZiplineSection = world.showIndependentSection(rightZipline, Direction.UP);

        final Vec3 leftOffset = vector.of(-3, -9.5, 0);
        final Vec3 rightOffset = vector.of(2.5,-11,0);

        world.moveSection(leftZiplineSection, leftOffset,0);
        world.moveSection(rightZiplineSection, rightOffset,0);

        final BlockPos ziplineStartConnector = new BlockPos(6,15,0);
        final BlockPos ziplineEndConnector = new BlockPos(0,12,10);

        final Vec3 ziplineStart = vector.centerOf(ziplineStartConnector).add(leftOffset).subtract(0, connectorRopeOffset,0);
        final Vec3 ziplineEnd = vector.centerOf(ziplineEndConnector).add(rightOffset).subtract(0, connectorRopeOffset,0);
        final RopeStrandElement ziplineRope = new RopeStrandElement(ziplineStart, ziplineEnd, 10, 0, 1);

        scene.idle(15);

        scene.addInstruction(new CreateRopeStrandInstruction(ziplineRope));
        scene.world().modifyBlockEntity(ziplineStartConnector, RopeConnectorBlockEntity.class, be -> be.getRopeHolder().renderAttached = true);
        scene.world().modifyBlockEntity(ziplineEndConnector, RopeConnectorBlockEntity.class, be -> be.getRopeHolder().renderAttached = true);

        ziplineRope.modify(30)
                .setSog(0.1)
                .setInterpolator(SmoothMovementUtils.elasticOut())
                .start(scene);

        scene.idle(20);

        overlay.showText(70)
                .attachKeyFrame()
                .text("Right-Click holding a wrench to slide down the Rope")
                .independent(30);

        scene.idle(40);
        final ElementLink<ParrotElement> parrot = new ElementLinkImpl<>(ParrotElement.class);
        final Vec3 parrotStart = vector.of(6.5,13.7,1).add(leftOffset);
        final FrogAndConveyorScenes.ChainConveyorParrotElement element =
                new FrogAndConveyorScenes.ChainConveyorParrotElement(parrotStart, ParrotPose.FacePointOfInterestPose::new);
        scene.addInstruction(new CreateParrotInstruction(0, Direction.DOWN, element));
        scene.addInstruction(s -> s.linkElement(element, parrot));
        scene.special().movePointOfInterest(vector.of(0,11,12).add(rightOffset));

        scene.addInstruction(CustomAnimateParrotInstruction.move(parrot, vector.of(-0.5, -3.9, 8.7), 60, SmoothMovementUtils.cubicRise()));

        scene.idle(60);

        scene.addInstruction(CustomAnimateParrotInstruction.move(parrot, vector.of(0, 0.2, -0.4), 10, SmoothMovementUtils.quadraticJump()));

        scene.idle(10);

        world.moveSection(leftZiplineSection, vector.of(0,-3,0),30);
        world.moveSection(rightZiplineSection, vector.of(0,3,0),30);

        ziplineRope.modify(30)
                .setInterpolator(SmoothMovementUtils.linear())
                .setStart(ziplineStart.subtract(0,3,0))
                .setEnd(ziplineEnd.add(0,3,0))
                .start(scene);

        scene.addInstruction(CustomAnimateParrotInstruction.move(parrot, vector.of(0.1, 2.55, 0), 30, SmoothMovementUtils.linear()));
        scene.idle(5);
        scene.addInstruction(CustomAnimateParrotInstruction.move(parrot, vector.of(0.4, -1.2, -8.5), 60, SmoothMovementUtils.cubicRise()));
        scene.idle(5);

        scene.special().movePointOfInterest(vector.of(6,12,-2).add(leftOffset));

        scene.idle(20);

        ziplineRope.modify(20)
                .setInterpolator(SmoothMovementUtils.elasticOut())
                .setSog(0)
                .start(scene);

        scene.idle(30);

        scene.special().hideElement(parrot, Direction.EAST);

        scene.idle(5);

        scene.addInstruction(CustomAnimateParrotInstruction.move(parrot, vector.of(-0.1, 0.2, 0.4), 10, SmoothMovementUtils.quadraticJump()));

        scene.idle(10);
    }

    public static void ropeConnections(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();
        final VectorUtil vector = util.vector();

        scene.title("rope_connections", "Types of Rope Connections");

        scene.configureBasePlate(1, 3, 5);
        scene.setSceneOffsetY(-1);
        scene.showBasePlate();

        final BlockPos tallWinch = new BlockPos(4, 2, 4);
        final BlockPos tallConnector = new BlockPos(4, 3, 6);

        final Selection leftStructure = select.fromTo(4, 1, 4, 5, 2, 4);
        final Selection rightStructure = select.fromTo(4, 1, 6, 5, 3, 6).add(select.position(tallConnector));

        final Selection leftZipline = select.fromTo(6, 0, 0, 6, 15, 0);
        final Selection rightZipline = select.fromTo(0, 0, 10, 0, 12, 10);

        final BlockPos connector = new BlockPos(1, 1, 5);
        final BlockPos lowWinch = new BlockPos(1, 1, 4);
        final BlockPos lowConnector = new BlockPos(1, 1, 6);

        world.showSection(rightStructure, Direction.DOWN);
        world.showSection(select.position(lowConnector), Direction.DOWN);
        world.showSection(leftStructure, Direction.DOWN);
        world.showSection(select.position(lowWinch), Direction.DOWN);

        scene.idle(20);

        overlay.showControls(vector.centerOf(tallConnector).add(0,0,0.25), Pointing.LEFT, 40).withItem(SimItems.ROPE_COUPLING.asStack());
        overlay.showControls(vector.centerOf(lowConnector).subtract(0,-0.25,0.5), Pointing.RIGHT, 40).withItem(SimItems.ROPE_COUPLING.asStack());

        scene.idle(6);

        final RopeStrandElement rightRope = new RopeStrandElement(tallConnector.getCenter().add(0.15,0,0), lowConnector.getCenter().subtract(0,0.15,0), 4, 0, 1);
        scene.addInstruction(new CreateRopeStrandInstruction(rightRope));

        scene.world().modifyBlockEntity(connector, RopeConnectorBlockEntity.class, be -> be.getRopeHolder().renderAttached = true);

        rightRope.modify(30)
                .setSog(0.2)
                .setInterpolator(SmoothMovementUtils.elasticOut())
                .start(scene);

        scene.idle(20);

        overlay.showText(60)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(vector.centerOf(3,2,6))
                .colored(PonderPalette.GREEN)
                .text("A pair of Rope Connectors can be attached with Rope");

        scene.idle(80);

        scene.idle(20);

        overlay.showControls(vector.centerOf(tallWinch).add(0,0,0.25), Pointing.LEFT, 20).withItem(SimItems.ROPE_COUPLING.asStack());
        overlay.showControls(vector.centerOf(lowWinch).subtract(0,-0.25,0.5), Pointing.RIGHT, 20).withItem(SimItems.ROPE_COUPLING.asStack());

        scene.idle(6);

        overlay.showLine(PonderPalette.RED, vector.centerOf(tallWinch), vector.centerOf(lowWinch), 80);

        scene.idle(20);

        overlay.showText(60)
                .placeNearTarget()
                .pointAt(vector.of(3,2.25,4.5))
                .colored(PonderPalette.RED)
                .text("A pair of Rope Winches cannot connect");
    }
}