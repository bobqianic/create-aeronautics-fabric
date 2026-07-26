package dev.simulated_team.simulated.content.blocks.rope;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientRopeStrand;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.RopeAttachment;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerRopeStrand;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface RopeStrandHolderBlockEntity extends BlockEntitySubLevelActor {

    RopeStrandHolderBehavior getBehavior();

    Vec3 getAttachmentPoint(final BlockPos pos, final BlockState state);

    default Vec3 getVisualAttachmentPoint(final BlockPos pos, final BlockState state) {
        return this.getAttachmentPoint(pos, state);
    }

    @Override
    @Nullable
    default Iterable<@NotNull SubLevel> sable$getConnectionDependencies() {
        final RopeStrandHolderBehavior behavior = this.getBehavior();
        final SmartBlockEntity be = behavior.blockEntity;

        final Level level = be.getLevel();
        final SubLevelContainer container = SubLevelContainer.getContainer(level);
        assert container != null;

        final ServerRopeStrand serverStrand = behavior.getAttachedStrand();

        if (serverStrand != null) {
            final ObjectList<SubLevel> dependencies = new ObjectArrayList<>();
            final Iterable<RopeAttachment> attachments = serverStrand.getAttachments();

            for (final RopeAttachment attachment : attachments) {
                final UUID id = attachment.subLevelID();
                if (id == null) continue;

                final SubLevel subLevel = container.getSubLevel(id);

                if (subLevel != null) {
                    dependencies.add(subLevel);
                }
            }

            return dependencies;
        }

        final ClientRopeStrand clientStrand = behavior.getClientStrand();

        if (clientStrand != null) {
            final ObjectList<SubLevel> dependencies = new ObjectArrayList<>();

            final SubLevel subLevelA = Sable.HELPER.getContaining(level, clientStrand.startAttachment);
            final SubLevel subLevelB = Sable.HELPER.getContaining(level, clientStrand.endAttachment);

            if (subLevelA != null) dependencies.add(subLevelA);
            if (subLevelB != null) dependencies.add(subLevelB);

            return dependencies;
        }

        return null;
    }
}
