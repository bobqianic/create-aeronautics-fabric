package dev.simulated_team.simulated.content.blocks.steering_wheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.waterwheel.WaterWheelRenderer;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import dev.simulated_team.simulated.index.SimPartialModels;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperBufferFactory;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class SteeringWheelRenderer extends KineticBlockEntityRenderer<SteeringWheelBlockEntity> {
    public static final SuperByteBufferCache.Compartment<ModelKey> STEERING_WHEEL = new SuperByteBufferCache.Compartment<>();


    public SteeringWheelRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final SteeringWheelBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer,
                              final int light, final int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel()) && !isRenderingInSubLevel()) {
            return;
        }

        final boolean floor = be.getBlockState().getValue(SteeringWheelBlock.ON_FLOOR);
        final Direction facing = be.getBlockState().getValue(SteeringWheelBlock.FACING);

        if (be.shouldRenderShaft()) {
            final BlockState state = this.getRenderedBlockState(be);
            final RenderType type = RenderType.solid();
            renderRotatingBuffer(be, CachedBuffers.partialFacing(
                    AllPartialModels.SHAFT_HALF,
                    be.getBlockState(),
                    floor ? Direction.DOWN : Direction.UP
            ), ms, buffer.getBuffer(type), light);
        }

        final SuperByteBuffer model = this.getWheelModel(be);

        model.rotateCentered(facing.getRotation());
        if (floor) {
            model.translate(0, 6.5 / 16f, -5 / 16f);
        } else {
            model.translate(0, 6.5 / 16f, 5 / 16f);
        }
        model.rotateCentered(be.getRenderAngle(partialTicks), Direction.UP);

        model.light(light);
        model.color(Color.WHITE);
        model.renderInto(ms.last(), buffer.getBuffer(RenderType.solid()));
    }

    private SuperByteBuffer getWheelModel(final SteeringWheelBlockEntity be) {
        final ModelKey key = new ModelKey(be.material);
        return SuperByteBufferCache.getInstance().get(STEERING_WHEEL, key, () -> {
            final SimpleModelWrapper model = generateModel(SimPartialModels.STEERING_WHEEL.get(), be.material);
            return SuperBufferFactory.getInstance().createForBlock(model, Blocks.AIR.defaultBlockState(), new PoseStack());
        });
    }

    public static SimpleModelWrapper generateModel(final SimpleModelWrapper template, final BlockState planksBlockState) {
        final Block planksBlock = planksBlockState.getBlock();
        final ResourceLocation id = RegisteredObjectsHelper.getKeyOrThrow(planksBlock);
        final String wood = plankStateToWoodName(planksBlockState);

        if (wood == null)
            return BakedModelHelper.generateModel(template, sprite -> null);

        final Map<TextureAtlasSprite, TextureAtlasSprite> map = new Reference2ReferenceOpenHashMap<>();
        map.put(WaterWheelRenderer.OAK_PLANKS_TEMPLATE.get(), getSpriteOnSide(planksBlockState, Direction.UP));

        return BakedModelHelper.generateModel(template, map::get);
    }

    public record ModelKey(BlockState material) {
    }

    // todo tell create to make these public :p
    @Nullable
    private static String plankStateToWoodName(final BlockState planksBlockState) {
        final Block planksBlock = planksBlockState.getBlock();
        final ResourceLocation id = RegisteredObjectsHelper.getKeyOrThrow(planksBlock);
        final String path = id.getPath();

        if (path.endsWith("_planks")) // Covers most wood types
            return (path.startsWith("archwood") ? "blue_" : "") + path.substring(0, path.length() - 7);

        if (path.contains("wood/planks/")) // TerraFirmaCraft
            return path.substring(12);

        return null;
    }

    private static TextureAtlasSprite getSpriteOnSide(final BlockState state, final Direction side) {
        final BlockStateModel model = Minecraft.getInstance()
                .getBlockRenderer()
                .getBlockModel(state);
        if (model == null)
            return null;
        final RandomSource random = RandomSource.create();
        random.setSeed(42L);
        final List<BlockModelPart> parts = model.collectParts(random);
        for (final BlockModelPart part : parts) {
            final List<BakedQuad> quads = part.getQuads(side);
            if (!quads.isEmpty()) {
                return quads.getFirst().sprite();
            }
        }
        random.setSeed(42L);
        for (final BlockModelPart part : parts) {
            for (final BakedQuad quad : part.getQuads(null)) {
                if (quad.direction() == side) {
                    return quad.sprite();
                }
            }
        }
        return model.particleIcon();
    }
}
