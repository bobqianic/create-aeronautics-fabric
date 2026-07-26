package dev.ryanhcode.offroad;

import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.ryanhcode.offroad.data.OffroadLang;
import dev.ryanhcode.offroad.events.OffroadCommonEvents;
import dev.ryanhcode.offroad.index.*;
import dev.ryanhcode.offroad.network.OffroadPacketManager;
import dev.ryanhcode.sable.platform.SableEventPlatform;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Offroad {
	public static final String MOD_ID = "offroad";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final NonNullSupplier<SimulatedRegistrate> REGISTRATE = NonNullSupplier.lazy(() ->
			(SimulatedRegistrate) new SimulatedRegistrate(Offroad.path(MOD_ID), MOD_ID).defaultCreativeTab((ResourceKey<CreativeModeTab>) null));

	public static void init() {
		getRegistrate().addDataGenerator(ProviderType.LANG, OffroadLang::registrateLang);

		OffroadBlocks.init();
		OffroadBlockEntityTypes.init();
		OffroadEntityTypes.init();
		OffroadDataComponents.init();
		OffroadItems.init();
		OffroadSoundEvents.init();
		OffroadPacketManager.init();

		OffroadContraptionTypes.init();

		listenCommonEvents();
	}

	private static void listenCommonEvents() {
		SableEventPlatform.INSTANCE.onPhysicsTick(OffroadCommonEvents::physicsTick);
	}

	public static SimulatedRegistrate getRegistrate() {
		return REGISTRATE.get();
	}

	public static ResourceLocation path(final String path) {
		return ResourceLocation.tryBuild(MOD_ID, path);
	}
}
