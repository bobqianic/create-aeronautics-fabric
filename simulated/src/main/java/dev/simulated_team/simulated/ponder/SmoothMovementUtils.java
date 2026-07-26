package dev.simulated_team.simulated.ponder;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;

public class SmoothMovementUtils {

    public static FloatUnaryOperator cubicSmoothing() {
        return t -> t * t * (3 - 2 * t);
    }

    public static FloatUnaryOperator linear() {
        return t -> t;
    }

    public static FloatUnaryOperator quinticSmoothing() {
        return t -> t * t * t * (10 - 3 * t * (5 - 2 * t));
    }

    public static FloatUnaryOperator quadraticJump() {
        return t -> 4f * t * (1f - t);
    }

    public static FloatUnaryOperator quadraticRise() {
        return t -> t * t;
    }

    public static FloatUnaryOperator quadraticRiseDual() {
        return t -> t * (2 - t);
    }

    public static FloatUnaryOperator quadraticRiseInOut() {
        return t -> t < 0.5 ? 2 * t * t : 2 * t * (2 - t) - 1;
    }

    public static FloatUnaryOperator quadraticRiseOut() {
        return t -> t * (2 - t);
    }

    public static FloatUnaryOperator elasticOut() {
        final double c4 = (2 * Math.PI) / 3;
        return t -> (float) (Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * c4) + 1);
    }

    public static FloatUnaryOperator softElasticOut() {
        final double c4 = (2 * Math.PI) / 3;
        return t -> t < 0.5 ? 2 * t: ((float) (Math.pow(2, -10 * t) * Math.sin((t * Math.pow(t + 1, 5) - 0.75) * c4) + 1));
    }

    public static FloatUnaryOperator cubicRise() {
        return t -> t * t * t;
    }

    public static FloatUnaryOperator asymptoticAcceleration(final float smoothing) {
        return t -> (float) ((t * smoothing + Math.exp(-smoothing * t) - 1f) / (smoothing + Math.exp(-smoothing) - 1f));
    }
}