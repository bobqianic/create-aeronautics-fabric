package dev.simulated_team.simulated.content.physics_staff;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.SimulatedClient;
import dev.simulated_team.simulated.client.model.BakedItemModelPart;
import dev.simulated_team.simulated.client.render.FirstPersonItemFocus;
import dev.simulated_team.simulated.index.SimRenderTypes;
import dev.simulated_team.simulated.util.SimDistUtil;
import dev.simulated_team.simulated.util.SimMathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class PhysicsStaffItemModel implements ItemModel, SpecialModelRenderer<PhysicsStaffItemModel.RenderData> {
    public static final ResourceLocation ID = Simulated.path("model/creative_physics_staff");
    private static final ResourceLocation ITEM = Simulated.path("item/creative_physics_staff/item");
    private static final ResourceLocation CORE = Simulated.path("item/creative_physics_staff/core");
    private static final ResourceLocation CORE_GLOW = Simulated.path("item/creative_physics_staff/core_glow");
    private static final ResourceLocation RING = Simulated.path("item/creative_physics_staff/ring");
    private static final ResourceLocation SIGMA = Simulated.path("item/creative_physics_staff/sigma");
    private static final ResourceLocation INNER_CUBE = Simulated.path("item/creative_physics_staff/inner_cube");
    private static final ResourceLocation OUTER_CUBE = Simulated.path("item/creative_physics_staff/outer_cube");

    private final BakedItemModelPart item;
    private final BakedItemModelPart core;
    private final BakedItemModelPart coreGlow;
    private final BakedItemModelPart ring;
    private final BakedItemModelPart sigma;
    private final BakedItemModelPart innerCube;
    private final BakedItemModelPart outerCube;

    private PhysicsStaffItemModel(
            final BakedItemModelPart item,
            final BakedItemModelPart core,
            final BakedItemModelPart coreGlow,
            final BakedItemModelPart ring,
            final BakedItemModelPart sigma,
            final BakedItemModelPart innerCube,
            final BakedItemModelPart outerCube
    ) {
        this.item = item;
        this.core = core;
        this.coreGlow = coreGlow;
        this.ring = ring;
        this.sigma = sigma;
        this.innerCube = innerCube;
        this.outerCube = outerCube;
    }

    @Override
    public void update(
            final ItemStackRenderState state,
            final ItemStack stack,
            final ItemModelResolver resolver,
            final ItemDisplayContext displayContext,
            @Nullable final ClientLevel level,
            @Nullable final ItemOwner owner,
            final int seed
    ) {
        state.appendModelIdentityElement(this);
        state.setAnimated();

        final Minecraft minecraft = Minecraft.getInstance();
        final PhysicsStaffClientHandler handler = SimulatedClient.PHYSICS_STAFF_CLIENT_HANDLER;
        final Player player = SimDistUtil.getClientPlayer();
        final float partialTicks = AnimationTickHolder.getPartialTicks();
        final StaffAnimation animation = animation(stack, displayContext, handler, player, partialTicks);
        final ItemStackRenderState.FoilType foil = stack.hasFoil()
                ? ItemStackRenderState.FoilType.STANDARD
                : ItemStackRenderState.FoilType.NONE;
        final boolean shadersActive = OptionalShaderMods.isShaderPackActive();
        final boolean captureBodyFocus = FirstPersonItemFocus.isLocalPlayerBodyRender(
                displayContext, owner, minecraft);
        final Consumer<PoseStack> commonTransform = matrices -> applyCommonTransform(
                matrices, displayContext, handler, player, minecraft, partialTicks, animation.tilt());

        addLayer(state, displayContext, item, Sheets.cutoutBlockSheet(), foil, commonTransform, -1, false, partialTicks);
        addLayer(state, displayContext, core, SimRenderTypes.itemGlowingSolid(shadersActive),
                ItemStackRenderState.FoilType.NONE, commonTransform, LightTexture.FULL_BRIGHT, false, partialTicks);
        addLayer(state, displayContext, coreGlow, SimRenderTypes.itemGlowingTranslucent(shadersActive),
                ItemStackRenderState.FoilType.NONE, commonTransform, LightTexture.FULL_BRIGHT, false, partialTicks);

        addLayer(state, displayContext, ring, Sheets.cutoutBlockSheet(), ItemStackRenderState.FoilType.NONE,
                matrices -> {
                    commonTransform.accept(matrices);
                    matrices.translate(0, 6.5 / 16.0, 0);
                }, -1, false, partialTicks);

        for (int side = 0; side < 2; side++) {
            final int currentSide = side;
            addLayer(state, displayContext, sigma, Sheets.cutoutBlockSheet(), ItemStackRenderState.FoilType.NONE,
                    matrices -> {
                        commonTransform.accept(matrices);
                        matrices.translate(0, 9 / 16.0, 0);
                        matrices.mulPose(Axis.YP.rotationDegrees(currentSide * 180));
                        matrices.translate(-3 / 16.0, 0, 0);
                        matrices.mulPose(Axis.ZP.rotationDegrees(animation.openAmount() * 20));
                    }, -1, false, partialTicks);
        }

        final Consumer<PoseStack> cubeTransform = matrices -> {
            commonTransform.accept(matrices);
            matrices.translate(0, 15 / 16.0, 0);
            if (displayContext.firstPerson()) {
                final PhysicsStaffClientHandler.ClientDragSession dragSession = handler.getDragSession();
                if (dragSession != null) {
                    handler.lastCubeOrientation.set(dragSession.dragOrientation());
                }

                final Matrix4f orientation = new Matrix4f(matrices.last().pose());
                orientation.m30(0).m31(0).m32(0);
                orientation.invert();
                orientation.rotate(handler.lastCubeOrientation);
                matrices.mulPose(orientation);
            }
            matrices.scale(animation.cubeScale(), animation.cubeScale(), animation.cubeScale());
        };

        addLayer(state, displayContext, innerCube, SimRenderTypes.itemGlowingSolid(shadersActive),
                ItemStackRenderState.FoilType.NONE, cubeTransform, LightTexture.FULL_BRIGHT,
                displayContext.firstPerson() || captureBodyFocus, partialTicks);
        addLayer(state, displayContext, outerCube, SimRenderTypes.itemGlowingTranslucent(shadersActive),
                ItemStackRenderState.FoilType.NONE, matrices -> {
                    cubeTransform.accept(matrices);
                    matrices.scale(1.2f, 1.2f, 1.2f);
                }, LightTexture.FULL_BRIGHT, false, partialTicks);
    }

    private StaffAnimation animation(
            final ItemStack stack,
            final ItemDisplayContext displayContext,
            final PhysicsStaffClientHandler handler,
            @Nullable final Player player,
            final float partialTicks
    ) {
        float openAmount = 0;
        float cubeScale = 0;
        final boolean held = displayContext.firstPerson()
                || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

        if (player != null && held) {
            if (player.getMainHandItem() == stack || player.getOffhandItem() == stack) {
                openAmount = Mth.lerp(partialTicks, handler.previousExtension, handler.extension);
                cubeScale = Mth.lerp(partialTicks, handler.previousCubeScale, handler.cubeScale);
            } else {
                for (final UUID playerId : handler.beams.keySet()) {
                    final Player otherPlayer = Minecraft.getInstance().level.getPlayerByUUID(playerId);
                    if (otherPlayer != null
                            && (otherPlayer.getMainHandItem() == stack || otherPlayer.getOffhandItem() == stack)) {
                        final PhysicsStaffClientHandler.PhysicsBeam beam = handler.beams.get(playerId);
                        openAmount = Mth.lerp(partialTicks, beam.previousExtension, beam.extension);
                        cubeScale = Mth.lerp(partialTicks, beam.previousCubeScale, beam.cubeScale);
                        break;
                    }
                }
            }
        }

        cubeScale = Mth.clamp(Mth.lerp(cubeScale, -0.05f, 1), 0, 1) * 0.8f;
        final float tilt = Mth.lerp(partialTicks, handler.previousTilt, handler.tilt);
        return new StaffAnimation(openAmount, cubeScale, tilt);
    }

    private void applyCommonTransform(
            final PoseStack matrices,
            final ItemDisplayContext displayContext,
            final PhysicsStaffClientHandler handler,
            @Nullable final Player player,
            final Minecraft minecraft,
            final float partialTicks,
            final float tilt
    ) {
        if (!displayContext.firstPerson()) {
            return;
        }

        final PhysicsStaffClientHandler.ClientDragSession dragSession = handler.getDragSession();
        final Quaternionf quaternion = new Quaternionf();
        if (dragSession != null && player != null) {
            final Quaternionf cameraRotation = minecraft.gameRenderer.getMainCamera().rotation();
            final Vector3d globalAnchor = ((ClientSubLevel) dragSession.dragSubLevel()).renderPose()
                    .transformPosition(new Vector3d(dragSession.dragLocalAnchor()));
            final Vector3d direction = globalAnchor
                    .sub(JOMLConversion.toJOML(player.getEyePosition(partialTicks)))
                    .normalize();
            cameraRotation.transformInverse(direction);

            final Quaternionf aim = SimMathUtils.getQuaternionfFromVectorRotation(
                    new Vector3d(0, 0, -1), direction);
            matrices.mulPose(quaternion.identity().rotateY(-Mth.HALF_PI));
            matrices.mulPose(aim.slerp(quaternion.identity(), 0.6f));
            matrices.mulPose(quaternion.identity().rotateY(Mth.HALF_PI));
        }

        final float handMultiplier = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -1 : 1;
        matrices.mulPose(quaternion.identity().rotateZ(
                (float) Math.toRadians((tilt * 0.5f + 0.5f) * -61) * handMultiplier));
    }

    private void addLayer(
            final ItemStackRenderState state,
            final ItemDisplayContext displayContext,
            final BakedItemModelPart part,
            final RenderType renderType,
            final ItemStackRenderState.FoilType foil,
            final Consumer<PoseStack> transform,
            final int lightOverride,
            final boolean captureFocus,
            final float partialTicks
    ) {
        final LayerRenderState layer = state.newLayer();
        layer.setRenderType(renderType);
        layer.setExtents(part.extents());
        item.properties().applyToLayer(layer, displayContext);
        layer.prepareQuadList().addAll(part.quads());
        layer.setFoilType(foil);
        layer.setupSpecialModel(this,
                new RenderData(layer, renderType, foil, transform, lightOverride, captureFocus, partialTicks));
    }

    @Override
    public void submit(
            final RenderData data,
            final ItemDisplayContext displayContext,
            final PoseStack matrices,
            final SubmitNodeCollector queue,
            final int light,
            final int overlay,
            final boolean glint,
            final int seed
    ) {
        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);
        data.transform().accept(matrices);
        if (data.captureFocus()) {
            if (displayContext.firstPerson()) {
                PhysicsStaffItemRenderer.captureFirstPersonFocus(
                        matrices, Minecraft.getInstance(), data.partialTicks());
            } else {
                PhysicsStaffItemRenderer.captureFirstPersonBodyFocus(matrices);
            }
        }
        matrices.pushPose();
        matrices.translate(-0.5, -0.5, -0.5);
        queue.submitItem(
                matrices,
                displayContext,
                data.lightOverride() < 0 ? light : data.lightOverride(),
                overlay,
                0,
                data.layer().prepareTintLayers(0),
                data.layer().prepareQuadList(),
                data.renderType(),
                data.foil()
        );
        matrices.popPose();
        matrices.popPose();
    }

    @Override
    public void getExtents(final Set<Vector3f> vertices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RenderData extractArgument(final ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    private record StaffAnimation(float openAmount, float cubeScale, float tilt) {
    }

    public record RenderData(
            LayerRenderState layer,
            RenderType renderType,
            ItemStackRenderState.FoilType foil,
            Consumer<PoseStack> transform,
            int lightOverride,
            boolean captureFocus,
            float partialTicks
    ) {
    }

    public static final class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(final Resolver resolver) {
            resolver.markDependency(ITEM);
            resolver.markDependency(CORE);
            resolver.markDependency(CORE_GLOW);
            resolver.markDependency(RING);
            resolver.markDependency(SIGMA);
            resolver.markDependency(INNER_CUBE);
            resolver.markDependency(OUTER_CUBE);
        }

        @Override
        public ItemModel bake(final ItemModel.BakingContext context) {
            final ModelBaker baker = context.blockModelBaker();
            return new PhysicsStaffItemModel(
                    BakedItemModelPart.bake(baker, ITEM),
                    BakedItemModelPart.bake(baker, CORE),
                    BakedItemModelPart.bake(baker, CORE_GLOW),
                    BakedItemModelPart.bake(baker, RING),
                    BakedItemModelPart.bake(baker, SIGMA),
                    BakedItemModelPart.bake(baker, INNER_CUBE),
                    BakedItemModelPart.bake(baker, OUTER_CUBE)
            );
        }
    }
}
