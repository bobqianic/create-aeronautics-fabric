package dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class LiftingGasData {

    public static Codec<LiftingGasData> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(Codec.DOUBLE.fieldOf("target").forGetter(LiftingGasData::getTarget),
                            Codec.DOUBLE.fieldOf("amount").forGetter(LiftingGasData::getAmount),
                            Codec.DOUBLE.fieldOf("nudge").forGetter(LiftingGasData::getNudge)
                    ).apply(instance, LiftingGasData::new));

    public double target;
    public double amount;
    public double nudge;

    public LiftingGasData() {
        this.target = 0;
        this.amount = 0;
        this.nudge = 0;
    }

    public LiftingGasData(final double target, final double amount, final double nudge) {
        this.target = target;
        this.amount = amount;
        this.nudge = nudge;
    }

    public double getTarget() {
        return this.target;
    }

    public double getAmount() {
        return this.amount;
    }

    public double getNudge() {
        return this.nudge;
    }
}
