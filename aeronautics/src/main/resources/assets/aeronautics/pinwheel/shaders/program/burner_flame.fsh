#include aeronautics:util
#include veil:fog

uniform sampler2D FirePalette;
uniform float FlameRenderTime;
uniform float Intensity;
uniform float Palette;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec2 texCoord0;
out vec4 fragColor;

vec3 sample_palette(float x) {
    float i = x + mix(0.9, 0.2, Intensity);
    return texture(FirePalette, vec2(1.0 - i, Palette)).rgb;
}

vec4 main_flame(vec2 uv, float time) {
    vec4 color = vec4(0.0);

    const int count = 6;
    vec2 radius_range = vec2(0.15, 0.4);
    float x_offset_range = 0.05;

    float speed = time * 0.3;

    vec2[count] circle_positions;

    for(int i = 0; i < count; i++) {
        float n = float(i) / float(count);

        float normal_y = mod(n + speed, 1.0);
        int odd = i & 1;
        float x_offset = float(odd) * x_offset_range - x_offset_range / 2.0;
        circle_positions[i] = vec2(x_offset, normal_y);
    }

    // Sort the initialized positions from tip to base. Deriving an array
    // index from normal_y can collide at wrap boundaries (and even address
    // count, one past the end), which leaves undefined values that flicker.
    for (int pass = 0; pass < count - 1; pass++) {
        for (int i = 0; i < count - 1 - pass; i++) {
            if (circle_positions[i].y < circle_positions[i + 1].y) {
                vec2 temporary = circle_positions[i];
                circle_positions[i] = circle_positions[i + 1];
                circle_positions[i + 1] = temporary;
            }
        }
    }

    for(int i = 0; i < count; i++) {
        vec2 circle_position = circle_positions[i];

        vec2 circle_relative_pos = uv - circle_position;
        float radius = mix(radius_range.x, radius_range.y, circle_position.y);
        vec4 circle = simple_circle(circle_relative_pos, radius);
        circle.xyz *= circle_position.y * 0.8;

        if (circle.a > 0.0) {
            // darker as we get closer to horizontal edges
            circle = mix(circle, vec4(1.0), abs(circle_relative_pos.x) / 1.0);
        }

        color = alpha_composite(color, circle);
    }

    return color;
}

// returns vec2 with format (minimum distance to circle, alpha of cutout)
vec2 cutout_circles(vec2 uv, float time) {
    vec4 color = vec4(0.0);

    float intensity = (1.0 - Intensity) * 0.2;

    const int count = 3;
    vec2 radius_range = vec2(0.08, 0.23 + intensity);
    float x_offset_range = 0.1;

    float speed = time * 1.2;
    float minDistance = 5.0;

    for(int i = 0; i < count; i++) {
        float n = float(i) / float(count);

        float normal_y = mod(n * 2.0 + speed, 2.0);
        int index = int(floor((1.0 - normal_y) * count));
        index = min(max(index, 0), count);

        int odd = i & 1;
        float x_offset = float(odd) * x_offset_range - x_offset_range / 2.0;

        vec2 circle_position = vec2(x_offset, normal_y);
        float radius = mix(radius_range.x, radius_range.y, circle_position.y);
        vec2 circle_relative_pos = uv - circle_position;
        vec4 circle = simple_circle(circle_relative_pos, radius);
        minDistance = min(minDistance, length(circle_relative_pos) - radius);

        // if we've hit a cutout circle, we can return 0 as there's no need to go any furher
        if (circle.r >= 1.0) {
            return vec2(0.0, 0.0);
        }
    }

    return vec2(minDistance, 1.0);
}

const float resolution = 32.0;

void main() {
    // Mirror the uv so that +y is up
    vec2 uv = vec2(texCoord0.x, 1.0 - texCoord0.y);
    float time = FlameRenderTime;

    // Misc uv transforms
    uv.x += 1.0 / resolution / 2.0;
    uv = posterize(uv, resolution);

    uv -= vec2(0.5, 0.12);
    uv *= 1.8;

    vec4 color = vec4(0.0);

    // Create flame circles
    vec4 flame = main_flame(uv, time);
    color = alpha_composite(color, flame);

    // Create cutout circles
    vec4 base_circle = simple_circle(uv, 0.15);
    base_circle.rgb = vec3(0.01);
    color = alpha_composite(color, base_circle);

    uv -= vec2(0.3, -0.3);
    vec2 cutout1 = cutout_circles(uv, time);

    uv *= vec2(-1.0, 1.0);
    uv -= vec2(0.6, 0.0);
    vec2 cutout2 = cutout_circles(uv, time + 0.35);

    color.a *= cutout1.y * cutout2.y;
    float minDistance = min(cutout1.x, cutout2.x);

    // burn the edges that are almost touching the cutout circle
    if (color.a > 0.0) {
        float edgeBurnFactor = clamp((1.0 - minDistance - 0.95) * 100.0, 0.0, 1.0);

        color.r = mix(color.r, 1.0, edgeBurnFactor * 0.3);
    }

    if (color.a < 1.0) {
        discard;
    }
    color.rgb = sample_palette(color.r);

    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
