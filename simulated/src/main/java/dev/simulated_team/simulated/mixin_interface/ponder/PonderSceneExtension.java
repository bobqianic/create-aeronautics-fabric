package dev.simulated_team.simulated.mixin_interface.ponder;

import net.minecraft.world.phys.Vec3;

public interface PonderSceneExtension {
    float simulated$getBasePlateAnimationTimer(float partialTicks);
    void simulated$toggleRenderBasePlateShadow();
    Vec3 simulated$getShadowOffset(float pt);
    void simulated$setShadowOffset(Vec3 v);
    void simulated$setOldShadowOffset(Vec3 v);
    void simulated$moveShadowOffset(Vec3 v);
    void simulated$setScaleFactor(float scale);
    float simulated$getScale(float pt);
    void simulated$setYOffset(float yOffset);
    float simulated$getYOffset(float pt);
}
