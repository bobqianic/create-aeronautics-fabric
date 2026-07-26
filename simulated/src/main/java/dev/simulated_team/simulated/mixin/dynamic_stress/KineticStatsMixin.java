package dev.simulated_team.simulated.mixin.dynamic_stress;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.client.foundation.item.KineticStats;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import dev.simulated_team.simulated.api.CustomStressImpactTooltipProvider;
import dev.simulated_team.simulated.Simulated;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KineticStats.class)
public class KineticStatsMixin {

	@WrapOperation(method = "getKineticStats", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/client/catnip/lang/LangBuilder;add(Lcom/zurrtum/create/client/catnip/lang/LangBuilder;)Lcom/zurrtum/create/client/catnip/lang/LangBuilder;", ordinal = 0), remap = false)
	private static LangBuilder aeronautics$getKinetidStats(final LangBuilder instance, final LangBuilder otherBuilder, final Operation<LangBuilder> original, @Local(argsOnly = true) final Block block, @Local(name = "impactId") final IRotate.StressImpact impactId) {
		if(block instanceof final CustomStressImpactTooltipProvider impact) {
			return instance.text(TooltipHelper.makeProgressBar(impact.getBarLength(), impact.getFilledBarLength()))
					.style(impactId.getAbsoluteColor());
		}

		return instance.add(otherBuilder);
	}

	@WrapOperation(method = "getKineticStats", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/client/catnip/lang/LangBuilder;add(Lcom/zurrtum/create/client/catnip/lang/LangBuilder;)Lcom/zurrtum/create/client/catnip/lang/LangBuilder;", ordinal = 2), remap = false)
	private static LangBuilder aeronautics$getKinetidStats2(final LangBuilder instance, final LangBuilder otherBuilder, final Operation<LangBuilder> original, @Local(argsOnly = true) final Block block) {
		if(block instanceof final CustomStressImpactTooltipProvider impact) {
			return instance.add(otherBuilder.text(" x ")).add(impact.getCustomImpactLang());
		}

		return instance.add(otherBuilder);
	}

	@WrapOperation(method = "getKineticStats", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/client/catnip/lang/LangBuilder;translate(Ljava/lang/String;[Ljava/lang/Object;)Lcom/zurrtum/create/client/catnip/lang/LangBuilder;", ordinal = 0), remap = false)
	private static LangBuilder aeronautics$getKinetidStats4(final LangBuilder instance, final String langKey, final Object[] args, final Operation<LangBuilder> original, @Local(argsOnly = true) final Block block, @Local(name = "impactId") final IRotate.StressImpact impactId) {
		if(block instanceof CustomStressImpactTooltipProvider) {
			return new LangBuilder(Simulated.MOD_ID).space().translate("tooltip.dynamic_stress_impact")
					.style(impactId.getAbsoluteColor());
		}

		return instance.translate(langKey, args);
	}

}
