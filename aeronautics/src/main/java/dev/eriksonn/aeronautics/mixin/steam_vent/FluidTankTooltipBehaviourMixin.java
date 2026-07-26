package dev.eriksonn.aeronautics.mixin.steam_vent;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.FluidTankTooltipBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.fluids.tank.BoilerData;
import dev.eriksonn.aeronautics.data.AeroLang;
import dev.eriksonn.aeronautics.mixinterface.BoilerDataExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = FluidTankTooltipBehaviour.class, remap = false)
public abstract class FluidTankTooltipBehaviourMixin {
    @ModifyExpressionValue(method = "addBoilerDataTooltip", at = {
            @At(value = "FIELD", target = "Lcom/zurrtum/create/content/fluids/tank/BoilerData;attachedEngines:I", ordinal = 0),
            @At(value = "FIELD", target = "Lcom/zurrtum/create/content/fluids/tank/BoilerData;attachedEngines:I", ordinal = 2)
    })
    private int aeronautics$countSteamConsumers(final int original, final BoilerData data,
                                                 final List<Component> tooltip, final int boilerSize) {
        return original + ((BoilerDataExtension) data).aeronautics$getAttachedVents();
    }

    @Inject(method = "addBoilerDataTooltip", at = @At(value = "INVOKE",
            target = "Lcom/zurrtum/create/client/catnip/lang/LangBuilder;forGoggles(Ljava/util/List;)V",
            shift = At.Shift.AFTER, ordinal = 2), cancellable = true)
    private void aeronautics$reformatBoilerTooltip(final BoilerData data, final List<Component> tooltip,
                                                    final int boilerSize,
                                                    final CallbackInfoReturnable<Boolean> cir) {
        final BoilerDataExtension extension = (BoilerDataExtension) data;
        final int boilerLevel = Math.max(Math.min(data.activeHeat,
                Math.min(data.maxHeatForWater, data.maxHeatForSize)), 1);
        final float idealEfficiency = extension.aeronautics$getIdealEfficiency(boilerSize);
        final double totalSU = idealEfficiency * 16 * boilerLevel
                * BlockStressValues.getCapacity(AllBlocks.STEAM_ENGINE);

        CreateLang.number(totalSU)
                .translate("generic.unit.stress")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        final float efficiency = data.getEngineEfficiency(boilerSize) / idealEfficiency;
        final double engineSU = totalSU * efficiency * data.attachedEngines / boilerLevel;
        final int attachedVents = extension.aeronautics$getAttachedVents();
        final double ventSU = totalSU * efficiency * attachedVents / boilerLevel;

        if (engineSU > 0 || ventSU > 0) {
            AeroLang.translate("tooltip.capacity_used").style(ChatFormatting.GRAY).forGoggles(tooltip);
        }
        if (engineSU > 0) {
            CreateLang.number(engineSU)
                    .translate("generic.unit.stress")
                    .style(ChatFormatting.AQUA)
                    .space()
                    .add((data.attachedEngines == 1 ? CreateLang.translate("boiler.via_one_engine")
                            : CreateLang.translate("boiler.via_engines", data.attachedEngines))
                            .style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);
        }
        if (ventSU > 0) {
            CreateLang.number(ventSU)
                    .translate("generic.unit.stress")
                    .style(ChatFormatting.AQUA)
                    .space()
                    .add((attachedVents == 1 ? AeroLang.translate("boiler.via_one_vent")
                            : AeroLang.translate("boiler.via_vents", attachedVents))
                            .style(ChatFormatting.DARK_GRAY).component())
                    .forGoggles(tooltip, 1);
        }
        cir.setReturnValue(true);
    }
}
