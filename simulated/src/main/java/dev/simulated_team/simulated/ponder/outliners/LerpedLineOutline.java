package dev.simulated_team.simulated.ponder.outliners;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.simulated_team.simulated.ponder.records.PonderLineRecord;
import com.zurrtum.create.client.catnip.outliner.LineOutline;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector4f;

public class LerpedLineOutline extends LineOutline {

    Vector3d prevStart;
    Vector3d prevEnd;

    public LerpedLineOutline(final PonderLineRecord initialLine) {
        this.prevStart = JOMLConversion.toJOML(initialLine.startPos());
        this.prevEnd = JOMLConversion.toJOML(initialLine.endPos());
    }

    public LerpedLineOutline(final Vec3 initialPoint) {
        this.prevStart = JOMLConversion.toJOML(initialPoint);
        this.prevEnd = JOMLConversion.toJOML(initialPoint);
    }

    public void update(final Vec3 prevStart, final Vec3 prevEnd, final Vec3 start, final Vec3 end) {
        this.prevStart = JOMLConversion.toJOML(prevStart);
        this.prevEnd = JOMLConversion.toJOML(prevEnd);

        this.set(start, end);
    }

    @Override
    public void render(final Minecraft minecraft, final PoseStack ms, final SuperRenderTypeBuffer buffer, final Vec3 camera, final float pt) {
        final float width = this.params.getLineWidth();
        if (width == 0)
            return;

        final VertexConsumer consumer = buffer.getBuffer(PonderRenderTypes.outlineSolid());
        this.params.loadColor(this.colorTemp);
        final Vector4f color = this.colorTemp;
        final int lightmap = LightTexture.FULL_BRIGHT;
        final boolean disableLineNormals = false;
        this.renderInner(ms, consumer, camera, pt, width, color, lightmap, disableLineNormals);
    }

    @Override
    protected void renderInner(final PoseStack ms, final VertexConsumer consumer, final Vec3 camera, final float pt, final float width, final Vector4f color, final int lightmap, final boolean disableNormals) {
        this.bufferCuboidLine(ms, consumer, camera, interpolatePoint(this.prevStart, this.start, pt), interpolatePoint(this.prevEnd, this.end, pt), width, color, lightmap, disableNormals);
    }

    public static Vector3d interpolatePoint(final Vector3d current, final Vector3d target, final float pt) {
        return new Vector3d(
                Mth.lerp(pt, current.x, target.x),
                Mth.lerp(pt, current.y, target.y),
                Mth.lerp(pt, current.z, target.z)
        );
    }
}
