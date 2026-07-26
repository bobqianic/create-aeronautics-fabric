package dev.eriksonn.aeronautics;

import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.audio.KineticAudioBehaviour;
import com.zurrtum.create.client.foundation.item.ItemDescription;
import dev.eriksonn.aeronautics.content.ponder.AeroPonderPlugin;
import dev.eriksonn.aeronautics.content.blocks.mounted_potato_cannon.MountedPotatoCannonVisual;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.gyroscopic_propeller_bearing.GyroscopicPropellerBearingVisual;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing.PropellerThrustDirectionClientBehaviour;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing.PropellerBearingVisual;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.andesite.AndesitePropellerVisual;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.wooden.WoodenPropellerVisual;
import dev.eriksonn.aeronautics.index.AeroClickInteractions;
import dev.eriksonn.aeronautics.index.AeroBlockEntityTypes;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.eriksonn.aeronautics.index.AeroPartialModels;
import dev.eriksonn.aeronautics.index.client.AeroClientRegistries;
import dev.eriksonn.aeronautics.index.client.AeroRenderTypes;
import dev.eriksonn.aeronautics.index.client.AeroSituationalMusic;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import dev.simulated_team.simulated.compat.create.LegacyCustomKineticTooltipBehaviour;
import dev.simulated_team.simulated.compat.create.LegacyItemTooltips;
import dev.simulated_team.simulated.compat.create.LegacyScrollValueClientBehaviour;
import dev.simulated_team.simulated.compat.create.LegacyTooltipBehaviour;
import dev.simulated_team.simulated.compat.create.SableSubLevelRendererCompat;

public class AeronauticsClient {
    public static void init() {
        AeroRenderTypes.init();
        registerBlockEntityBehaviours();
        registerVisuals();
        registerSableImmediateRenderers();

        AeroBlocks.DYED_ENVELOPE_BLOCKS.forEach(block ->
                ItemDescription.useKey(block, "block.aeronautics.white_envelope"));
        AeroBlocks.ENVELOPE_ENCASED_SHAFTS.forEach(block ->
                ItemDescription.useKey(block, "block.aeronautics.white_envelope_encased_shaft"));

        LegacyItemTooltips.register(Aeronautics.getRegistrate());

        PonderIndex.addPlugin(new AeroPonderPlugin());

        AeroClientRegistries.init();
        AeroPartialModels.init();
        AeroSituationalMusic.init();
        AeroClickInteractions.init();
    }

    private static void registerSableImmediateRenderers() {
        SableSubLevelRendererCompat.registerLegacyRenderers(
                Aeronautics.LOGGER,
                AeroBlockEntityTypes.HOT_AIR_BURNER.get(),
                AeroBlockEntityTypes.STEAM_VENT.get(),
                AeroBlockEntityTypes.PROPELLER_BEARING.get(),
                AeroBlockEntityTypes.GYROSCOPIC_PROPELLER_BEARING.get(),
                AeroBlockEntityTypes.SMART_PROPELLER.get(),
                AeroBlockEntityTypes.ANDESITE_PROPELLER.get(),
                AeroBlockEntityTypes.WOODEN_PROPELLER.get(),
                AeroBlockEntityTypes.MOUNTED_POTATO_CANNON.get()
        );
    }

    private static void registerBlockEntityBehaviours() {
        AllBlockEntityBehaviours.add(AeroBlockEntityTypes.STEAM_VENT.get(), LegacyTooltipBehaviour::new, LegacyScrollValueClientBehaviour::new);
        AllBlockEntityBehaviours.add(AeroBlockEntityTypes.HOT_AIR_BURNER.get(), LegacyTooltipBehaviour::new, LegacyScrollValueClientBehaviour::new);
        AllBlockEntityBehaviours.add(AeroBlockEntityTypes.PROPELLER_BEARING.get(), LegacyCustomKineticTooltipBehaviour::new,
                KineticAudioBehaviour::new, PropellerThrustDirectionClientBehaviour::new);
        AllBlockEntityBehaviours.add(AeroBlockEntityTypes.GYROSCOPIC_PROPELLER_BEARING.get(), LegacyCustomKineticTooltipBehaviour::new,
                KineticAudioBehaviour::new, PropellerThrustDirectionClientBehaviour::new);
        AllBlockEntityBehaviours.add(AeroBlockEntityTypes.ENVELOPE_ENCASED_SHAFT.get(), LegacyCustomKineticTooltipBehaviour::new,
                KineticAudioBehaviour::new);
        AllBlockEntityBehaviours.add(AeroBlockEntityTypes.ANDESITE_PROPELLER.get(), LegacyCustomKineticTooltipBehaviour::new,
                KineticAudioBehaviour::new);
        AllBlockEntityBehaviours.add(AeroBlockEntityTypes.WOODEN_PROPELLER.get(), LegacyCustomKineticTooltipBehaviour::new,
                KineticAudioBehaviour::new);
        AllBlockEntityBehaviours.add(AeroBlockEntityTypes.SMART_PROPELLER.get(), LegacyCustomKineticTooltipBehaviour::new,
                KineticAudioBehaviour::new);
        AllBlockEntityBehaviours.add(AeroBlockEntityTypes.MOUNTED_POTATO_CANNON.get(), LegacyCustomKineticTooltipBehaviour::new,
                KineticAudioBehaviour::new);
    }

    private static void registerVisuals() {
        SimpleBlockEntityVisualizer.builder(AeroBlockEntityTypes.PROPELLER_BEARING.get()).factory(PropellerBearingVisual::new).apply();
        SimpleBlockEntityVisualizer.builder(AeroBlockEntityTypes.GYROSCOPIC_PROPELLER_BEARING.get()).factory(GyroscopicPropellerBearingVisual::new).apply();
        SimpleBlockEntityVisualizer.builder(AeroBlockEntityTypes.ENVELOPE_ENCASED_SHAFT.get()).factory(SingleAxisRotatingVisual::shaft).apply();
        SimpleBlockEntityVisualizer.builder(AeroBlockEntityTypes.ANDESITE_PROPELLER.get()).factory(AndesitePropellerVisual::new).apply();
        SimpleBlockEntityVisualizer.builder(AeroBlockEntityTypes.WOODEN_PROPELLER.get()).factory(WoodenPropellerVisual::new).apply();
        SimpleBlockEntityVisualizer.builder(AeroBlockEntityTypes.MOUNTED_POTATO_CANNON.get()).factory(MountedPotatoCannonVisual::new).apply();
    }
}
