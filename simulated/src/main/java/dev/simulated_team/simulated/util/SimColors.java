package dev.simulated_team.simulated.util;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.awt.*;

public class SimColors {
    public static int SUCCESS_LIME = new Color(158, 222, 115).getRGB();
    public static int NUH_UH_RED = new Color(255, 113, 113).getRGB();

    public static int REDSTONE_OFF = new Color(86, 1, 1).getRGB();
    public static int REDSTONE_ON = new Color(205, 0, 0).getRGB();
    public static int redstone(final float frac) {
        return com.zurrtum.create.catnip.theme.Color.mixColors(REDSTONE_OFF, REDSTONE_ON, frac);
    }

    public static int ADVANCABLE_GOLD = new Color(219, 162, 19).getRGB();
    public static int EPIC_OURPLE = new Color(165, 0, 170).getRGB();

    // sping
    public static int STRESSED_RED = new Color(235, 50, 48).getRGB();

    // throttle lever
    public static int THROTTLE_VALUE_BROWN = new Color(68, 32, 0).getRGB();

    // mostly for honey glue
    public static int ACTIVE_YELLOW = new Color(255, 235, 133).getRGB();
    public static int PERCHANCE_ORANGE = new Color(255, 201, 102).getRGB();
    public static int DISCARDABLE_ORANGE = new Color(255, 161, 102).getRGB();

    // linked typewriter
    public static int TITLE_DARK_RED = new Color(89, 36, 36).getRGB();
    public static int GROSS_BINDING_BROWN = new Color(183, 60, 45).getRGB(); // why create....

    // altitude sensor ui stuff
    public static int WOODEN_BROWN = new Color(142, 111, 73).getRGB();
    public static int OFF_WHITE = new Color(221, 221, 221).getRGB();

    // laser pointer
    public static int MEDIA_OURPLE = new Color(188, 118, 255).getRGB();

    /**
     * <a href="https://bottosson.github.io/posts/oklab/">Oklab colour space</a>
     * @param lightness 0-1
     * @param chroma 0-1
     * @param hue modular 0-2pi (e.g. 0 == 2pi == 4pi etc)
     * @return a colour constructed from Oklab Lightness, Chroma, Hue
     */
    public static Color LChOklab(final float lightness, final float chroma, final float hue) {
        final double a = chroma * Math.cos(hue);
        final double b = chroma * Math.sin(hue);
        return fromOklab(lightness, (float)a, (float)b);
    }

    /**
     * <a href="https://bottosson.github.io/posts/oklab/">Oklab colour space</a>
     * @param a green/red amount
     * @param b blue/yellow amount
     */
    public static Color fromOklab(final float lightness, final float a, final float b) {
        final float l_ = lightness + 0.3963377774f * a + 0.2158037573f * b;
        final float m_ = lightness - 0.1055613458f * a - 0.0638541728f * b;
        final float s_ = lightness - 0.0894841775f * a - 1.2914855480f * b;

        final float l = l_*l_*l_;
        final float m = m_*m_*m_;
        final float s = s_*s_*s_;

        return new Color(
                Math.clamp(+4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s, 0, 1),
                Math.clamp(-1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s, 0, 1),
                Math.clamp(-0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s, 0, 1)
        );
    }

    /**
     * <a href="https://bottosson.github.io/posts/oklab/">Oklab colour space</a>
     * @return (Lightness, a: green/red, b: blue/yellow)
     */
    public static Vector3d toOklab(final Color c) {
        final float l = 0.4122214708f * c.getRed() + 0.5363325363f * c.getGreen() + 0.0514459929f * c.getBlue();
        final float m = 0.2119034982f * c.getRed() + 0.6806995451f * c.getGreen() + 0.1073969566f * c.getBlue();
        final float s = 0.0883024619f * c.getRed() + 0.2817188376f * c.getGreen() + 0.6299787005f * c.getBlue();

        final double l_ = Math.cbrt(l / 255);
        final double m_ = Math.cbrt(m / 255);
        final double s_ = Math.cbrt(s / 255);

        return new Vector3d(
                0.2104542553f*l_ + 0.7936177850f*m_ - 0.0040720468f*s_,
                1.9779984951f*l_ - 2.4285922050f*m_ + 0.4505937099f*s_,
                0.0259040371f*l_ + 0.7827717662f*m_ - 0.8086757660f*s_
        );
    }
    /**
     * <a href="https://bottosson.github.io/posts/oklab/">Oklab colour space</a>
     * @param Lab (Lightness, a: green/red, b: blue/yellow)
     * @return (Lightness, Chroma, hue)
     */
    public static Vector3d LabToLCh(final Vector3dc Lab) {
        return new Vector3d(
                Lab.x(),
                Math.sqrt(Lab.y()*Lab.y() + Lab.z()*Lab.z()),
                Math.atan2(Lab.y(), Lab.z())
        );
    }
}
