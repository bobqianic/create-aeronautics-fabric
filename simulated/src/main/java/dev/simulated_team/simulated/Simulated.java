package dev.simulated_team.simulated;

import com.mojang.logging.LogUtils;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.ryanhcode.sable.platform.SableEventPlatform;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.events.SimulatedCommonEvents;
import dev.simulated_team.simulated.index.*;
import dev.simulated_team.simulated.network.SimPacketManager;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import dev.simulated_team.simulated.service.SimModCompatibilityService;
import dev.simulated_team.simulated.util.SimAssemblyHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public final class Simulated {
    public static final String MOD_ID = "simulated";
    public static final String MOD_NAME = "Create Simulated";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final NonNullSupplier<SimulatedRegistrate> REGISTRATE = NonNullSupplier.lazy(() ->
            (SimulatedRegistrate) new SimulatedRegistrate(path("simulated"), MOD_ID).defaultCreativeTab((ResourceKey)null));

    public static void init() {
        SimEntityDataSerializers.register();
        getRegistrate().addDataGenerator(ProviderType.LANG, SimLang::registrateLang);

        SimRegistries.register();
        SimTags.register();
        SimBlocks.register();
        SimItems.register();
        SimBlockEntityTypes.register();
        SimParticleTypes.register();
        SimSoundEvents.init();
        SimPacketManager.init();
        SimEntityTypes.register();
        SimMenuTypes.register();
        SimNavigationTargets.register();
        SimDataComponents.register();
        SimItemAttributeTypes.init();

        SimulatedCommonEvents.register();
        SimBlockMovementChecks.register();
        SimAssemblyHelper.register();
        SimModCompatibilityService.initLoaded();

        SableEventPlatform.INSTANCE.onPhysicsTick(SimulatedCommonEvents::onPhysicsTick);
        SableEventPlatform.INSTANCE.onPostPhysicsTick(SimulatedCommonEvents::onPostPhysicsTick);
    }

    public static SimulatedRegistrate getRegistrate() {
        return REGISTRATE.get();
    }

    public static ResourceLocation path(final String path) {
        return ResourceLocation.tryBuild(MOD_ID, path);
    }

}
