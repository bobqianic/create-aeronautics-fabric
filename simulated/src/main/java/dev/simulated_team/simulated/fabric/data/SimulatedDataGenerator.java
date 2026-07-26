package dev.simulated_team.simulated.fabric.data;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.data.advancements.SimAdvancements;
import dev.simulated_team.simulated.index.SimSoundEvents;
import dev.simulated_team.simulated.index.SimTags;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

import java.util.List;
import java.util.Set;

public final class SimulatedDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        SimTags.addGenerators();
        final FabricDataGenerator.Pack pack = generator.createPack();
        Simulated.getRegistrate().setupDatagen(
                pack, new ExistingFileHelper(List.of(), Set.of(), false, null, null));
        pack.addProvider(SimAdvancements::new);
        pack.addProvider((output, registries) -> SimSoundEvents.REGISTRY.getProvider(output));
    }
}
