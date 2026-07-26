package dev.ryanhcode.offroad.fabric;

import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.data.OffroadAdvancementTriggers;
import dev.ryanhcode.offroad.events.OffroadCommonEvents;
import dev.ryanhcode.offroad.fabric.service.FabricOffroadConfigService;
import dev.ryanhcode.offroad.index.OffroadAdvancements;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;

public final class OffroadFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricOffroadConfigService.register();
        Offroad.init();
        OffroadAdvancements.init();
        OffroadAdvancementTriggers.register();
        Offroad.getRegistrate().register();

        DefaultItemComponentEvents.MODIFY.register(context ->
                OffroadCommonEvents.modifyDefaultComponents((item, modifier) ->
                        context.modify(item.asItem(), builder -> modifier.accept(builder::set))));
        ServerTickEvents.END_WORLD_TICK.register(OffroadCommonEvents::tickLevelEvent);
    }
}
