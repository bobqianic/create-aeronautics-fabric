package dev.simulated_team.simulated.fabric;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.index.SimStats;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

import java.util.ArrayList;
import java.util.List;

public final class FabricSimStats extends SimStats {
    private final List<Stat> stats = new ArrayList<>();

    public static void register() {
        final FabricSimStats registry = new FabricSimStats();
        registry.init();
        registry.stats.forEach(stat -> Stats.CUSTOM.get(stat.identifier().get(), stat.formatter()));
    }

    @Override
    protected Stat makeCustomStat(final String key, final StatFormatter formatter) {
        final ResourceLocation id = Simulated.path(key);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
        final Stat stat = new Stat(() -> id, formatter);
        this.stats.add(stat);
        return stat;
    }
}
