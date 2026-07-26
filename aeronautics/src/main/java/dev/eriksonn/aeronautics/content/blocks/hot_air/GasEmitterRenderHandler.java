package dev.eriksonn.aeronautics.content.blocks.hot_air;


import com.zurrtum.create.catnip.animation.LerpedFloat;

public class GasEmitterRenderHandler {
    private final LerpedFloat position;
    private final LerpedFloat fade;

    public GasEmitterRenderHandler() {
        this.position = LerpedFloat.linear();
        this.fade = LerpedFloat.linear();
        this.position.chase(0, 0.2, LerpedFloat.Chaser.EXP);
        this.fade.chase(0, 0.2, LerpedFloat.Chaser.EXP);
    }

    public void targetFromRedstoneSignal(final int signal) {
        this.targetFromValue(signal / 15f);
    }

    public void targetFromValue(final float value) {
        this.position.updateChaseTarget(value);
    }

    public void tick() {
        this.position.tickChaser();
        this.fade.updateChaseTarget((this.position.getChaseTarget() > 0 || this.position.getValue() > 0.5) ? 1 : 0);
        this.fade.tickChaser();
    }

    public int getAlpha(final float partialTick) {
        return (int) (this.fade.getValue(partialTick) * 255);
    }

    public float getPosition(final float partialTick) {
        return this.position.getValue(partialTick);
    }
}
