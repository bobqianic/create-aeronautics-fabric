package dev.simulated_team.simulated.content.entities.honey_glue;

import dev.simulated_team.simulated.service.SimConfigService;
import com.zurrtum.create.catnip.data.Pair;
import net.minecraft.world.phys.AABB;

public class HoneyGlueMaxSizing {

    public static Pair<Boolean, String> checkBounds(final AABB bb) {
        if (checkBBMin(bb)) {
            return Pair.of(false, "Contracted area is too small");
        }

        if (checkBBMax(bb)) {
            return Pair.of(false, "Expanded area is too large");
        }

        return Pair.of(true, "");
    }

    public static boolean checkBBMin(final AABB bb) {
        return bb.getXsize() < 1 || bb.getYsize() < 1 || bb.getZsize() < 1;
    }

    public static boolean checkBBMax(final AABB bb) {
        final int max = SimConfigService.INSTANCE.server().assembly.honeyGlueRange.get();
        return bb.getXsize() > max || bb.getYsize() > max || bb.getZsize() > max;
    }
}
