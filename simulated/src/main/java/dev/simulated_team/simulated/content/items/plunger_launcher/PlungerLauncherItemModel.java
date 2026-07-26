package dev.simulated_team.simulated.content.items.plunger_launcher;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.client.model.BakedItemModelPart;
import dev.simulated_team.simulated.client.render.FirstPersonItemFocus;
import dev.simulated_team.simulated.content.entities.launched_plunger.LaunchedPlungerEntity;
import dev.simulated_team.simulated.mixin_interface.PlayerLaunchedPlungerExtension;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Set;
import java.util.function.Consumer;

public final class PlungerLauncherItemModel implements ItemModel, SpecialModelRenderer<PlungerLauncherItemModel.RenderData> {
    public static final ResourceLocation ID = Simulated.path("model/plunger_launcher");
    private static final ResourceLocation ITEM = Simulated.path("item/plunger_launcher/item");
    private static final ResourceLocation BODY = Simulated.path("item/plunger_launcher/plunger_tether");
    private static final ResourceLocation JOINT = Simulated.path("item/plunger_launcher/spool_joint");
    private static final ResourceLocation SPOOL = Simulated.path("item/plunger_launcher/tether_spool");

    private final BakedItemModelPart item;
    private final BakedItemModelPart body;
    private final BakedItemModelPart joint;
    private final BakedItemModelPart spool;

    private PlungerLauncherItemModel(
            final BakedItemModelPart item,
            final BakedItemModelPart body,
            final BakedItemModelPart joint,
            final BakedItemModelPart spool
    ) {
        this.item = item;
        this.body = body;
        this.joint = joint;
        this.spool = spool;
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
        final LocalPlayer player = minecraft.player;
        final DeltaTracker timer = minecraft.getDeltaTracker();
        final float partialTicks = timer.getGameTimeDeltaPartialTick(false);
        final ItemStackRenderState.FoilType foil = stack.hasFoil()
                ? ItemStackRenderState.FoilType.STANDARD
                : ItemStackRenderState.FoilType.NONE;
        final boolean captureBodyFocus = FirstPersonItemFocus.isLocalPlayerBodyRender(
                displayContext, owner, minecraft);
        final Consumer<PoseStack> commonTransform = matrices -> {
            matrices.scale(0.8f, 0.8f, 0.8f);
            matrices.translate(0, 0, 0.15f);
        };

        addLayer(state, displayContext, item, Sheets.cutoutBlockSheet(), foil,
                commonTransform, true, displayContext.firstPerson() || captureBodyFocus, partialTicks);

        boolean renderFirst = player == null;
        boolean renderSecond = player == null;
        float cooldown = 0;
        if (player != null) {
            cooldown = player.getCooldowns().getCooldownPercent(stack, partialTicks);
            final LaunchedPlungerEntity launchedPlunger = player instanceof PlayerLaunchedPlungerExtension extension
                    ? extension.simulated$getLaunchedPlunger()
                    : null;
            if (cooldown <= 0.6f || launchedPlunger != null && launchedPlunger.getOther() == null) {
                renderFirst = (launchedPlunger == null || launchedPlunger.isRemoved() || launchedPlunger.getOther() != null)
                        && cooldown <= 0.4f;
                renderSecond = true;
            }
        }

        if (renderFirst) {
            addPlunger(state, displayContext, commonTransform, true, cooldown);
        }
        if (renderSecond) {
            addPlunger(state, displayContext, commonTransform, false, cooldown);
        }
    }

    private void addPlunger(
            final ItemStackRenderState state,
            final ItemDisplayContext displayContext,
            final Consumer<PoseStack> commonTransform,
            final boolean first,
            final float cooldown
    ) {
        final Consumer<PoseStack> bodyTransform = matrices -> {
            commonTransform.accept(matrices);
            matrices.translate(2 / 16.0 * (first ? -1 : 1), -1 / 16.0, -5 / 16.0);

            if (cooldown > 0 && PlungerLauncherItem.reloadCooldown) {
                final float start = first ? 0.1f : 0.3f;
                final float end = first ? 0.4f : 0.6f;
                float slideIn = Mth.clamp(Mth.map(cooldown, start, end, 0, 1), 0, 1);
                slideIn = (float) Math.pow(slideIn, 3);
                matrices.translate(0, 0, -slideIn / 12.0);
            }
        };

        // Unlike the launcher base, these partials are authored around [0, 0, 0].
        addLayer(state, displayContext, body, Sheets.solidBlockSheet(),
                ItemStackRenderState.FoilType.NONE, bodyTransform, false, false, 0);
        addLayer(state, displayContext, joint, Sheets.solidBlockSheet(),
                ItemStackRenderState.FoilType.NONE, bodyTransform, false, false, 0);
        addLayer(state, displayContext, spool, Sheets.solidBlockSheet(),
                ItemStackRenderState.FoilType.NONE, matrices -> {
                    bodyTransform.accept(matrices);
                    matrices.translate(0, 0, 3 / 16.0);
                }, false, false, 0);
    }

    private void addLayer(
            final ItemStackRenderState state,
            final ItemDisplayContext displayContext,
            final BakedItemModelPart part,
            final RenderType renderType,
            final ItemStackRenderState.FoilType foil,
            final Consumer<PoseStack> transform,
            final boolean centerQuads,
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
                new RenderData(layer, renderType, foil, transform, centerQuads, captureFocus, partialTicks));
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
            matrices.pushPose();
            matrices.translate(2 / 16.0, -1 / 16.0, -4 / 16.0);
            if (displayContext.firstPerson()) {
                PlungerLauncherItemRenderer.captureFirstPersonFocus(
                        matrices, Minecraft.getInstance(), data.partialTicks());
            } else {
                PlungerLauncherItemRenderer.captureFirstPersonBodyFocus(matrices);
            }
            matrices.popPose();
        }
        matrices.pushPose();
        if (data.centerQuads()) {
            matrices.translate(-0.5, -0.5, -0.5);
        }
        queue.submitItem(
                matrices,
                displayContext,
                light,
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

    public record RenderData(
            LayerRenderState layer,
            RenderType renderType,
            ItemStackRenderState.FoilType foil,
            Consumer<PoseStack> transform,
            boolean centerQuads,
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
            resolver.markDependency(BODY);
            resolver.markDependency(JOINT);
            resolver.markDependency(SPOOL);
        }

        @Override
        public ItemModel bake(final ItemModel.BakingContext context) {
            final ModelBaker baker = context.blockModelBaker();
            return new PlungerLauncherItemModel(
                    BakedItemModelPart.bake(baker, ITEM),
                    BakedItemModelPart.bake(baker, BODY),
                    BakedItemModelPart.bake(baker, JOINT),
                    BakedItemModelPart.bake(baker, SPOOL)
            );
        }
    }
}
