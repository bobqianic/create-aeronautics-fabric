package dev.simulated_team.simulated.content.blocks.redstone_magnet;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import dev.simulated_team.simulated.compat.create.BlockEntityBehaviour;
import dev.simulated_team.simulated.util.SimMovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;

public class MagnetBehaviour extends BlockEntityBehaviour {
    public static final BehaviourType<MagnetBehaviour> TYPE = new BehaviourType<>();

    private SectionPos currentSection;
    private final MagnetMap<?> map;

    public MagnetBehaviour(final SmartBlockEntity te, final MagnetMap<?> map) {
        super(te);
        this.map = map;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (this.getWorld().isClientSide())
            return;
        this.currentSection = this.getCurrentSection();
        this.map.addMagnet(this.blockEntity.getLevel(), this.currentSection, this.blockEntity.getBlockPos());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClientSide())
            return;

        final SimMovementContext context = SimMovementContext.getMovementContext(this.getWorld(), Vec3.atCenterOf(this.blockEntity.getBlockPos()));
        final SectionPos newSection = SectionPos.of(context.globalPosition());

        if (!(newSection.x() == this.currentSection.x() && newSection.y() == this.currentSection.y() && newSection.z() == this.currentSection.z())) {
            this.map.removeMagnet(this.blockEntity.getLevel(), this.currentSection, this.blockEntity.getBlockPos());
            this.currentSection = newSection;
            this.map.addMagnet(this.blockEntity.getLevel(), this.currentSection, this.blockEntity.getBlockPos());
        }
    }


    @Override
    public void unload() {
        super.unload();
        if (this.getWorld().isClientSide())
            return;
        this.map.removeMagnet(this.blockEntity.getLevel(), this.currentSection, this.blockEntity.getBlockPos());
    }

    private SectionPos getCurrentSection() {
        final SimMovementContext context = SimMovementContext.getMovementContext(this.blockEntity.getLevel(), Vec3.atCenterOf(this.blockEntity.getBlockPos()));
        return SectionPos.of(BlockPos.containing(context.globalPosition()));
    }
}
