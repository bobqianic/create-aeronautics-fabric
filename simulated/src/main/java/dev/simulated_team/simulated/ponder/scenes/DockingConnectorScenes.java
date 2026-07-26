package dev.simulated_team.simulated.ponder.scenes;

import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.instructions.*;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.scene.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class DockingConnectorScenes {
    public static void DockingConnector(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final CreateSceneBuilder.EffectInstructions effects = scene.effects();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();
        scene.title("docking_connector", "Using Docking Connectors");
        scene.configureBasePlate(0, 0, 12);
        scene.setSceneOffsetY(-1.5f);
        scene.scaleSceneView(0.7f);
        world.showSection(select.fromTo(0,0,0,11,0,11), Direction.UP);
        scene.idle(10);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(new BlockPos(2,3,5), true, true));
        BlockPos connector1 = new BlockPos(8,3,5);
        BlockPos connector2 = new BlockPos(5,3,5);

        world.showSection(select.fromTo(8,1,4,8,2,6),Direction.DOWN);
        scene.idle(5);
        world.showSection(select.position(8,3,5),Direction.DOWN);
        scene.idle(7);
        ElementLink<WorldSectionElement> cartCenter = world.showIndependentSection(select.fromTo(2,2,5,5,2,5),Direction.DOWN);
        ElementLink<WorldSectionElement> cartLeft = world.showIndependentSection(select.fromTo(2,1,3,5,2,3),Direction.DOWN);
        ElementLink<WorldSectionElement> cartRight = world.showIndependentSection(select.fromTo(2,1,7,5,2,7),Direction.DOWN);

        List<ElementLink<WorldSectionElement>> cartSections = new ArrayList<>();
        cartSections.add(cartCenter);
        cartSections.add(cartLeft);
        cartSections.add(cartRight);
        world.configureCenterOfRotation(cartCenter,new Vec3(4,2,5.5));
        world.configureCenterOfRotation(cartLeft,new Vec3(4,2,4.5));
        world.configureCenterOfRotation(cartRight,new Vec3(4,2,6.5));
        for (ElementLink<WorldSectionElement> section : cartSections) {
            world.rotateSection(section,0,60,0,0);
            world.moveSection(section,new Vec3(-1,0,3),0);
        }
        world.moveSection(cartLeft,new Vec3(0,0,1),0);
        world.moveSection(cartRight,new Vec3(0,0,-1),0);

        scene.idle(5);
        for (int i = 5; i >= 2; i--) {
            world.showSectionAndMerge(select.position(i, 3, 5), Direction.DOWN, cartCenter);
            scene.idle(3);
        }
        ElementLink<WorldSectionElement> cartFence = world.showIndependentSection(select.fromTo(2,4,4,4,4,6),Direction.DOWN);
        world.configureCenterOfRotation(cartFence,new Vec3(4,2,5.5));
        world.rotateSection(cartFence,0,60,0,0);
        world.moveSection(cartFence,new Vec3(-1,-1,3),0);
        cartSections.add(cartFence);
        scene.idle(15);
        overlay.showText(60)
                .pointAt(new Vec3(3, 4, 6))
                .attachKeyFrame()
                .placeNearTarget()
                .text("Docking Connectors can create rigid connections in pairs");
        scene.idle(70);
        world.toggleRedstonePower(select.fromTo(connector1.below().north(),connector1));
        effects.indicateRedstone(new BlockPos(connector1.below().north()));
        scene.idle(15);
        world.toggleRedstonePower(select.fromTo(connector2,connector2.west()));
        effects.indicateRedstone(new BlockPos(3,3,8));
        scene.idle(15);
        for (ElementLink<WorldSectionElement> section : cartSections) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(section,new Vec3(0,0,-3),30, SmoothMovementUtils.cubicSmoothing()));
            scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(section,new Vec3(0,-60,0),30, SmoothMovementUtils.cubicSmoothing()));
        }
        scene.idle(15);
        for (ElementLink<WorldSectionElement> section : cartSections) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(section,new Vec3(1,0,0),20, SmoothMovementUtils.quadraticRise()));
        }
        scene.idle(25);
        overlay.showText(55)
                .pointAt(new Vec3(7,3.5,5.5))
                .attachKeyFrame()
                .placeNearTarget()
                .text("Extended Connectors attempt to align with each other");

        scene.idle(65);

        scene.addInstruction(new ToggleConnectorLockInstruction(connector1,true));
        scene.addInstruction(new ToggleConnectorLockInstruction(connector2,true));
        scene.addInstruction(new LinkDockingConnectorsInstruction(connector1,connector2));
        scene.idle(20);
        overlay.showText(60)
                .pointAt(new Vec3(7,3.5,5.5))
                .attachKeyFrame()
                .placeNearTarget()
                .text("Once sufficiently aligned, the Connectors will lock together");
        scene.idle(70);

        //hammer time
        world.showSection(select.fromTo(5,1,8,6,8,8),Direction.DOWN);
        ElementLink<WorldSectionElement> hammer = world.showIndependentSection(select.fromTo(3,2,9,5,8,11),Direction.DOWN);
        world.configureCenterOfRotation(hammer,new Vec3(5.5,7.5,10.5));
        world.moveSection(hammer,new Vec3(0,0,-2),0);
        world.rotateSection(hammer,-60,0,0,0);
        scene.idle(10);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(hammer,new Vec3(60,0,0),20,SmoothMovementUtils.quadraticRise()));
        scene.idle(20);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(hammer,new Vec3(-10,0,0),10,SmoothMovementUtils.quadraticRiseDual()));
        scene.idle(10);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(hammer,new Vec3(10,0,0),20,SmoothMovementUtils.cubicSmoothing()));
        scene.idle(15);
        overlay.showText(60)
                .pointAt(new Vec3(7,3.5,5.5))
                .attachKeyFrame()
                .placeNearTarget()
                .text("The Connection is rigid, but keeps the two sides independent");
        scene.idle(70);
        world.hideIndependentSection(hammer,Direction.UP);
        world.hideSection(select.fromTo(5,1,8,6,8,8),Direction.UP);
        scene.idle(20);

        world.moveSection(cartRight,new Vec3(0,0,1),10);
        world.moveSection(cartLeft,new Vec3(0,0,-1),10);
        world.hideIndependentSection(cartFence,Direction.UP);
        scene.idle(15);
        Selection cartBeltSelection = select.fromTo(2,2,4,5,3,4).add(select.fromTo(2,2,6,6,3,6));
        Selection mainBeltSelection = select.fromTo(9,1,5,11,3,6).add(select.position(8,3,6)).add(select.fromTo(11,1,4,11,3,4));
        Selection cogwheelSelection = select.fromTo(9,1,7,12,1,7).add(select.position(12,0,8));

        world.showSection(cartBeltSelection,Direction.DOWN);
        scene.idle(10);
        world.showSection(cogwheelSelection,Direction.DOWN);
        scene.idle(5);
        world.showSection(mainBeltSelection,Direction.DOWN);

        scene.world().modifyBlockEntity(new BlockPos(11,1,4), FluidTankBlockEntity.class, be -> be.getTankInventory()
                .insert(new FluidStack(Fluids.LAVA, 12 * BucketFluidInventory.CAPACITY)));
        scene.idle(10);

        BlockPos entryBeltCart = new BlockPos(3,2,4);
        BlockPos exitBeltCart = new BlockPos(5,2,4);
        BlockPos entryBeltGround = new BlockPos(9,2,5);
        ItemStack itemStack = Blocks.BIRCH_LOG.asItem().getDefaultInstance();
        itemStack.setCount(16);

        int items = 11;
        int steps = items*2;
        for (int i = 0; i < steps+2; i++) {
            int j = i/2;
            if((i&1)==0) {
                if (j < items)
                    world.createItemOnBelt(entryBeltCart, Direction.WEST, itemStack);
                if (j > 0) {
                    world.removeItemsFromBelt(exitBeltCart);
                    world.flapFunnel(exitBeltCart.above(), false);
                }
            }
            else if(j>0)
                world.createItemOnBelt(entryBeltGround,Direction.WEST,itemStack);
            scene.idle(8);

            if(i==2)
            {
                overlay.showText(65)
                        .pointAt(new Vec3(5.5,3.5,4.5))
                        .attachKeyFrame()
                        .placeNearTarget()
                        .text("Once locked, Items and Fluids can be transferred in either direction");
            }
            if(i==13)
            {
                overlay.showOutline(PonderPalette.BLUE,0,select.fromTo(9,3,5,9,3,6).add(select.position(5,3,4)).add(select.position(4,3,6)),90);
            }
            if(i == 14)
            {
                overlay.showText(80)
                        .pointAt(new Vec3(5.5,3.5,4.5))
                        .attachKeyFrame()
                        .placeNearTarget()
                        .text("Insertion and Extraction must occur on both sides of the connection");
            }
        }
        scene.idle(15);

        world.hideSection(mainBeltSelection,Direction.UP);
        scene.idle(5);
        world.hideSection(cogwheelSelection,Direction.UP);
        scene.idle(5);
        world.hideSection(cartBeltSelection,Direction.UP);
        scene.idle(15);
        world.moveSection(cartRight,new Vec3(0,0,-1),10);
        world.moveSection(cartLeft,new Vec3(0,0,1),10);
        scene.idle(10);
        cartFence = world.showIndependentSection(select.fromTo(2,4,4,4,4,6),Direction.DOWN);
        world.configureCenterOfRotation(cartFence,new Vec3(4,2,5.5));
        world.moveSection(cartFence,new Vec3(0,-1,0),0);
        cartSections.add(cartFence);
        scene.idle(30);
        world.toggleRedstonePower(select.fromTo(connector2,connector2.west()));
        effects.indicateRedstone(connector2.west());
        scene.addInstruction(new ToggleConnectorLockInstruction(connector1,false));
        scene.addInstruction(new ToggleConnectorLockInstruction(connector2,false));
        scene.idle(20);
        overlay.showText(70)
                .pointAt(connector2.getCenter())
                .attachKeyFrame()
                .placeNearTarget()
                .text("Either Connector can be retracted to undock");
        scene.idle(80);

        for (ElementLink<WorldSectionElement> section : cartSections) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(section,new Vec3(-1.5,0,0),20,SmoothMovementUtils.cubicSmoothing()));
        }
        scene.idle(20);
        Selection comparatorSelection = select.fromTo(9,2,4,10,3,4);
        ElementLink<WorldSectionElement> comparatorSection = world.showIndependentSection(comparatorSelection,Direction.DOWN);
        world.moveSection(comparatorSection,new Vec3(0,0,1),0);
        scene.idle(20);
        world.toggleRedstonePower(select.fromTo(connector2,connector2.west()));
        scene.idle(10);
        scene.addInstruction(new RedstoneSignalInstruction(comparatorSelection,8));
        effects.indicateRedstone(new BlockPos(10,3,5));
        scene.idle(10);
        overlay.showText(80)
                .pointAt(new BlockPos(9,3,5).getCenter())
                .attachKeyFrame()
                .placeNearTarget()
                .text("Redstone Comparators can be used to read the Docking progress");
        scene.idle(20);

        for (ElementLink<WorldSectionElement> section : cartSections) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(section,new Vec3(1.5,0,0),40,SmoothMovementUtils.cubicSmoothing()));
        }
        for (int i = 8; i < 15; i++) {
            scene.idle(4);
            scene.addInstruction(new RedstoneSignalInstruction(comparatorSelection,i));
        }
        scene.idle(10);
        scene.addInstruction(new ToggleConnectorLockInstruction(connector1,true));
        scene.addInstruction(new ToggleConnectorLockInstruction(connector2,true));
        scene.idle(10);
        scene.addInstruction(new RedstoneSignalInstruction(comparatorSelection,15));
    }
}
