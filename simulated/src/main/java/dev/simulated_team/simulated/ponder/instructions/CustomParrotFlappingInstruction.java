package dev.simulated_team.simulated.ponder.instructions;

import dev.simulated_team.simulated.mixin.accessor.ParrotElementAccessor;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.ParrotElement;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import com.zurrtum.create.client.ponder.foundation.ui.PonderUI;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;

import java.util.Objects;

public class CustomParrotFlappingInstruction extends TickingInstruction {

    protected final ElementLink<ParrotElement> parrotLink;
    protected final float speed;
    protected final boolean shouldGround;
    protected ParrotElement parrot;

    public CustomParrotFlappingInstruction(final ElementLink<ParrotElement> parrotLink, final float speed, final int ticks) {
        super(false, ticks);
        this.parrotLink = parrotLink;
        this.speed = speed;
        this.shouldGround = false;
    }

    public CustomParrotFlappingInstruction(final ElementLink<ParrotElement> parrotLink) {
        super(false, 0);
        this.parrotLink = parrotLink;
        this.speed = 0;
        this.shouldGround = true;
    }

    @Override
    protected void firstTick(final PonderScene scene) {
        super.firstTick(scene);
        this.parrot = Objects.requireNonNull(scene.resolve(this.parrotLink), "parrot");
    }

    @Override
    public void tick(final PonderScene scene) {
        super.tick(scene);
        final Parrot entity = ((ParrotElementAccessor) this.parrot).getEntity();
        if (!this.shouldGround) {
            entity.setOnGround(false);
            entity.flapSpeed = Mth.sin((PonderUI.ponderTicks % 100) * this.speed) + 1;
        } else {
            entity.setOnGround(true);
            entity.flapSpeed = 0;
        }
    }
}
