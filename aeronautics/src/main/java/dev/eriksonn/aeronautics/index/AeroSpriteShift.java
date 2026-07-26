package dev.eriksonn.aeronautics.index;

import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShifter;
import dev.eriksonn.aeronautics.Aeronautics;

import static com.zurrtum.create.client.foundation.block.connected.AllCTTypes.OMNIDIRECTIONAL;

public class AeroSpriteShift {
	public static final CTSpriteShiftEntry LEVITITE = omni("block/levitite");
	public static final CTSpriteShiftEntry PEARLESCENT_LEVITITE = omni("block/pearlescent_levitite");

	static CTSpriteShiftEntry omni(String name) {
		return CTSpriteShifter.getCT(OMNIDIRECTIONAL,
				Aeronautics.path(name),
				Aeronautics.path(name + "_connected"));
	}
}
