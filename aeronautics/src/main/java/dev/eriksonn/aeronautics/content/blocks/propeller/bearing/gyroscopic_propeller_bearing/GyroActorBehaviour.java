package dev.eriksonn.aeronautics.content.blocks.propeller.bearing.gyroscopic_propeller_bearing;

import dev.eriksonn.aeronautics.content.blocks.propeller.behaviour.PropellerActorBehaviour;
import dev.eriksonn.aeronautics.data.AeroLang;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class GyroActorBehaviour<T extends GyroscopicPropellerBearingBlockEntity> extends PropellerActorBehaviour {

    public GyroActorBehaviour(final T be) {
        super(be, be);
    }

    @Override
    public void additionalTooltipInfo(final List<Component> tooltip, final boolean isPlayerSneaking) {
        final double gravStrength = DimensionPhysicsData.getGravity(this.getWorld(), JOMLConversion.toJOML(this.getPos().getCenter())).length();

        final MutableComponent canLiftComponent = AeroLang.kilopixelGram(Math.abs(this.propeller.getScaledThrust()) / gravStrength)
                .style(ChatFormatting.AQUA)
                .component();

        AeroLang.translate("propeller.can_lift", canLiftComponent)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
    }
}
