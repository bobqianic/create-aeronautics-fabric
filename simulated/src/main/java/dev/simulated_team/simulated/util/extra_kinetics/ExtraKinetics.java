package dev.simulated_team.simulated.util.extra_kinetics;

import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import dev.simulated_team.simulated.data.SimLang;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * BlockEntity interface that indicates that this block entity contains extra kinetics that should be accounted for. <br/>
 * Setting level, blockstate, invalidating, removing, and NBT serialization is handled automatically inside the KineticBlockEntity
 */
//TODO: contraption fixing, wrenching fix
public interface ExtraKinetics {

    /**
     * @return The ExtraKinetic BlockEntity associated with the parent BlockEntity
     */
    @Nullable
    KineticBlockEntity getExtraKinetics();

    /**
     * @return Whether this BlockEntity should connect to its ExtraKinetic BlockEntity, assumed to be symmetrical
     */
    boolean shouldConnectExtraKinetics();

    /**
     * @return The name this ExtraKinetic will be saved to in the parent's NBT data
     */
    default String getExtraKineticsSaveName() {
        return "DEFAULT";
    }

    /**
     * A helper interface intended to be used on Blocks that contain an ExtraKinetic BlockEntity
     */
    @FunctionalInterface
    interface ExtraKineticsBlock {
        IRotate getExtraKineticsRotationConfiguration();
    }

    interface ExtraKineticsBlockEntity {
        KineticBlockEntity getParentBlockEntity();

        default Component getKey() {
            return SimLang.translate("extra_kinetics.default").component();
        }
    }
}