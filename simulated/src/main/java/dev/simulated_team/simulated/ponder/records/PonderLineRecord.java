package dev.simulated_team.simulated.ponder.records;

import net.minecraft.world.phys.Vec3;

public record PonderLineRecord(Vec3 startPos, Vec3 endPos) {
    public static PonderLineRecord withOffset(final Vec3 startPos, final Vec3 endPos) {
        return new PonderLineRecord(startPos.add(87, 0, 0), endPos.add(87, 0, 0));
    }

    public static PonderLineRecord withOffset(final double startPosX, final double startPosY, final double endPosX, final double endPosY) {
        return new PonderLineRecord(new Vec3(startPosX + 87, startPosY, 0), new Vec3(endPosX + 87, endPosY, 0));
    }
}