package dev.simulated_team.simulated.compat.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public abstract class EntityRenderer<T extends Entity>
        extends net.minecraft.client.renderer.entity.EntityRenderer<T, EntityRenderer.RenderState<T>> {

    protected EntityRenderer(final EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public RenderState<T> createRenderState() {
        return new RenderState<>();
    }

    @Override
    public void extractRenderState(final T entity, final RenderState<T> state, final float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.entity = entity;
        state.partialTicks = partialTicks;
        state.yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
    }

    @Override
    public Vec3 getRenderOffset(final RenderState<T> state) {
        return state.entity == null ? Vec3.ZERO : getRenderOffset(state.entity, state.partialTicks);
    }

    public Vec3 getRenderOffset(final T entity, final float partialTicks) {
        return Vec3.ZERO;
    }

    @Override
    public void submit(final RenderState<T> state, final PoseStack poseStack, final SubmitNodeCollector queue,
                       final CameraRenderState cameraState) {
        super.submit(state, poseStack, queue, cameraState);
        if (state.entity == null || state.entity.isRemoved()) {
            return;
        }

        dev.simulated_team.simulated.compat.create.RenderBridge.submit(poseStack, queue,
                (legacyPose, buffers) -> render(state.entity, state.yaw, state.partialTicks, legacyPose, buffers, state.lightCoords));
    }

    public void render(final T entity, final float yaw, final float partialTicks, final PoseStack poseStack,
                       final MultiBufferSource bufferSource, final int light) {
    }

    public abstract ResourceLocation getTextureLocation(T entity);

    @Override
    public boolean shouldRender(final T entity, final Frustum frustum, final double x, final double y, final double z) {
        return super.shouldRender(entity, frustum, x, y, z);
    }

    public static class RenderState<T extends Entity> extends EntityRenderState {
        private T entity;
        private float partialTicks;
        private float yaw;
    }
}
