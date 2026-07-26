package dev.simulated_team.simulated.content.blocks.altitude_sensor;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class AltitudeSensorMovementBehaviour extends MovementBehaviour {

    public boolean disableBlockEntityRendering() {
        return true;
    }

    @Override
    public void tick(final MovementContext context) {
        super.tick(context);

        // temporaryData <- (previousVisualHeight, visualHeight)
        final float yPos = (float) Sable.HELPER.projectOutOfSubLevel(context.world, JOMLConversion.toJOML(context.position)).y;
        if (context.temporaryData instanceof final Tuple<?, ?> heights) {
            context.temporaryData = new Tuple<>(heights.getB(), yPos);
        } else {
            context.temporaryData = new Tuple<>(yPos, yPos);
        }
    }

    public void renderInContraption(final MovementContext context, final VirtualRenderWorld renderWorld, final ContraptionMatrices matrices, final MultiBufferSource buffer) {
        final float lowSignal = context.blockEntityData.getFloatOr("low_signal", 0);
        final float highSignal = context.blockEntityData.getFloatOr("high_signal", 0);

        final float visualHeight;
        if (context.temporaryData instanceof final Tuple<?, ?> heights) {
            visualHeight = ((float) heights.getA()) * (1 - AnimationTickHolder.getPartialTicks()) + (float) heights.getB() * AnimationTickHolder.getPartialTicks();
        } else {
            final Vector3d pos = context.position != null ? JOMLConversion.toJOML(context.position) : new Vector3d();
            visualHeight = (float) Sable.HELPER.projectOutOfSubLevel(context.world, pos).y;
        }

        final Level level = context.contraption.entity.level();
        final float y = (float) Mth.map(context.position.y, level.getMinY(), level.getMinY() + level.getHeight(), 0.0f, 1.0f);
        final float value = Mth.clampedMap(y, 0.0f, 1.0f, lowSignal, highSignal);

        AltitudeSensorRenderer.render(context.state, 1000, value, visualHeight, matrices.getViewProjection(), matrices.getModel(), matrices.getWorld(), buffer, LevelRenderer.getLightColor(renderWorld, context.localPos));
    }
}
