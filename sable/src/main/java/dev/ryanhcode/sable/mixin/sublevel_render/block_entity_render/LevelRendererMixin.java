package dev.ryanhcode.sable.mixin.sublevel_render.block_entity_render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.mixinterface.BlockEntityRenderDispatcherExtension;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderData;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.SortedSet;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    @Nullable
    private ClientLevel level;

    @Shadow
    @Final
    private BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    @Unique
    private final Map<BlockEntityRenderState, SableBlockEntityTransform> sable$blockEntityTransforms = new IdentityHashMap<>();
    @Unique
    private Quaternionf sable$cameraOrientation;

    @Inject(method = "extractVisibleBlockEntities", at = @At("RETURN"))
    private void sable$extractBlockEntities(final Camera camera, final float partialTick, final LevelRenderState levelRenderState, final CallbackInfo ci) {
        this.sable$blockEntityTransforms.clear();
        levelRenderState.blockEntityRenderStates.removeIf(state -> Sable.HELPER.getContainingClient(state.blockPos) != null);

        if (this.level == null) {
            return;
        }

        final ClientSubLevelContainer container = SubLevelContainer.getContainer(this.level);
        if (container == null) {
            return;
        }

        final Vec3 cameraPosition = camera.getPosition();
        final BlockEntityRenderDispatcherExtension dispatcherExtension = (BlockEntityRenderDispatcherExtension) this.blockEntityRenderDispatcher;

        for (final ClientSubLevel subLevel : container.getAllSubLevels()) {
            final SubLevelRenderData renderData = subLevel.getRenderData();
            if (renderData == null) {
                continue;
            }

            final Vector3dc rotationPoint = subLevel.renderPose(partialTick).rotationPoint();
            final Matrix4f transformation = renderData.getTransformation(cameraPosition.x, cameraPosition.y, cameraPosition.z);
            final Vector3f localCamera = transformation.invert(new Matrix4f()).transformPosition(new Vector3f());

            dispatcherExtension.sable$setCameraPosition(new Vec3(
                    localCamera.x + rotationPoint.x(),
                    localCamera.y + rotationPoint.y(),
                    localCamera.z + rotationPoint.z()
            ));

            try {
                for (final PlotChunkHolder holder : subLevel.getPlot().getLoadedChunks()) {
                    for (final BlockEntity blockEntity : holder.getChunk().getBlockEntities().values()) {
                        if (blockEntity.isRemoved()) {
                            continue;
                        }

                        final BlockPos blockPos = blockEntity.getBlockPos();
                        final CrumblingOverlay crumblingOverlay = this.sable$createCrumblingOverlay(blockPos, transformation, rotationPoint);
                        final BlockEntityRenderState renderState = this.blockEntityRenderDispatcher.tryExtractRenderState(blockEntity, partialTick, crumblingOverlay);
                        if (renderState == null) {
                            continue;
                        }

                        final Vector3d physicalCenter = subLevel.renderPose(partialTick).transformPosition(
                                new Vector3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5)
                        );
                        renderState.lightCoords = LevelRenderer.getLightColor(
                                this.level,
                                BlockPos.containing(physicalCenter.x, physicalCenter.y, physicalCenter.z)
                        );
                        levelRenderState.blockEntityRenderStates.add(renderState);
                        this.sable$blockEntityTransforms.put(renderState, new SableBlockEntityTransform(transformation, rotationPoint, new Quaternionf(subLevel.renderPose(partialTick).orientation())));
                    }
                }
            } finally {
                dispatcherExtension.sable$setCameraPosition(null);
            }
        }
    }

    @Inject(method = "submitBlockEntities", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V", shift = At.Shift.AFTER))
    private void sable$transformBlockEntity(final PoseStack poseStack, final LevelRenderState levelRenderState, final SubmitNodeStorage submitNodeStorage, final CallbackInfo ci, @Local final BlockEntityRenderState renderState, @Local final BlockPos blockPos) {
        final SableBlockEntityTransform transform = this.sable$blockEntityTransforms.remove(renderState);
        if (transform == null) {
            return;
        }

        final Vec3 cameraPosition = levelRenderState.cameraRenderState.pos;
        poseStack.translate(
                -(blockPos.getX() - cameraPosition.x),
                -(blockPos.getY() - cameraPosition.y),
                -(blockPos.getZ() - cameraPosition.z)
        );
        poseStack.mulPose(transform.transformation());
        poseStack.translate(
                blockPos.getX() - transform.rotationPoint().x(),
                blockPos.getY() - transform.rotationPoint().y(),
                blockPos.getZ() - transform.rotationPoint().z()
        );

        this.sable$cameraOrientation = new Quaternionf(levelRenderState.cameraRenderState.orientation);
        levelRenderState.cameraRenderState.orientation = new Quaternionf(transform.orientation()).conjugate().mul(levelRenderState.cameraRenderState.orientation);
    }

    @Inject(method = "submitBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", shift = At.Shift.AFTER))
    private void sable$restoreCameraOrientation(final PoseStack poseStack, final LevelRenderState levelRenderState, final SubmitNodeStorage submitNodeStorage, final CallbackInfo ci) {
        if (this.sable$cameraOrientation != null) {
            levelRenderState.cameraRenderState.orientation = this.sable$cameraOrientation;
            this.sable$cameraOrientation = null;
        }
    }

    @Unique
    private CrumblingOverlay sable$createCrumblingOverlay(final BlockPos blockPos, final Matrix4f transformation, final Vector3dc rotationPoint) {
        final SortedSet<BlockDestructionProgress> progress = this.destructionProgress.get(blockPos.asLong());
        if (progress == null || progress.isEmpty()) {
            return null;
        }

        final PoseStack poseStack = new PoseStack();
        poseStack.mulPose(transformation);
        poseStack.translate(
                blockPos.getX() - rotationPoint.x(),
                blockPos.getY() - rotationPoint.y(),
                blockPos.getZ() - rotationPoint.z()
        );
        return new CrumblingOverlay(progress.last().getProgress(), poseStack.last());
    }

    @Unique
    private record SableBlockEntityTransform(Matrix4f transformation, Vector3d rotationPoint, Quaternionf orientation) {
        private SableBlockEntityTransform(final Matrix4f transformation, final Vector3dc rotationPoint, final Quaternionf orientation) {
            this(new Matrix4f(transformation), new Vector3d(rotationPoint), new Quaternionf(orientation));
        }
    }
}
