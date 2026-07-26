#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:fog.glsl>

uniform sampler2D Sampler0;

in float vertexDistance;
in vec2 texCoord0;
flat in vec3 flameData;

out vec4 fragColor;

const float FLAME_ANIMATION_PERIOD = 10.0 / 3.0;
const float RESOLUTION = 32.0;

vec2 posterize(vec2 color, float colors) {
    return floor(color * colors) / colors;
}

vec4 simple_circle(vec2 uv, float radius) {
    float distanceToEdge = length(uv) - radius;
    float mask = step(distanceToEdge, 0.0);
    return vec4(mask);
}

vec4 alpha_composite(vec4 background, vec4 foreground) {
    return foreground * foreground.a
        + background * background.a * (1.0 - foreground.a);
}

vec3 sample_palette(float x, float intensity, float palette) {
    float index = x + mix(0.9, 0.2, intensity);
    return texture(Sampler0, vec2(1.0 - index, palette)).rgb;
}

vec4 main_flame(vec2 uv, float time) {
    vec4 color = vec4(0.0);

    const int count = 6;
    vec2 radiusRange = vec2(0.15, 0.4);
    float xOffsetRange = 0.05;
    float speed = time * 0.3;
    vec2 circlePositions[count];

    for (int i = 0; i < count; i++) {
        float n = float(i) / float(count);
        float normalY = mod(n + speed, 1.0);
        int odd = i & 1;
        float xOffset = float(odd) * xOffsetRange - xOffsetRange / 2.0;
        circlePositions[i] = vec2(xOffset, normalY);
    }

    // Keep every slot initialized, then preserve the original tip-to-base
    // compositing order. Calculating an array index from normalY can produce
    // duplicate indices at wrap boundaries due to floating-point rounding,
    // leaving undefined positions that flash elsewhere on the billboard.
    for (int pass = 0; pass < count - 1; pass++) {
        for (int i = 0; i < count - 1 - pass; i++) {
            if (circlePositions[i].y < circlePositions[i + 1].y) {
                vec2 temporary = circlePositions[i];
                circlePositions[i] = circlePositions[i + 1];
                circlePositions[i + 1] = temporary;
            }
        }
    }

    for (int i = 0; i < count; i++) {
        vec2 circlePosition = circlePositions[i];
        vec2 circleRelativePos = uv - circlePosition;
        float radius = mix(radiusRange.x, radiusRange.y, circlePosition.y);
        vec4 circle = simple_circle(circleRelativePos, radius);
        circle.xyz *= circlePosition.y * 0.8;

        if (circle.a > 0.0) {
            circle = mix(
                circle,
                vec4(1.0),
                abs(circleRelativePos.x)
            );
        }

        color = alpha_composite(color, circle);
    }

    return color;
}

vec2 cutout_circles(vec2 uv, float time, float intensity) {
    float intensityOffset = (1.0 - intensity) * 0.2;

    const int count = 3;
    vec2 radiusRange = vec2(0.08, 0.23 + intensityOffset);
    float xOffsetRange = 0.1;
    float speed = time * 1.2;
    float minDistance = 5.0;

    for (int i = 0; i < count; i++) {
        float n = float(i) / float(count);
        float normalY = mod(n * 2.0 + speed, 2.0);
        int odd = i & 1;
        float xOffset = float(odd) * xOffsetRange - xOffsetRange / 2.0;

        vec2 circlePosition = vec2(xOffset, normalY);
        float radius = mix(radiusRange.x, radiusRange.y, circlePosition.y);
        vec2 circleRelativePos = uv - circlePosition;
        vec4 circle = simple_circle(circleRelativePos, radius);
        minDistance = min(
            minDistance,
            length(circleRelativePos) - radius
        );

        if (circle.r >= 1.0) {
            return vec2(0.0, 0.0);
        }
    }

    return vec2(minDistance, 1.0);
}

void main() {
    float time = flameData.r * FLAME_ANIMATION_PERIOD;
    float intensity = flameData.g;
    float palette = flameData.b;

    vec2 uv = vec2(texCoord0.x, 1.0 - texCoord0.y);
    uv.x += 1.0 / RESOLUTION / 2.0;
    uv = posterize(uv, RESOLUTION);
    uv -= vec2(0.5, 0.12);
    uv *= 1.8;

    vec4 color = main_flame(uv, time);
    vec4 baseCircle = simple_circle(uv, 0.15);
    baseCircle.rgb = vec3(0.01);
    color = alpha_composite(color, baseCircle);

    uv -= vec2(0.3, -0.3);
    vec2 cutout1 = cutout_circles(uv, time, intensity);

    uv *= vec2(-1.0, 1.0);
    uv -= vec2(0.6, 0.0);
    vec2 cutout2 = cutout_circles(uv, time + 0.35, intensity);

    color.a *= cutout1.y * cutout2.y;
    float minDistance = min(cutout1.x, cutout2.x);

    if (color.a > 0.0) {
        float edgeBurnFactor = clamp(
            (1.0 - minDistance - 0.95) * 100.0,
            0.0,
            1.0
        );
        color.r = mix(color.r, 1.0, edgeBurnFactor * 0.3);
    }

    if (color.a < 1.0) {
        discard;
    }

    color.rgb = sample_palette(color.r, intensity, palette);
    color *= ColorModulator;

    float environmentalFog = linear_fog_value(
        vertexDistance,
        FogEnvironmentalStart,
        FogEnvironmentalEnd
    );
    float renderDistanceFog = linear_fog_value(
        vertexDistance,
        FogRenderDistanceStart,
        FogRenderDistanceEnd
    );
    float fogFade = 1.0 - max(environmentalFog, renderDistanceFog);
    fragColor = vec4(color.rgb * fogFade, color.a);
}
