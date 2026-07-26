package dev.ryanhcode.sable.sublevel.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ryanhcode.sable.render.SubLevelDynamicLights;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Preserves terrain-rendered per-vertex light while accounting for a sub-level's
 * physical location.
 *
 * <p>The physical light field is sampled at each transformed vertex instead of
 * once per block. A single packed value per block creates hard one-block bands
 * around light sources even when the model renderer supplies smooth lighting.
 * Trilinear sampling also guarantees that two blocks sharing a physical vertex
 * receive the same parent-world light value.</p>
 */
public final class SubLevelLightVertexConsumerProvider
        implements BlockVertexConsumerProvider, MultiBufferSource {

    private static final double LIGHT_CELL_CENTER = 0.5;

    private final ClientLevel level;
    private final double cameraX;
    private final double cameraY;
    private final double cameraZ;
    private final SubLevelDynamicLights.OwnLightMask ownLightMask;
    private final BlockVertexConsumerProvider blockDelegate;
    private final MultiBufferSource blockEntityDelegate;
    private final Map<VertexConsumer, SubLevelLightVertexConsumer> consumers = new IdentityHashMap<>();
    private final Long2IntOpenHashMap lightCache = new Long2IntOpenHashMap();
    private final BlockPos.MutableBlockPos lightPos = new BlockPos.MutableBlockPos();

    public SubLevelLightVertexConsumerProvider(
            final ClientLevel level,
            final ClientSubLevel subLevel,
            final double cameraX,
            final double cameraY,
            final double cameraZ,
            final BlockVertexConsumerProvider blockDelegate,
            final MultiBufferSource blockEntityDelegate
    ) {
        this.level = level;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.cameraZ = cameraZ;
        this.ownLightMask = SubLevelDynamicLights.getOwnLightMask(
                level.getLightEngine(),
                subLevel
        );
        this.blockDelegate = blockDelegate;
        this.blockEntityDelegate = blockEntityDelegate;
        this.lightCache.defaultReturnValue(-1);
    }

    @Override
    public VertexConsumer getBuffer(final ChunkSectionLayer layer) {
        return this.wrap(this.blockDelegate.getBuffer(layer));
    }

    @Override
    public VertexConsumer getBuffer(final RenderType renderType) {
        return this.wrap(this.blockEntityDelegate.getBuffer(renderType));
    }

    private VertexConsumer wrap(final VertexConsumer delegate) {
        return this.consumers.computeIfAbsent(delegate, SubLevelLightVertexConsumer::new);
    }

    private int samplePhysicalLight(final double worldX, final double worldY, final double worldZ) {
        // Light values belong to block-cell centers. Moving the interpolation
        // lattice by half a block makes the value at a shared model vertex the
        // weighted average of the same eight cells from either adjacent block.
        final double sampleX = worldX - LIGHT_CELL_CENTER;
        final double sampleY = worldY - LIGHT_CELL_CENTER;
        final double sampleZ = worldZ - LIGHT_CELL_CENTER;
        final int x0 = floor(sampleX);
        final int y0 = floor(sampleY);
        final int z0 = floor(sampleZ);
        final double xFraction = sampleX - x0;
        final double yFraction = sampleY - y0;
        final double zFraction = sampleZ - z0;

        final int light000 = this.getCellLight(x0, y0, z0);
        final int light100 = this.getCellLight(x0 + 1, y0, z0);
        final int light010 = this.getCellLight(x0, y0 + 1, z0);
        final int light110 = this.getCellLight(x0 + 1, y0 + 1, z0);
        final int light001 = this.getCellLight(x0, y0, z0 + 1);
        final int light101 = this.getCellLight(x0 + 1, y0, z0 + 1);
        final int light011 = this.getCellLight(x0, y0 + 1, z0 + 1);
        final int light111 = this.getCellLight(x0 + 1, y0 + 1, z0 + 1);

        final double blockLight = interpolate(
                component(light000, 0), component(light100, 0),
                component(light010, 0), component(light110, 0),
                component(light001, 0), component(light101, 0),
                component(light011, 0), component(light111, 0),
                xFraction, yFraction, zFraction
        );
        final double skyLight = interpolate(
                component(light000, 4), component(light100, 4),
                component(light010, 4), component(light110, 4),
                component(light001, 4), component(light101, 4),
                component(light011, 4), component(light111, 4),
                xFraction, yFraction, zFraction
        );

        return toLightUv(blockLight) | toLightUv(skyLight) << 16;
    }

    private int getCellLight(final int x, final int y, final int z) {
        final long packedPos = BlockPos.asLong(x, y, z);
        int packedLight = this.lightCache.get(packedPos);
        if (packedLight >= 0) {
            return packedLight;
        }

        this.lightPos.set(x, y, z);
        int blockLight = this.level.getBrightness(LightLayer.BLOCK, this.lightPos);
        if (blockLight <= this.ownLightMask.upperBound(packedPos)) {
            // The plot renderer already supplies this sub-level's own light.
            // Do not let the same source's whole-voxel parent projection
            // override it as the moving structure crosses block boundaries.
            blockLight = 0;
        }
        packedLight = blockLight
                | this.level.getBrightness(LightLayer.SKY, this.lightPos) << 4;
        this.lightCache.put(packedPos, packedLight);
        return packedLight;
    }

    private static int floor(final double value) {
        final int integer = (int) value;
        return value < integer ? integer - 1 : integer;
    }

    private static int component(final int packedLight, final int shift) {
        return packedLight >>> shift & 15;
    }

    private static double interpolate(
            final double value000,
            final double value100,
            final double value010,
            final double value110,
            final double value001,
            final double value101,
            final double value011,
            final double value111,
            final double x,
            final double y,
            final double z
    ) {
        final double value00 = lerp(x, value000, value100);
        final double value10 = lerp(x, value010, value110);
        final double value01 = lerp(x, value001, value101);
        final double value11 = lerp(x, value011, value111);
        return lerp(z, lerp(y, value00, value10), lerp(y, value01, value11));
    }

    private static double lerp(final double delta, final double start, final double end) {
        return start + delta * (end - start);
    }

    private static int toLightUv(final double light) {
        return Math.max(0, Math.min(240, (int) Math.round(light * 16.0)));
    }

    private final class SubLevelLightVertexConsumer implements VertexConsumer {

        private final VertexConsumer delegate;
        private double worldX;
        private double worldY;
        private double worldZ;

        private SubLevelLightVertexConsumer(final VertexConsumer delegate) {
            this.delegate = delegate;
        }

        @Override
        public VertexConsumer addVertex(final float x, final float y, final float z) {
            this.worldX = x + SubLevelLightVertexConsumerProvider.this.cameraX;
            this.worldY = y + SubLevelLightVertexConsumerProvider.this.cameraY;
            this.worldZ = z + SubLevelLightVertexConsumerProvider.this.cameraZ;
            this.delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(final int red, final int green, final int blue, final int alpha) {
            this.delegate.setColor(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer setUv(final float u, final float v) {
            this.delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(final int u, final int v) {
            this.delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(final int blockLight, final int skyLight) {
            final int physicalLight = SubLevelLightVertexConsumerProvider.this.samplePhysicalLight(
                    this.worldX,
                    this.worldY,
                    this.worldZ
            );
            this.delegate.setUv2(
                    Math.max(blockLight, physicalLight & 0xffff),
                    Math.min(skyLight, physicalLight >>> 16 & 0xffff)
            );
            return this;
        }

        @Override
        public VertexConsumer setNormal(final float x, final float y, final float z) {
            this.delegate.setNormal(x, y, z);
            return this;
        }
    }
}
