package dev.eriksonn.aeronautics.content.ponder.scenes;

import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import dev.eriksonn.aeronautics.content.ponder.instructions.ChangePropellerRotateInstruction;
import dev.eriksonn.aeronautics.content.ponder.instructions.PropellerParticleSpawningInstruction;
import dev.eriksonn.aeronautics.content.ponder.instructions.PropellerRotateInstruction;
import dev.eriksonn.aeronautics.content.ponder.instructions.RedstoneSignalInstruction;
import dev.eriksonn.aeronautics.content.ponder.instructions.TickingStoppingInstruction;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.instructions.*;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.scene.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AirPressureScenes {
    public static void airPressure(final SceneBuilder builder, final SceneBuildingUtil util, final PressureItem pressureItem) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final CreateSceneBuilder.SpecialInstructions special = scene.special();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();
        final VectorUtil vector = util.vector();

        scene.title(pressureItem.getSceneId(), pressureItem.getSceneTitle());
        scene.configureBasePlate(2, 2, 5);
        scene.setSceneOffsetY(pressureItem.getSceneOffset()-1f);
        scene.scaleSceneView(0.9f);
        final Selection plate = select.fromTo(3,0,3,5,0,5);
        final Selection groundSelection = select.fromTo(2,0,2,6,0,6).substract(plate);
        ElementLink<WorldSectionElement> ground = world.showIndependentSection(groundSelection,Direction.UP);
        world.showSection(plate, Direction.UP);
        scene.idle(10);

        final Vec3 pointingPos = pressureItem.getPointingPos();

        pressureItem.setup(scene,util);

        scene.idle(10);
        overlay.showText(60)
                .pointAt(pointingPos)
                .attachKeyFrame()
                .placeNearTarget()
                .text(pressureItem.getItemName()+" are affected by Air Pressure");
        scene.idle(50);

        for (int i = 0; i < 8; i++) {
            final double y = (Math.exp(i / 3f) - 1) * 1.3 + 1;
            final AABB outline = new AABB(2, y, 2, 7, y, 7);
            //overlay.chaseBoundingBoxOutline(PonderPalette.INPUT, outline, outline, 150 - i * 4);
            scene.addInstruction(new ChaseAABBWithLinkInstruction(ground,PonderPalette.INPUT, outline, outline, 120 - i * 4,4));
            scene.idle(2);
        }

        scene.idle(20);


        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(ground,
                new Vec3(0, -8, 0), 60, SmoothMovementUtils.cubicSmoothing()));
        scene.addInstruction(CustomMoveBaseShadowInstruction.delta(
                new Vec3(0, -4, 0), 30, SmoothMovementUtils.quadraticRise()));

        scene.idle(15);
        world.hideIndependentSection(ground, Direction.DOWN);
        scene.addInstruction(new CustomToggleBaseShadowInstruction());

        scene.idle(15);
        final ElementLink<WorldSectionElement> cloudsSection = world.showIndependentSection(select.fromTo(1, 0, 7, 6, 2, 9).add(select.fromTo(7, 0, 0, 9, 2, 9)), Direction.DOWN);
        world.moveSection(cloudsSection, new Vec3(0, 4, 0), 0);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(cloudsSection,
                new Vec3(0, -4, 0), 30, SmoothMovementUtils.quadraticRiseDual()));
        scene.idle(50);

        overlay.showText(60)
                .pointAt(pointingPos)
                .attachKeyFrame()
                .placeNearTarget()
                .text(pressureItem.getForceName() + " weakens as Altitude increases");
        scene.idle(50);
        if(pressureItem instanceof final HoveringPressureItem hoverItem) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(cloudsSection,
                    new Vec3(0, 1, 0), 40, SmoothMovementUtils.cubicSmoothing()));

            scene.idle(40);
            overlay.showText(75)
                    .pointAt(pointingPos)
                    .attachKeyFrame()
                    .placeNearTarget()
                    .text("Simulated Contraptions will settle when "+pressureItem.getForceName()+" and Weight are in balance");

            scene.idle(90);


            //scene.idle(10);
            overlay.showText(70)
                    .pointAt(pointingPos)
                    .attachKeyFrame()
                    .placeNearTarget()
                    .text("Changing "+pressureItem.getForceName()+" thus allows changing Altitude");

            scene.idle(20);
            hoverItem.increasePower(scene,util);
            scene.idle(10);
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(cloudsSection,
                    new Vec3(0, -1, 0), 40, SmoothMovementUtils.cubicSmoothing()));

            scene.idle(80);
        }else
            scene.idle(40);
        final BlockPos sensorPos = pressureItem.getSensorPos();
        final Vec3 sensorPointing = sensorPos.getCenter();
        world.showSection(select.position(sensorPos),Direction.DOWN);
        scene.addInstruction(new AltitudeSensorVisualHeightInstruction.Linear(sensorPos, 0, 0.5f, 0.5f, f -> f));
        scene.idle(10);
        overlay.showText(70)
                .pointAt(sensorPointing)
                .attachKeyFrame()
                .placeNearTarget()
                .text("The precise Air Pressure can be read from an Altitude Sensor");
        scene.idle(80);

        pressureItem.decreasePower(scene,util);
        scene.idle(10);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(cloudsSection,
                new Vec3(0, 4, 0), 30, SmoothMovementUtils.quadraticRise()));
        scene.addInstruction(new AltitudeSensorVisualHeightInstruction.Linear(sensorPos, 60, 0.5f, -0.5f, SmoothMovementUtils.quadraticRiseInOut()));
        scene.idle(15);
        world.hideIndependentSection(cloudsSection, Direction.UP);
        scene.idle(15);
        ground = world.showIndependentSection(groundSelection,Direction.UP);
        world.moveSection(ground, new Vec3(0, -4, 0), 0);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(ground,
                new Vec3(0, 4, 0), 30, SmoothMovementUtils.quadraticRiseDual()));
        scene.addInstruction(new CustomToggleBaseShadowInstruction());
        scene.addInstruction(CustomMoveBaseShadowInstruction.delta(
                new Vec3(0, 4, 0), 30, SmoothMovementUtils.quadraticRiseDual()));
        scene.idle(5);
    }

    public interface PressureItem
    {
        default int getSceneOffset()
        {
            return 0;
        }
        String getSceneId();
        String getSceneTitle();
        void setup(final CreateSceneBuilder scene,final SceneBuildingUtil util);
        String getItemName();
        String getForceName();
        Vec3 getPointingPos();
        BlockPos getSensorPos();
        void decreasePower(final CreateSceneBuilder scene,final SceneBuildingUtil util);
    }
    public interface HoveringPressureItem extends PressureItem
    {
        void increasePower(final CreateSceneBuilder scene,final SceneBuildingUtil util);
    }
    public static class PropellerBearing implements PressureItem
    {
        PropellerRotateInstruction propellerRotate;
        @Override
        public String getSceneId()
        {
            return "propeller_pressure";
        }
        @Override
        public String getSceneTitle()
        {
            return "Effect of Air Pressure on Propeller Bearings";
        }
        @Override
        public void setup(final CreateSceneBuilder scene,final SceneBuildingUtil util)
        {
            final CreateSceneBuilder.WorldInstructions world = scene.world();
            final SelectionUtil select = util.select();
            world.showSection(select.position(4,1,4).add(select.position(5,1,5)),Direction.DOWN);
            scene.idle(5);
            world.showSection(select.fromTo(5,1,4,5,2,4),Direction.DOWN);
            world.showSection(select.fromTo(3,1,4,3,2,4),Direction.DOWN);
            scene.idle(5);
            final BlockPos bearingPos = new BlockPos(4,2,4);
            world.showSection(select.position(bearingPos),Direction.DOWN);
            scene.idle(5);
            final ElementLink<WorldSectionElement> propeller = world.showIndependentSection(select.fromTo(2,3,2,6,3,6),Direction.DOWN);

            this.propellerRotate = new PropellerRotateInstruction(bearingPos,propeller,Direction.UP,32,8);
            scene.addInstruction(this.propellerRotate);
            scene.addInstruction(new ChangePropellerRotateInstruction.SetParticles(this.propellerRotate,null,2f,-6f,2f,false));
        }
        @Override
        public String getItemName()
        {
            return "Propeller Bearings";
        }
        @Override
        public String getForceName()
        {
            return "Thrust";
        }
        @Override
        public Vec3 getPointingPos()
        {
            return new Vec3(4.5,2.5,4.5);
        }

        @Override
        public void decreasePower(final CreateSceneBuilder scene,final SceneBuildingUtil util) {
            scene.world().multiplyKineticSpeed(util.select().everywhere(),0);
            scene.addInstruction(new ChangePropellerRotateInstruction.StopRotation(this.propellerRotate,30));
        }
        public BlockPos getSensorPos()
        {
            return new BlockPos(3,1,3);
        }
    }
    public static class GyroBearing extends PropellerBearing implements HoveringPressureItem
    {
        @Override
        public String getSceneId()
        {
            return "gyro_pressure";
        }
        @Override
        public String getSceneTitle()
        {
            return "Effect of Air Pressure on Gyroscopic Propeller Bearings";
        }
        @Override
        public String getItemName()
        {
            return "Gyroscopic Propeller Bearings";
        }
        @Override
        public void increasePower(final CreateSceneBuilder scene,final SceneBuildingUtil util) {
            scene.world().multiplyKineticSpeed(util.select().everywhere(), 1.5f);
            scene.addInstruction(new ChangePropellerRotateInstruction.SetRotationRate(this.propellerRotate, 48));
        }
    }
    public static class Miniprop implements PressureItem
    {
        PropellerParticleSpawningInstruction particles1;
        PropellerParticleSpawningInstruction particles2;
        @Override
        public String getSceneId() {
            return "miniprop_pressure";
        }

        @Override
        public String getSceneTitle() {
            return "Effect of Air Pressure on Propellers";
        }

        @Override
        public void setup(final CreateSceneBuilder scene, final SceneBuildingUtil util) {
            final CreateSceneBuilder.WorldInstructions world = scene.world();
            final SelectionUtil select = util.select();
            world.showSection(select.position(4,1,4),Direction.DOWN);
            scene.idle(5);
            world.showSection(select.position(3,1,5),Direction.DOWN);
            world.showSection(select.position(5,1,3),Direction.DOWN);
            scene.idle(5);
            world.showSection(select.position(3,2,5),Direction.DOWN);
            world.showSection(select.position(5,2,3),Direction.DOWN);

            this.particles1 = new PropellerParticleSpawningInstruction(null,new BlockPos(3,2,5),Direction.DOWN,1000,1.0f,4,1);
            this.particles2 = new PropellerParticleSpawningInstruction(null,new BlockPos(5,2,3),Direction.DOWN,1000,1.0f,4,1);
            scene.addInstruction(this.particles1);
            scene.addInstruction(this.particles2);
        }

        @Override
        public String getItemName() {
            return "Propellers";
        }

        @Override
        public String getForceName() {
            return "Thrust";
        }

        @Override
        public Vec3 getPointingPos() {
            return new Vec3(3.5,2.5,5.5);
        }

        @Override
        public BlockPos getSensorPos() {
            return new BlockPos(3,1,3);
        }

        @Override
        public void decreasePower(final CreateSceneBuilder scene, final SceneBuildingUtil util) {
            scene.world().multiplyKineticSpeed(util.select().everywhere(),0);
            scene.addInstruction(new TickingStoppingInstruction(this.particles1));
            scene.addInstruction(new TickingStoppingInstruction(this.particles2));
        }
    }
    public static class Burner implements HoveringPressureItem
    {
        @Override
        public int getSceneOffset()
        {
            return -1;
        }
        @Override
        public String getSceneId() {
            return "burner_pressure";
        }

        @Override
        public String getSceneTitle() {
            return "Effect of Air Pressure on Hot Air Burners";
        }

        @Override
        public void setup(final CreateSceneBuilder scene, final SceneBuildingUtil util) {
            final CreateSceneBuilder.WorldInstructions world = scene.world();
            final SelectionUtil select = util.select();
            world.showSection(select.position(4,1,4),Direction.DOWN);
            scene.idle(5);
            world.showSection(select.position(3,1,4),Direction.DOWN);
            scene.idle(5);
            for (int i = 1; i < 4; i++) {
                world.showSection(select.position(3,i,3)
                                .add(select.position(3,i,5))
                                .add(select.position(5,i,5))
                                .add(select.position(5,i,3)),
                        Direction.DOWN);
                scene.idle(2);
            }
            world.showSection(select.fromTo(2,4,2,6,7,6),Direction.DOWN);
            scene.addInstruction(new RedstoneSignalInstruction(util.select().fromTo(4,1,4,3,1,4), 8));
        }

        @Override
        public String getItemName() {
            return "Hot Air Burners";
        }

        @Override
        public String getForceName() {
            return "Lift";
        }

        @Override
        public Vec3 getPointingPos() {
            return new Vec3(4.5,1.5,4.5);
        }

        @Override
        public BlockPos getSensorPos() {
            return new BlockPos(4,1,3);
        }
        @Override
        public void increasePower(final CreateSceneBuilder scene, final SceneBuildingUtil util) {
            scene.addInstruction(new RedstoneSignalInstruction(util.select().fromTo(4,1,4,3,1,4), 12));
        }
        @Override
        public void decreasePower(final CreateSceneBuilder scene, final SceneBuildingUtil util) {
            scene.addInstruction(new RedstoneSignalInstruction(util.select().fromTo(4,1,4,3,1,4), 0));
        }
    }
    public static class Vent extends Burner
    {
        @Override
        public String getSceneId() {
            return "vent_pressure";
        }

        @Override
        public String getSceneTitle() {
            return "Effect of Air Pressure on Steam Vents";
        }
        @Override
        public String getItemName() {
            return "Steam Vents";
        }
    }
}
