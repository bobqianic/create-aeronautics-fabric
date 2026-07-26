package dev.simulated_team.simulated.index;

import dev.simulated_team.simulated.Simulated;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SpriteShifter;

public class SimSpriteShifts {

    public static final SpriteShiftEntry ROPE_WINCH_COIL = get("block/rope_winch/winch_coil", "block/rope_winch/winch_coil_scroll");

    public static void init() {

    }

    private static SpriteShiftEntry get(final String originalLocation, final String targetLocation) {
        return SpriteShifter.get(Simulated.path(originalLocation), Simulated.path(targetLocation));
    }

}
