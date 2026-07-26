package com.zurrtum.create.foundation.block;

import net.minecraft.world.level.block.state.BlockBehaviour;

/** Basic Fabric-safe property presets used by the content registration code. */
public class SharedProperties {
    protected SharedProperties() {
    }

    public static BlockBehaviour.Properties wooden() { return BlockBehaviour.Properties.of(); }
    public static BlockBehaviour.Properties stone() { return BlockBehaviour.Properties.of(); }
    public static BlockBehaviour.Properties softMetal() { return BlockBehaviour.Properties.of(); }
    public static BlockBehaviour.Properties copperMetal() { return BlockBehaviour.Properties.of(); }
    public static BlockBehaviour.Properties netheriteMetal() { return BlockBehaviour.Properties.of(); }
}
