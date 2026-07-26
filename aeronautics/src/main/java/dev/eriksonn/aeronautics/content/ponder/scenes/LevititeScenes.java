package dev.eriksonn.aeronautics.content.ponder.scenes;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.Create;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import dev.eriksonn.aeronautics.content.ponder.instructions.ChangePropellerRotateInstruction;
import dev.eriksonn.aeronautics.content.ponder.instructions.PropellerRotateInstruction;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.instructions.*;
import dev.eriksonn.aeronautics.config.AeroConfig;
import dev.eriksonn.aeronautics.content.ponder.instructions.PropellerParticleSpawningInstruction;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.eriksonn.aeronautics.service.AeroLevititeService;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.ponder.api.ParticleEmitter;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.scene.*;
import com.zurrtum.create.client.ponder.foundation.SelectionImpl;
import com.zurrtum.create.client.ponder.foundation.element.InputWindowElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LevititeScenes {
    private static final net.minecraft.util.RandomSource RANDOM = net.minecraft.util.RandomSource.create();
    public static void levititeBlend(final SceneBuilder scene, final SceneBuildingUtil util) {
        scene.title("levitite_blend", "Crystallizing Levitite Blend into Levitite");
        scene.configureBasePlate(1, 1, 5);
        scene.world().showSection(util.select().fromTo(1, 0, 1, 5, 0, 5), Direction.UP);
        scene.idle(10);
        final ElementLink<WorldSectionElement> link1 = scene.world().showIndependentSection(util.select().position(3, 3, 3), Direction.DOWN);
        scene.world().moveSection(link1, new Vec3(0, -2, 0), 0);
        scene.idle(20);
        scene.overlay().showText(60)
                .pointAt(util.vector().topOf(3, 1, 3))
                .attachKeyFrame()
                .placeNearTarget()
                .text("Levitite Blend can be crystallized into Levitite");
        scene.idle(80);
        scene.overlay().showText(50)
                .pointAt(util.vector().topOf(3, 1, 3))
                .attachKeyFrame()
                .placeNearTarget()
                .text("This process requires heat");
        scene.idle(20);
        scene.overlay().showControls(util.vector().topOf(3, 1, 3), Pointing.DOWN, 20).withItem(new ItemStack(Items.FLINT_AND_STEEL));
        scene.idle(5);
        scene.effects().emitParticles(new Vec3(3.5, 1.5, 3.5), withinBlockSpace(ParticleTypes.SMOKE, 1.5f), 15, 2);
        scene.idle(50);
        scene.world().replaceBlocks(util.select().position(3, 3, 3), AeroBlocks.LEVITITE.getDefaultState(), false);
        scene.effects().emitParticles(new Vec3(3.5, 1.5, 3.5), withinBlockSpace(ParticleTypes.FLAME, 1.5f), 15, 2);
        scene.idle(25);

        scene.overlay().showText(60)
                .pointAt(util.vector().centerOf(3, 1, 3))
                .attachKeyFrame()
                .placeNearTarget()
                .colored(PonderPalette.RED)
                .text("Once catalyzed, the Levitite cannot be recollected");

        scene.idle(20);

        for(int i = 0; i < 4; ++i) {
            scene.world().incrementBlockBreakingProgress(new BlockPos(3,3,3));
            scene.world().incrementBlockBreakingProgress(new BlockPos(3,3,3));
            scene.idle(5);
        }

        scene.world().setBlock(new BlockPos(3,3,3), Blocks.AIR.defaultBlockState(), false);
        scene.addInstruction(new OffsetBreakParticlesInstruction(AABB.unitCubeFromLowerCorner(new Vec3(3, 1, 3)), AeroBlocks.LEVITITE.getDefaultState()));

        scene.idle(50);

        final ElementLink<WorldSectionElement>[] link2 = new ElementLink[3];

        for (int i = 0; i < 3; i++) {
            link2[i] = scene.world().showIndependentSection(util.select().position(5 - 2 * i, 2, 3), Direction.DOWN);
            scene.world().moveSection(link2[i], new Vec3(0, -1, 0), 0);
            scene.idle(5);
        }

        scene.idle(5);
        final ElementLink<WorldSectionElement>[] link3 = new ElementLink[3];
        Selection[] selections = {util.select().fromTo(1, 3, 3,1, 4, 4),util.select().position(4, 3, 4),util.select().position(5, 3, 4)};
        for (int i = 0; i < 3; i++) {
            link3[i] = scene.world().showIndependentSection(selections[i], Direction.DOWN);
            scene.world().moveSection(link3[i], new Vec3((i == 1 ? -1 : 0) + (1-i)*4, -2, 0), 0);
            scene.idle(5);
        }
        scene.idle(10);
        scene.overlay().showText(70)
                .pointAt(util.vector().topOf(3, 1, 4))
                .attachKeyFrame()
                .placeNearTarget()
                .text("Nearby heat sources can also start the crystallization process");
        for (int i = 0; i < 3; i++) {
            scene.effects().emitParticles(new Vec3(5.5 - i * 2.0, 1.5, 3.5), withinBlockSpace(ParticleTypes.SMOKE, 1.5f), 15, 2);
            scene.idle(5);
        }
        scene.idle(60);
        for (int i = 0; i < 3; i++) {
            scene.effects().emitParticles(new Vec3(5.5 - i * 2.0, 1.5, 3.5), withinBlockSpace(ParticleTypes.FLAME, 1.5f), 15, 2);
            scene.world().replaceBlocks(util.select().position(5 - i * 2, 2, 3), AeroBlocks.LEVITITE.getDefaultState(), false);
            scene.idle(5);
        }
        scene.idle(20);
        for (int i = 0; i < 3; i++) {
            scene.world().hideIndependentSection(link2[i], Direction.UP);
            scene.idle(2);
        }
        for (int i = 0; i < 3; i++) {
            scene.world().hideIndependentSection(link3[i], Direction.UP);
            scene.idle(2);
        }
        scene.idle(20);

        final Selection pulley = util.select().fromTo(2, 2, 6, 3, 5, 6).add(util.select().fromTo(3, 5, 5, 3, 5, 6));

        final ElementLink<WorldSectionElement> pulleySection = scene.world().showIndependentSection(pulley, Direction.DOWN);
        scene.world().moveSection(pulleySection,new Vec3(0,-2,0),0);
        scene.idle(15);
        for (int i = 0; i < 7; i++) {

            Selection sel = null;
            for (int x = 0; x < 5; x++) {
                final int z = 4 - i + Math.abs(x - 2);
                if (z >= 0 && z < 5) {
                    sel = addSelection(sel,util.select().position(x+1, 1, z+1));
                }
            }
            if (sel != null)
                scene.world().showSection(sel, Direction.DOWN);

            scene.idle(4);
        }
        scene.idle(3);
        scene.world().hideIndependentSection(pulleySection, Direction.UP);

        scene.idle(20);
        scene.overlay().showControls(util.vector().topOf(3, 1, 3), Pointing.DOWN, 20).withItem(new ItemStack(Items.FLINT_AND_STEEL));
        scene.addKeyframe();
        scene.idle(5);

        animateLeviblendSpread(scene,util,0, new BlockPos(3,1,3),2,5,k -> {
            if (k == 3) {
                scene.overlay().showText(60)
                        .pointAt(util.vector().topOf(3, 1, 3))
                        .placeNearTarget()
                        .text("After the reaction is started, the crystal will spread throughout the entire fluid");
            }
        });

        boolean pass;

        try {
            pass = AeroConfig.server().blocks.breakBlocksOnCrystallize.get();
        } catch (final Exception ignored) {
            pass = true;
        }

        if(pass)
        {

            //long clayIndex = 0b0100110_1011001_1000010_0101101_1000001_1011101_0100010L;
            long clayIndex = 0b0100010_1011101_1000001_1011010_0100001_1001101_0110010L;
            Selection claySelection = null;
            Selection levititeSelection = null;
            long x = 1;
            for (int j = 0; j < 7; j++) {
                for (int i = 0; i < 7; i++) {
                    if((x & clayIndex) > 0)
                    {
                        claySelection = addSelection(claySelection,util.select().position(i,1,j));
                    }else if(i > 0 && j > 0 && i < 6 && j < 6)
                    {
                        levititeSelection = addSelection(levititeSelection,util.select().position(i,1,j));
                    }
                    x <<= 1;
                }
            }

            scene.idle(30);
            scene.world().hideSection(util.select().fromTo(1, 1, 1, 5, 1, 5), Direction.UP);
            scene.idle(20);

            scene.world().replaceBlocks(claySelection, Blocks.CLAY.defaultBlockState(), false);
            scene.world().replaceBlocks(levititeSelection, AeroLevititeService.INSTANCE.getFluid().defaultFluidState().createLegacyBlock(), false);

            scene.world().showSection(claySelection,Direction.DOWN);
            scene.idle(20);
            scene.world().showSection(levititeSelection,Direction.DOWN);
            scene.idle(30);
            scene.overlay().showText(60)
                    .pointAt(util.vector().topOf(1, 1, 3))
                    .attachKeyFrame()
                    .placeNearTarget()
                    .text("When certain blocks are used to cast Levitite...");
            scene.idle(70);
            scene.overlay().showControls(util.vector().topOf(2, 1, 3), Pointing.DOWN, 20).withItem(new ItemStack(Items.FLINT_AND_STEEL));
            scene.idle(5);
            animateLeviblendSpread(scene,util,clayIndex,new BlockPos(2,1,3),2,4,k -> {});
            scene.idle(10);
            scene.overlay().showText(60)
                    .pointAt(util.vector().topOf(1, 1, 3))
                    .placeNearTarget()
                    .text("...they will be destroyed during the crystallization process");
        }

    }
    static void animateLeviblendSpread(final SceneBuilder scene, final SceneBuildingUtil util, long clayIndex, BlockPos startPos, int minDelay, int maxDelay, Consumer<Integer> stepEvent)
    {
        final int[] arr1 = new int[25];
        final int[] arr2 = new int[25];


        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                final int i = x + 5 * y;
                final int i2 = (1+x + (y+1)*7);
                long n = 1L << i2;
                if((n & clayIndex) > 0)
                {
                    arr1[i] = -2;
                    arr2[i] = -2;
                }
            }
        }

        arr1[startPos.getX() + startPos.getZ()*5-6] = 4;
        scene.effects().emitParticles(new Vec3(0.5 + startPos.getX(), 1.5, 0.5 + startPos.getZ()), withinBlockSpace(ParticleTypes.SMOKE, 1.5f), 10, 2);

        for (int k = 0; k < 20; k++) {

            final int[] source = (k % 2) == 0 ? arr1 : arr2;
            final int[] target = (k % 2) == 0 ? arr2 : arr1;

            for (int x = 0; x < 5; x++) {
                for (int y = 0; y < 5; y++) {
                    final int i = x + 5 * y;
                    if (source[i] > 0) {
                        source[i]--;
                        if(source[i] == 0) {
                            source[i] = -1;
                            scene.world().replaceBlocks(util.select().position(x + 1, 1, y + 1), AeroBlocks.LEVITITE.getDefaultState(), false);
                            scene.effects().emitParticles(new Vec3(1.5 + x, 1.5, 1.5 + y), withinBlockSpace(ParticleTypes.FLAME, 1.5f), 10, 2);
                        }
                    }
                }
            }
            boolean done = true;
            for (int x = 0; x < 5; x++) {
                for (int y = 0; y < 5; y++) {
                    final int i = x + 5 * y;
                    target[i] = source[i];
                    if(source[i] >= 0)
                        done = false;
                    if (source[i] == 0) {
                        if (x > 0 && source[x - 1 + 5 * y] == -1
                                || x < 4 && source[x + 1 + 5 * y] == -1
                                || y > 0 && source[x + 5 * y - 5] == -1
                                || y < 4 && source[x + 5 * y + 5] == -1) {
                            target[i] = RANDOM.nextInt(minDelay, maxDelay);
                            scene.effects().emitParticles(new Vec3(1.5 + x, 1.5, 1.5 + y), withinBlockSpace(ParticleTypes.SMOKE, 1.5f), 10, 1);
                        }
                    }
                }
            }

            long bit = 1;
            for (int y = 0; y < 7; y++) {
                for (int x = 0; x < 7; x++) {
                    if((bit & clayIndex) > 0) {
                        boolean remove = true;
                        for (int i = 0; i < 4; i++) {
                            Vec3i dir = Direction.from2DDataValue(i).getUnitVec3i();
                            int x2 = x + dir.getX() - 1;
                            int y2 = y + dir.getZ() - 1;
                            if (x2 >= 0 && y2 >= 0 && x2 < 5 && y2 < 5 && source[x2 + y2 * 5] >= 0) {
                                remove = false;
                                break;
                            }
                        }
                        if(remove)
                        {
                            scene.world().destroyBlock(new BlockPos(x, 1, y));
                            clayIndex &= ~bit;
                        }
                    }
                    bit <<= 1;
                }
            }
            if(done)
                return;
            scene.idle(6);

            stepEvent.accept(k);
        }
    }
    static Selection addSelection(Selection source,Selection a)
    {
        if(source == null)
            return a;
        return source.add(a);
    }

    public static <T extends ParticleOptions> ParticleEmitter withinBlockSpace(final T data, final float range) {
        return (w, x, y, z) -> w.addParticle(data,
                Math.floor(x) + 0.5 + (RANDOM.nextFloat() - 0.5) * range,
                Math.floor(y) + 0.5 + (RANDOM.nextFloat() - 0.5) * range,
                Math.floor(z) + 0.5 + (RANDOM.nextFloat() - 0.5) * range,
                0, 0, 0);
    }

    public static void levitite(final SceneBuilder builder, SceneBuildingUtil util,boolean watermelon) {

        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final CreateSceneBuilder.WorldInstructions world = scene.world();
        final CreateSceneBuilder.SpecialInstructions special = scene.special();
        final OverlayInstructions overlay = scene.overlay();
        final SelectionUtil select = util.select();
        final VectorUtil vector = util.vector();

        scene.title("levitite", "Levitating contraptions with Levitite");
        scene.configureBasePlate(0, 0, 9);
        scene.setSceneOffsetY(-1);
        scene.scaleSceneView(0.8f);
        ElementLink<WorldSectionElement> ground = world.showIndependentSection(select.fromTo(0,0,0,8,0,8), Direction.UP);
        scene.idle(10);
        world.multiplyKineticSpeed(util.select().everywhere(),1);
        BlockPos propellerPos = new BlockPos(2, 3, 4);
        BlockPos gearshiftPos = new BlockPos(3, 3, 4);
        Selection propeller = select.fromTo(1, 2, 3, 1, 4, 5);
        //BlockPos assemblerPos = new BlockPos(7, 3, 4);
        scene.addInstruction(new PullTheAssemblerKronkInstruction(util.grid().at(7, 3, 4), true, true));

        ElementLink<WorldSectionElement> airship = world.showIndependentSection(select.fromTo(2, 2, 2, 7, 2, 6), Direction.DOWN);
        ElementLink<WorldSectionElement> stirlingSection = null;
        scene.idle(5);
        for (int i = 0; i < 5; i++) {
            int k = i>1?1:0;
            if(i == 2)
                stirlingSection = world.showIndependentSection(select.position(propellerPos.offset(2,0,0)),Direction.DOWN);
            else
                world.showSectionAndMerge(select.fromTo(propellerPos.offset(5-i-k,0,0),propellerPos.offset(5-i-k,i==3?1:0,0)),Direction.DOWN,airship);
            scene.idle(2);
        }
        ElementLink<WorldSectionElement> propellerLink = world.showIndependentSection(propeller, Direction.EAST);
        scene.idle(4);
        ElementLink<WorldSectionElement> stairs = world.showIndependentSection(
                util.select().position(propellerPos.offset(0,1,2))
                        .add(util.select().position(propellerPos.offset(4,1,2)))
                        .add(util.select().position(propellerPos.offset(0,1,-2)))
                        .add(util.select().position(propellerPos.offset(4,1,-2))),
                Direction.DOWN);
        world.moveSection(stairs,new Vec3(0,-1,0),0);
        scene.idle(2);
        world.showSectionAndMerge(
                util.select().fromTo(propellerPos.offset(1,0,2),propellerPos.offset(3,0,2))
                        .add(util.select().fromTo(propellerPos.offset(1,0,-2),propellerPos.offset(3,0,-2)))
                ,Direction.DOWN,airship);
        scene.idle(10);
        BlockState state = watermelon ? AeroBlocks.PEARLESCENT_LEVITITE.getDefaultState():AeroBlocks.LEVITITE.getDefaultState();
        SimpleParticleType particle = watermelon ? ParticleTypes.SOUL_FIRE_FLAME:ParticleTypes.FLAME;
        for (int i = 0; i < 3; i++) {
            BlockPos p = propellerPos.offset(3-i,0,2);
            world.replaceBlocks(util.select().position(p),state , false);
            scene.effects().emitParticles(p.getCenter(), withinBlockSpace(particle, 1.5f), 10, 2);
            p = propellerPos.offset(3-i,0,-2);
            world.replaceBlocks(util.select().position(p), state, false);
            scene.effects().emitParticles(p.getCenter(), withinBlockSpace(particle, 1.5f), 10, 2);
            scene.idle(5);
        }

        overlay.showText(50)
                .pointAt(util.vector().topOf(propellerPos.offset(1,0,2)))
                .attachKeyFrame()
                .placeNearTarget()
                .text("When enough Levitite is attached to a simulated contraption...");

        scene.idle(50);
        float revolutions = 0.75f;
        int duration = (int) (40 * revolutions);
        //ElementLink<WorldSectionElement>[] links = new ElementLink[]{};
        List<ElementLink<WorldSectionElement>> links = new ArrayList<>();
        links.add(airship);
        links.add(propellerLink);
        links.add(stairs);
        links.add(stirlingSection);
        PropellerRotateInstruction propellerRotate = new PropellerRotateInstruction(propellerPos,propellerLink,Direction.WEST,-32,4);
        scene.addInstruction(propellerRotate);
        scene.addInstruction(new ChangePropellerRotateInstruction.SetParticles(propellerRotate,airship,2,-5f,1.5f,false));
        //world.rotateBearing(propellerPos,-360*revolutions,duration);
        //world.rotateSection(propellerLink,-360*revolutions,0,0,duration);
        //scene.addInstruction(new PropellerParticleSpawningInstruction(propellerLink,propellerPos.west(),Direction.WEST,duration,2,3.2f,1.5f));
        for (ElementLink<WorldSectionElement> link : links) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link,new Vec3(1.5,0,0),duration, SmoothMovementUtils.quadraticRise()));
        }

        scene.idle(20);
        duration -= 20;

        overlay.showText(40)
                .pointAt(util.vector().topOf(propellerPos.offset(3,0,2)))
                .attachKeyFrame()
                .placeNearTarget()
                .text("...it will keep the contraption afloat");

        scene.idle(duration);
        world.multiplyKineticSpeed(util.select().everywhere(),-1);
        //world.toggleRedstonePower(util.select().fromTo(gearshiftPos,gearshiftPos.above()));
        revolutions *= 2;
        duration = (int) (40 * revolutions);
        //world.rotateBearing(propellerPos,360*revolutions,duration);
        //world.rotateSection(propellerLink,360*revolutions,0,0,duration);
        scene.addInstruction(new ChangePropellerRotateInstruction.SetRotationRate(propellerRotate,32));
        //scene.addInstruction(new PropellerParticleSpawningInstruction(propellerLink,propellerPos.west(),Direction.EAST,duration,2,3.2f,1.5f));
        for (ElementLink<WorldSectionElement> link : links) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link,new Vec3(1.5,0,0),duration, SmoothMovementUtils.quadraticJump()));
        }
        scene.idle(duration);
        world.multiplyKineticSpeed(util.select().everywhere(),-1);

        revolutions/=2;
        duration = (int) (40 * revolutions);
        //world.rotateBearing(propellerPos,-360*revolutions,duration);
        //world.rotateSection(propellerLink,-360*revolutions,0,0,duration);
        //scene.addInstruction(new PropellerParticleSpawningInstruction(propellerLink,propellerPos.west(),Direction.WEST,duration,2,3.2f,1.5f));
        //scene.addInstruction(new ChangePropellerRotateInstruction.SetRotationRate(propellerRotate,-32));
        scene.addInstruction(new ChangePropellerRotateInstruction.StopRotation(propellerRotate,20));
        for (ElementLink<WorldSectionElement> link : links) {
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link,new Vec3(-1.5,0,0),duration, SmoothMovementUtils.quadraticRiseDual()));
        }
        scene.idle(duration);
        world.toggleRedstonePower(util.select().fromTo(gearshiftPos,gearshiftPos.above()));


        scene.idle(5);
        world.hideIndependentSection(stairs,Direction.UP);
        scene.idle(16);
        Selection levititeCorners = util.select().position(propellerPos.offset(0,0,2))
                .add(util.select().position(propellerPos.offset(4,0,2)))
                .add(util.select().position(propellerPos.offset(0,0,-2)))
                .add(util.select().position(propellerPos.offset(4,0,-2)));

        if(watermelon)
            world.replaceBlocks(levititeCorners,AeroBlocks.PEARLESCENT_LEVITITE.getDefaultState(),false);

        world.showSectionAndMerge(levititeCorners,Direction.DOWN,airship);

        scene.idle(15);

        overlay.showText(60)
                .pointAt(util.vector().topOf(propellerPos.offset(1,0,2)))
                .attachKeyFrame()
                .placeNearTarget()
                .text("Simulated Contraptions cannot gain altitude using Levitite alone");
        scene.idle(70);

        world.moveSection(stirlingSection,new Vec3(1,0,0),10);
        scene.idle(20);
        Selection minipropSelection = select.fromTo(propellerPos.offset(2,1,0),propellerPos.offset(2,2,0));
        ElementLink<WorldSectionElement> miniprop = world.showIndependentSection(minipropSelection,Direction.DOWN);
        links.add(miniprop);
        world.moveSection(miniprop,new Vec3(0,-1,0),0);
        world.setKineticSpeed(minipropSelection,-32);
        scene.idle(20);
        scene.addInstruction(new PropellerParticleSpawningInstruction(miniprop,propellerPos.offset(2,2,0),Direction.DOWN,25,2,3.0f,0.7f));
        for (ElementLink<WorldSectionElement> link : links)
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link,new Vec3(0,0.5,0),25,SmoothMovementUtils.quadraticRise()));
        scene.idle(25);
        overlay.showText(60)
                .pointAt(util.vector().centerOf(propellerPos.offset(2,2,0)))
                .attachKeyFrame()
                .placeNearTarget()
                .text("Instead, additional forces are required");
        world.multiplyKineticSpeed(select.everywhere(),-1);
        scene.addInstruction(new PropellerParticleSpawningInstruction(miniprop,propellerPos.offset(2,2,0),Direction.UP,50,1,3.0f,0.7f));
        for (ElementLink<WorldSectionElement> link : links)
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link,new Vec3(0,0.5,0),50,SmoothMovementUtils.quadraticJump()));
        scene.idle(50);
        world.multiplyKineticSpeed(select.everywhere(),-1);
        scene.addInstruction(new PropellerParticleSpawningInstruction(miniprop,propellerPos.offset(2,2,0),Direction.DOWN,25,1,3.0f,0.7f));
        for (ElementLink<WorldSectionElement> link : links)
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link,new Vec3(0,-0.5,0),25,SmoothMovementUtils.quadraticRiseDual()));
        scene.idle(30);
        world.hideIndependentSection(miniprop,Direction.UP);
        scene.idle(15);
        world.moveSection(stirlingSection,new Vec3(-1,0,0),10);
        scene.idle(20);
        scene.addInstruction(new CustomToggleBaseShadowInstruction());
        world.toggleRedstonePower(util.select().fromTo(gearshiftPos,gearshiftPos.above()));
        propellerRotate = new PropellerRotateInstruction(propellerPos,propellerLink,Direction.WEST,-32,4);
        scene.addInstruction(propellerRotate);
        scene.addInstruction(new ChangePropellerRotateInstruction.SetParticles(propellerRotate,airship,2,-5f,1.5f,false));
        ElementLink<WorldSectionElement> ground2 = world.showIndependentSection(util.select().fromTo(9,0,2,39,1,6), Direction.WEST);
        ElementLink<WorldSectionElement>[] grounds = new ElementLink[]{ground,ground2};
        for (ElementLink<WorldSectionElement> link : grounds)
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link,new Vec3(-0.5,0,0),20,SmoothMovementUtils.quadraticRise()));


        scene.idle(20);
        float movementDistance = 6;
        int movementDuration = (int)(20*movementDistance);

        for (ElementLink<WorldSectionElement> link : grounds)
            world.moveSection(link,new Vec3(-movementDistance,0,0),movementDuration);

        scene.idle(10);
        movementDuration-=10;
        AABB aabb1 = new AABB(propellerPos.offset(0,0,2)).expandTowards(4,0,0);
        AABB aabb2 = new AABB(propellerPos.offset(0,0,-2)).expandTowards(4,0,0);
        overlay.chaseBoundingBoxOutline(PonderPalette.RED,aabb1,aabb1.contract(-5,0,0),1);
        overlay.chaseBoundingBoxOutline(PonderPalette.RED,aabb1,aabb1,100);
        overlay.chaseBoundingBoxOutline(PonderPalette.RED,aabb2,aabb2.contract(-5,0,0),1);
        overlay.chaseBoundingBoxOutline(PonderPalette.RED,aabb2,aabb2,100);
        scene.idle(10);
        movementDuration-=10;


        scene.addInstruction(new AirflowAABBInstruction(PonderPalette.RED,aabb1,90,Direction.WEST,1f,1,false,false));
        scene.addInstruction(new AirflowAABBInstruction(PonderPalette.RED,aabb2,90,Direction.WEST,1f,1,false,false));
        scene.idle(15);
        movementDuration-=15;
        overlay.showText(60)
                .pointAt(util.vector().topOf(propellerPos.offset(1,0,2)))
                .attachKeyFrame()
                .placeNearTarget()
                .text("At low movement speeds, Levitite significantly resists motion");
        scene.idle(movementDuration-4);

        world.hideIndependentSection(ground,null);
        overlay.showControls(
                util.vector().topOf(gearshiftPos.east()), Pointing.DOWN,
                10).withItem(new ItemStack(AllItems.BLAZE_CAKE.asItem()));
        scene.idle(4);
        world.multiplyKineticSpeed(util.select().everywhere(),2);

        scene.addInstruction(new ChangePropellerRotateInstruction.SetRotationRate(propellerRotate,-64));
        for (ElementLink<WorldSectionElement> link : grounds)
            scene.addInstruction(CustomAnimateWorldSectionInstruction.move(link,new Vec3(-2,0,0),40,t->SmoothMovementUtils.quadraticRise().apply(t)+t));
        scene.idle(20);

        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(ground2,new Vec3(-0.5,0,0),20,SmoothMovementUtils.quadraticRise()));
        scene.idle(20);

        movementDistance = 24;
        movementDuration = (int)(movementDistance*5);

        world.moveSection(ground2,new Vec3(-movementDistance,0,0),movementDuration);

        scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT,aabb1,aabb1.contract(-5,0,0),1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT,aabb1,aabb1,100);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT,aabb2,aabb2.contract(-5,0,0),1);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT,aabb2,aabb2,100);
        scene.idle(10);
        movementDuration-=10;
        scene.addInstruction(new AirflowAABBInstruction(PonderPalette.INPUT,aabb1,90,Direction.WEST,4f,3,false,false));
        scene.addInstruction(new AirflowAABBInstruction(PonderPalette.INPUT,aabb2,90,Direction.WEST,4f,3,false,false));
        scene.idle(15);
        movementDuration-=15;

        scene.overlay().showText(60)
                .pointAt(util.vector().topOf(propellerPos.offset(1,0,2)))
                .attachKeyFrame()
                .placeNearTarget()
                .text("This resistance drops at higher movement speeds");

        scene.idle(movementDuration);
        world.multiplyKineticSpeed(util.select().everywhere(),0);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(ground2,new Vec3(-5,0,0),50,SmoothMovementUtils.quadraticRiseDual()));
        ElementLink<WorldSectionElement> ground3 = world.showIndependentSection(util.select().fromTo(0,0,0,8,0,8), Direction.WEST);
        world.moveSection(ground3,new Vec3(5,0,0),0);
        scene.addInstruction(CustomAnimateWorldSectionInstruction.move(ground3,new Vec3(-5,0,0),50,SmoothMovementUtils.quadraticRiseDual()));
        scene.addInstruction(new ChangePropellerRotateInstruction.StopRotation(propellerRotate,30));
        scene.idle(20);
        world.hideIndependentSection(ground2,null);
        scene.markAsFinished();
        scene.idle(30);
        scene.addInstruction(new CustomToggleBaseShadowInstruction());
    }
}
