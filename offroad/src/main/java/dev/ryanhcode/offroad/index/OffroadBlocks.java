package dev.ryanhcode.offroad.index;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllMovementBehaviours;
import com.zurrtum.create.AllTags;
import com.zurrtum.create.content.contraptions.actors.roller.RollerBlockItem;
import com.zurrtum.create.foundation.data.BlockStateGen;
import com.zurrtum.create.foundation.data.SharedProperties;
import dev.simulated_team.simulated.data.SimBlockStateGen;
import dev.simulated_team.simulated.index.SimItems;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.config.server.OffroadStress;
import dev.ryanhcode.offroad.content.blocks.borehead_bearing.BoreheadBearingBlock;
import dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel.RockCuttingWheelActor;
import dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel.RockCuttingWheelBlock;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlock;
import dev.ryanhcode.offroad.content.components.TireLike;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static com.zurrtum.create.foundation.data.ModelGen.customItemModel;
import static com.zurrtum.create.foundation.data.TagGen.axeOrPickaxe;
import static com.zurrtum.create.foundation.data.TagGen.pickaxeOnly;

public class OffroadBlocks {
    private static final SimulatedRegistrate REGISTRATE = Offroad.getRegistrate();
    public static final RockCuttingWheelActor ROCK_CUTTING_WHEEL_ACTOR = new RockCuttingWheelActor();

    public static final BlockEntry<BoreheadBearingBlock> BOREHEAD_BEARING_BLOCK =
            REGISTRATE.block("borehead_bearing", BoreheadBearingBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .requiresCorrectToolForDrops()
                            .noOcclusion())
                    .transform(pickaxeOnly())
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY)
                            .noOcclusion()
                            .isRedstoneConductor((state, level, pos) -> false))
                    .transform(pickaxeOnly())
                    .transform(OffroadStress.setImpact(8))
                    .addLayer(() -> RenderType::cutoutMipped)
                    .blockstate(BlockStateGen.directionalAxisBlockProvider())
                    .recipe((c, p) -> p.shaped(RecipeCategory.MISC, c.get(), 1)
                            .pattern("S")
                            .pattern("G")
                            .pattern("I")
                            .define('S', ItemTags.WOODEN_SLABS)
                            .define('G', AllBlocks.GEARBOX)
                            .define('I', AllBlocks.INDUSTRIAL_IRON_BLOCK)
                            .unlockedBy("has_ingredient", RegistrateRecipeProvider.has(AllBlocks.INDUSTRIAL_IRON_BLOCK))
                            .save(p))
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<RockCuttingWheelBlock> ROCK_CUTTER_BLOCK =
            REGISTRATE.block("rockcutting_wheel", RockCuttingWheelBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .requiresCorrectToolForDrops()
                            .noOcclusion())
                    .transform(pickaxeOnly())
                    .onRegister(block -> AllMovementBehaviours.register(ROCK_CUTTING_WHEEL_ACTOR, block))
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .addLayer(() -> RenderType::cutoutMipped)
                    .blockstate(SimBlockStateGen::directionalAxisBlock)
                    .recipe((c, p) -> p.shaped(RecipeCategory.MISC, c.get(), 1)
                            .pattern("C")
                            .pattern("G")
                            .pattern("S")
                            .define('C', AllBlocks.CRUSHING_WHEEL)
                            .define('G', AllBlocks.INDUSTRIAL_IRON_BLOCK)
                            .define('S', AllItems.IRON_SHEET)
                            .unlockedBy("has_ingredient", RegistrateRecipeProvider.has(AllBlocks.INDUSTRIAL_IRON_BLOCK))
                            .save(p))
                    .item()
                    .properties(x -> x.component(OffroadDataComponents.TIRE, TireLike.ROCKCUTTING_WHEEL))
                    .transform(customItemModel())
                    .lang("Rock Cutting Wheel")
                    .register();

    public static final BlockEntry<WheelMountBlock> WHEEL_MOUNT =
            REGISTRATE.block("wheel_mount", WheelMountBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY)
                            .noOcclusion()
                            .isRedstoneConductor((state, level, pos) -> false))
                    .transform(axeOrPickaxe())
                    .addLayer(() -> RenderType::cutoutMipped)
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .blockstate(BlockStateGen.horizontalBlockProvider(true))
                    .transform(OffroadStress.setImpact(16.0))
                    .item(RollerBlockItem::new)
                    .transform(customItemModel())
                    .recipe((c, p) -> p.shaped(RecipeCategory.MISC, c.get(), 1)
                            .pattern("C")
                            .pattern("S")
                            .pattern("P")
                            .define('C', AllBlocks.ANDESITE_CASING)
                            .define('S', SimItems.SPRING.asItem())
                            .define('P', AllItems.IRON_SHEET.asItem())
                            .unlockedBy("has_ingredient", RegistrateRecipeProvider.has(AllBlocks.ANDESITE_CASING))
                            .save(p))
                    .register();

    public static void init() {
    }
}
