package dev.simulated_team.simulated.content.blocks.redstone_magnet;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class MagnetPairIdentifier {
    @NotNull
    private final BlockPos posA;

    @NotNull
    private final BlockPos posB;

    public MagnetPairIdentifier(final BlockPos posA, final BlockPos posB) {
        if(posA.compareTo(posB) > 0) {
            this.posA = posA;
            this.posB = posB;
        }else{
            this.posB = posA;
            this.posA = posB;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        final MagnetPairIdentifier that = (MagnetPairIdentifier) o;
        return this.posA.equals(that.posA) && this.posB.equals(that.posB);
    }

    @Override
    public int hashCode() {
        int result = this.posA.hashCode();
        result = 31 * result + this.posB.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MagnetPairIdentifier[" +
                "posA=" + this.posA + ", " +
                "posB=" + this.posB + ']';
    }
}
