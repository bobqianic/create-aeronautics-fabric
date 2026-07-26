package dev.simulated_team.simulated.ponder.scenes;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.zurrtum.create.content.logistics.funnel.AndesiteFunnelBlock;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import dev.simulated_team.simulated.content.blocks.auger_shaft.AugerCogBlock;
import dev.simulated_team.simulated.content.blocks.auger_shaft.AugerShaftBlock;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.ponder.instructions.AirflowAABBInstruction;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.EntityElement;
import com.zurrtum.create.client.ponder.api.scene.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AugerShaftScenes {

    public static void augerShaftIntro(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("auger_shaft_intro", "Using the Auger Shaft");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final SelectionUtil select = util.select();
        final OverlayInstructions overlay = scene.overlay();
        final VectorUtil vector = util.vector();

        final BlockPos augerCog = util.grid().at(2, 2, 3);
        final Selection augerShafts = select.fromTo(2, 2, 1, 2, 2, 4);

        final BlockPos inputFunnel = util.grid().at(2, 3, 1);
        final BlockPos invalidFunnel = util.grid().at(2, 2, 0);
        final BlockPos invalidSideFunnel = util.grid().at(1, 2, 3);
        final Selection validFunnel = select.fromTo(1,1,4,1, 2, 4);
        final Selection smallCogs = select.fromTo(2, 1, 5, 2,2,5);
        final BlockPos largeCog = util.grid().at(1, 0, 5);
        final Selection andesiteCasing = select.fromTo(2, 1, 1, 2, 1, 4);

        world.setKineticSpeed(select.position(largeCog), 16);
        world.setKineticSpeed(select.position(2,1,5), -32);
        world.setKineticSpeed(select.position(2,2,5), 32);
        world.setKineticSpeed(augerShafts, 32);
        world.showSection(smallCogs, Direction.UP);
        world.showSection(select.position(largeCog), Direction.UP);
        world.showSection(andesiteCasing, Direction.UP);
        scene.idle(5);
        world.showSection(augerShafts, Direction.DOWN);
        scene.idle(10);
        world.showSection(select.position(inputFunnel), Direction.DOWN);
        world.setBlock(inputFunnel, AllBlocks.ANDESITE_FUNNEL.defaultBlockState().setValue(AndesiteFunnelBlock.FACING, Direction.UP), false);
        scene.idle(12);
        world.modifyBlock(new BlockPos(2, 2, 1), s -> s.setValue(AugerShaftBlock.UP, true), false);
        scene.idle(8);
        overlay.showText(80)
                .attachKeyFrame()
                .text("Powered Auger Shafts will transport items and extract them from actors")
                .placeNearTarget()
                .pointAt(vector.blockSurface(util.grid().at(2,2, 2), Direction.WEST));
        for (int i = 0; i < 3; i++) {
            final ItemStack stack = new ItemStack(Items.COPPER_BLOCK);
            final ElementLink<EntityElement> remove =
                    world.createItemEntity(new Vec3(2.5, 5, 1.5), Vec3.ZERO, stack);
            scene.idle(9);
            world.modifyEntity(remove, Entity::discard);
            scene.idle(10);
        }
        scene.idle(30);
        world.setBlock(inputFunnel, Blocks.AIR.defaultBlockState(), true);
        world.modifyBlock(new BlockPos(2, 2, 1), s -> s.setValue(AugerShaftBlock.UP, false), false);
        scene.idle(20);
        scene.addKeyframe();
        scene.overlay().showControls(vector.topOf(augerCog), Pointing.DOWN, 80).rightClick().withItem(AllItems.WRENCH.getDefaultInstance());
        scene.idle(7);
        world.modifyBlock(augerCog.north(), s -> s.setValue(AugerShaftBlock.SECTION, AugerShaftBlock.BarrelSection.FRONT), false);
        world.modifyBlock(augerCog.south(), s -> s.setValue(AugerShaftBlock.SECTION, AugerShaftBlock.BarrelSection.SINGLE), false);
        world.setBlock(augerCog, SimBlocks.AUGER_COG.getDefaultState().setValue(AugerCogBlock.AXIS, Direction.Axis.Z), true);
        overlay.showText(80)
                .text("Using a Wrench, you can cycle between the Auger Shaft and Auger Cog")
                .placeNearTarget()
                .pointAt(vector.blockSurface(augerCog, Direction.UP));
        scene.idle(100);
        scene.overlay().showControls(vector.topOf(augerCog.north()), Pointing.DOWN, 80).rightClick().withItem(AllBlocks.INDUSTRIAL_IRON_BLOCK.asItem().getDefaultInstance());
        scene.idle(7);
        world.modifyBlock(augerCog.north(), s -> s.setValue(AugerShaftBlock.SECTION, AugerShaftBlock.BarrelSection.FRONT).setValue(AugerShaftBlock.ENCASED, true), true);
        overlay.showText(80)
                .text("Using Industrial Iron Blocks, Auger Shafts can be encased")
                .placeNearTarget()
                .pointAt(vector.blockSurface(augerCog.north(), Direction.UP));
        scene.idle(100);
        final Vec3 augerCenter = new Vec3(2.5, 2.5, 5);
        final AABB bb = new AABB(augerCenter, augerCenter).inflate(0.5, 0.5, 0);
        scene.addInstruction(new AirflowAABBInstruction(PonderPalette.GREEN,bb.expandTowards(0, 0, -4),150,Direction.SOUTH,0.75f,1.5f));
        overlay.showText(60)
                .attachKeyFrame()
                .text("Item flow can be observed via the indicator on the Auger Cog...")
                .placeNearTarget()
                .pointAt(vector.blockSurface(augerCog, Direction.WEST));
        scene.idle(80);
        scene.overlay().showControls(new Vec3(3, 2.5, 2.5), Pointing.RIGHT, 60).withItem(AllItems.GOGGLES.getDefaultInstance());
        overlay.showText(60)
                .text("...Or by inspecting the Auger while wearing Goggles")
                .placeNearTarget()
                .pointAt(new Vec3(2, 2.5, 2.5));
        scene.idle(85);
        world.setBlock(invalidFunnel, AllBlocks.ANDESITE_FUNNEL.defaultBlockState().setValue(AndesiteFunnelBlock.FACING, Direction.NORTH), false);
        world.setBlock(invalidSideFunnel, AllBlocks.ANDESITE_FUNNEL.defaultBlockState().setValue(AndesiteFunnelBlock.FACING, Direction.WEST).setValue(AndesiteFunnelBlock.EXTRACTING, true), false);
        world.setBlock(new BlockPos(1,2,4), AllBlocks.ANDESITE_BELT_FUNNEL.defaultBlockState().setValue(BeltFunnelBlock.HORIZONTAL_FACING, Direction.WEST).setValue(BeltFunnelBlock.SHAPE, BeltFunnelBlock.Shape.PUSHING), false);
        world.showSection(select.position(invalidFunnel), Direction.DOWN);
        world.showSection(select.position(invalidSideFunnel), Direction.DOWN);
        scene.idle(10);
        overlay.showOutlineWithText(select.position(invalidSideFunnel), 60)
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .text("Items cannot be inserted or extracted from the Auger Cog...")
                .pointAt(invalidSideFunnel.getCenter())
                .placeNearTarget();
        scene.idle(80);
        overlay.showOutlineWithText(select.position(invalidFunnel), 60)
                .colored(PonderPalette.RED)
                .text("...or either end of the Auger")
                .pointAt(invalidFunnel.getCenter())
                .placeNearTarget();
        scene.idle(80);
        world.showSection(validFunnel, Direction.DOWN);
        scene.idle(12);
        world.modifyBlock(new BlockPos(2, 2, 4), s -> s.setValue(AugerShaftBlock.WEST, true), false);
        world.createItemOnBeltLike(new BlockPos(1,1,4), Direction.EAST, new ItemStack(Items.COPPER_BLOCK));
    }

    public static void augerShaftExtracting(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("auger_shaft_extracting", "Extracting from Harvesting actors using the Auger Shaft");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();

        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final SelectionUtil select = util.select();
        final OverlayInstructions overlay = scene.overlay();
        final VectorUtil vector = util.vector();

        final BlockPos largeCog = util.grid().at(7, 0, 3);
        final Selection kineticsShaft = select.fromTo(7, 1, 4, 4, 1, 3);
        final Selection allCogs = select.fromTo(2, 1, 3, 4, 2, 3);

        final Selection bottomActors = select.fromTo(2, 1, 2, 4, 1, 2);
        final Selection allActors = select.fromTo(2, 1, 2, 4, 2, 2);

        final Selection bottomAuger = select.fromTo(3, 1, 3, 3, 1, 5);
        final Selection topAuger = select.fromTo(3, 2, 3, 3, 2, 5);

        final Selection planks = select.fromTo(2, 1, 1, 4, 2, 1);
        final Selection andesiteCasing = select.fromTo(3, 1, 4, 3, 1, 5);
        final Selection funnel = select.fromTo(2, 1, 5, 2, 2, 5);

        world.setKineticSpeed(bottomAuger, 0);
        world.showSection(bottomAuger, Direction.DOWN);
        scene.idle(15);
        world.showSection(bottomActors, Direction.DOWN);

        overlay.showText(180)
                .attachKeyFrame()
                .text("Auger Shafts collect materials from Harvesting actors")
                .placeNearTarget()
                .pointAt(vector.blockSurface(new BlockPos(2, 1, 2), Direction.WEST));

        scene.idle(20);
        final BlockState[] states = {AllBlocks.MECHANICAL_HARVESTER.defaultBlockState(), AllBlocks.MECHANICAL_SAW.defaultBlockState(), AllBlocks.MECHANICAL_DRILL.defaultBlockState()};
        for (final BlockState state : states) {
            scene.idle(20);
            world.hideSection(bottomActors, Direction.EAST);
            scene.idle(20);
            world.setBlocks(bottomActors, state, false);
            world.showSection(bottomActors, Direction.EAST);
        }
        scene.idle(40);
        world.hideSection(bottomActors, Direction.UP);
        world.hideSection(bottomAuger, Direction.UP);

        scene.idle(20);

        world.setBlock(new BlockPos(3, 1, 3), AllBlocks.ANDESITE_ENCASED_COGWHEEL.defaultBlockState().setValue(EncasedCogwheelBlock.AXIS, Direction.Axis.Z), false);
        world.setBlocks(andesiteCasing, AllBlocks.ANDESITE_CASING.defaultBlockState(), false);

        world.setKineticSpeed(select.position(3, 1, 3), -32);
        world.setKineticSpeed(select.position(3, 1, 4), -32);

        world.setKineticSpeed(select.position(2, 1, 2), 32);
        world.setKineticSpeed(select.position(3, 1, 2), -32);
        world.setKineticSpeed(select.position(4, 1, 2), 32);

        world.showSection(andesiteCasing, Direction.DOWN);
        world.showSection(topAuger, Direction.DOWN);
        world.showSection(allActors, Direction.DOWN);
        world.showSection(select.position(largeCog), Direction.DOWN);
        world.showSection(kineticsShaft, Direction.DOWN);
        world.showSection(allCogs, Direction.DOWN);

        scene.idle(20);

        final AABB bb = new AABB(3, 2, 3, 4, 3, 3);
        overlay.chaseBoundingBoxOutline(PonderPalette.INPUT, bb, bb.expandTowards(0, 0, 0), 5);
        overlay.chaseBoundingBoxOutline(PonderPalette.INPUT, bb, bb.expandTowards(0, 0, -1), 15);
        scene.idle(15);
        overlay.chaseBoundingBoxOutline(PonderPalette.INPUT, bb, bb.inflate(1, 0, 0).expandTowards(0, -1, -1), 70);
        overlay.showText(70)
                .attachKeyFrame()
                .text("All horizontally connected actors are included")
                .placeNearTarget()
                .pointAt(vector.blockSurface(new BlockPos(2, 2, 2), Direction.WEST));
        scene.idle(80);
        scene.rotateCameraY(-90);
        scene.idle(30);
        world.showSection(planks, Direction.DOWN);
        scene.idle(30);

        final BlockPos p1 = util.grid().at(2, 1, 1);
        final BlockPos p2 = util.grid().at(3, 1, 1);
        final BlockPos p3 = util.grid().at(4, 1, 1);
        final BlockPos p4 = util.grid().at(2, 2, 1);
        final BlockPos p5 = util.grid().at(3, 2, 1);
        final BlockPos p6 = util.grid().at(4, 2, 1);

        for (int i = 0; i < 10; i++) {
            scene.idle(10);
            world.incrementBlockBreakingProgress(p1);
            world.incrementBlockBreakingProgress(p2);
            world.incrementBlockBreakingProgress(p3);
            world.incrementBlockBreakingProgress(p4);
            world.incrementBlockBreakingProgress(p5);
            world.incrementBlockBreakingProgress(p6);
            if (i == 1) {
                overlay.showText(70)
                        .attachKeyFrame()
                        .colored(PonderPalette.INPUT)
                        .text("When a connected actor breaks a block...")
                        .placeNearTarget()
                        .pointAt(vector.blockSurface(new BlockPos(3, 2, 2), Direction.UP));

            }
        }
        scene.idle(10);
        scene.addInstruction(new AirflowAABBInstruction(PonderPalette.GREEN, bb.expandTowards(0, 0, 3), 110, Direction.SOUTH, 0.75f, 1.5f));
        scene.idle(40);
        overlay.showText(70)
                .colored(PonderPalette.OUTPUT)
                .text("...its items are automatically collected")
                .placeNearTarget()
                .pointAt(vector.blockSurface(new BlockPos(3, 2, 4), Direction.EAST));
        scene.idle(70);
        world.setBlock(new BlockPos(2,2,5), AllBlocks.ANDESITE_BELT_FUNNEL.defaultBlockState().setValue(BeltFunnelBlock.HORIZONTAL_FACING, Direction.WEST).setValue(BeltFunnelBlock.SHAPE, BeltFunnelBlock.Shape.PUSHING), false);
        world.showSection(funnel, Direction.DOWN);
        scene.idle(12);
        world.modifyBlock(new BlockPos(3, 2, 5), s -> s.setValue(AugerShaftBlock.WEST, true), false);
        world.createItemOnBeltLike(new BlockPos(2,1,5), Direction.EAST, new ItemStack(Items.OAK_PLANKS));

    }
}
