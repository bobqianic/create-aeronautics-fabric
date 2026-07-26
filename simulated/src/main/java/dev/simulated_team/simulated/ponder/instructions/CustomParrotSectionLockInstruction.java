package dev.simulated_team.simulated.ponder.instructions;

import dev.simulated_team.simulated.mixin.accessor.WorldSectionElementAccessor;
import dev.simulated_team.simulated.util.SimMathUtils;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.ParrotElement;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.Objects;

public class CustomParrotSectionLockInstruction extends TickingInstruction {

    protected ElementLink<WorldSectionElement> link;
    protected ElementLink<ParrotElement> parrotLink;
    protected final Vec3 position;

    protected WorldSectionElement element;
    protected ParrotElement parrot;

    public CustomParrotSectionLockInstruction(final ElementLink<WorldSectionElement> link, final ElementLink<ParrotElement> parrotLink, final Vec3 position, final int ticks) {
        super(false, ticks);
        this.link = link;
        this.parrotLink = parrotLink;
        this.position = position;
    }

    @Override
    protected void firstTick(final PonderScene scene) {
        super.firstTick(scene);
        this.element = Objects.requireNonNull(scene.resolve(this.link), "element");
        this.parrot = Objects.requireNonNull(scene.resolve(this.parrotLink), "parrot");
    }

    @Override
    public void tick(final PonderScene scene) {
        super.tick(scene);

        Vec3 totalOffset = this.position;
        final Quaternionf elementRot = new Quaternionf();
        if (this.link != null) {
            final Vec3 elementOffset = this.element.getAnimatedOffset();
            final Vec3 rotation = new Vec3(
                    Math.toRadians(this.element.getAnimatedRotation().x),
                    Math.toRadians(this.element.getAnimatedRotation().y),
                    Math.toRadians(this.element.getAnimatedRotation().z)
            );

            elementRot.mul(new Quaternionf((float) Math.sin(rotation.x / 2.0F), 0.0F, 0.0F, (float) Math.cos(rotation.x / 2.0F)));
            elementRot.mul(new Quaternionf(0.0F, 0.0F, (float) Math.sin(rotation.z / 2.0F), (float) Math.cos(rotation.z / 2.0F)));
            elementRot.mul(new Quaternionf(0.0F, (float) Math.sin(rotation.y / 2.0F), 0.0F, (float) Math.cos(rotation.y / 2.0F)));

            totalOffset = totalOffset.subtract(((WorldSectionElementAccessor) this.element).getCenterOfRotation());
            totalOffset = SimMathUtils.rotateQuatReverse(totalOffset, elementRot);
            totalOffset = totalOffset.add(((WorldSectionElementAccessor) this.element).getCenterOfRotation());
            totalOffset = totalOffset.add(elementOffset);
        }
        this.parrot.setPositionOffset(totalOffset.subtract(this.position), this.remainingTicks >= this.totalTicks - 2);
    }
}