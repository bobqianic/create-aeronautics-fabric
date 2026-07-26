uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D Palette;
uniform sampler2D Dither;
uniform vec4 LineColor;
uniform vec4 LineShadowColor;
uniform float PaletteOffset;
uniform float FadeScale;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

// from https://godotshaders.com/shader/dither-gradient-shader/
const int bit_depth = 32;
const float contrast = 1.8;
const float offset = 0.18f;

vec4 paletted_dither(vec2 screen_sample_uv, vec3 screen_col, sampler2D palette) {
    vec2 screen_size = vec2(textureSize(DiffuseSampler0, 0));

    float luminosity = (screen_col.r * 0.299) + (screen_col.g * 0.587) + (screen_col.b * 0.114);
    luminosity *= 1.7;

    luminosity = (luminosity - 0.5 + offset) * contrast + 0.5;
    luminosity = clamp(luminosity, 0.0, 1.0);

    float bits = float(bit_depth);
    luminosity = floor(luminosity * bits) / bits;

    ivec2 col_sizei = textureSize(palette, 0);
    vec2 col_size = vec2(col_sizei);
    col_size *= 1.5;

    float col_x = float(col_size.x) - 1.0;
    float col_texel_size = 1.0 / col_x;

    luminosity = max(luminosity - 0.00001, 0.0);
    float luminosity_lower = floor(luminosity * col_x) * col_texel_size;
    float luminosity_upper = (floor(luminosity * col_x) + 1.0) * col_texel_size;
    float luminosity_scaled = luminosity * col_x - floor(luminosity * col_x);

    ivec2 dither_size = textureSize(Dither, 0);
    vec2 inv_dither_size = vec2(1.0 / float(dither_size.x), 1.0 / float(dither_size.y));
    vec2 dither_uv = texCoord * inv_dither_size * vec2(float(screen_size.x), float(screen_size.y));
    float threshold = texture(Dither, dither_uv).r;

    threshold = threshold * 0.99 + 0.005;

    float ramp_value = step(threshold, luminosity_scaled);
    float col_sample = mix(luminosity_lower, luminosity_upper, ramp_value);
    return texture(palette, vec2(col_sample, PaletteOffset));
}

vec4 dither_colors(vec2 screen_sample_uv, vec3 screen_col, vec3 start_color, vec3 end_color) {
    vec2 screen_size = vec2(textureSize(DiffuseSampler0, 0));

    float luminosity = (screen_col.r * 0.299) + (screen_col.g * 0.587) + (screen_col.b * 0.114);

    luminosity = (luminosity - 0.5 + offset) * contrast + 0.5;
    luminosity = clamp(luminosity, 0.0, 1.0);

    float bits = float(bit_depth);
    luminosity = floor(luminosity * bits) / bits;

    ivec2 col_size = textureSize(Palette, 0);

    float col_x = float(col_size.x) - 1.0;
    float col_texel_size = 1.0 / col_x;

    luminosity = max(luminosity - 0.00001, 0.0);
    float luminosity_lower = floor(luminosity * col_x) * col_texel_size;
    float luminosity_upper = (floor(luminosity * col_x) + 1.0) * col_texel_size;
    float luminosity_scaled = luminosity * col_x - floor(luminosity * col_x);

    ivec2 dither_size = textureSize(Dither, 0);
    vec2 inv_dither_size = vec2(1.0 / float(dither_size.x), 1.0 / float(dither_size.y));
    vec2 dither_uv = texCoord * inv_dither_size * vec2(float(screen_size.x), float(screen_size.y));
    float threshold = texture(Dither, dither_uv).r;

    threshold = threshold * 0.99 + 0.005;

    float ramp_value = step(threshold, luminosity_scaled);
    float col_sample = mix(luminosity_lower, luminosity_upper, ramp_value);
    return vec4(mix(start_color, end_color, col_sample), 1.0);
}

vec4 outline_color() {
    float outline_fading = 1.04f;
    vec4 center = texture(DiffuseDepthSampler, texCoord);
    vec4 left = texture(DiffuseDepthSampler, texCoord - vec2(oneTexel.x, 0.0));
    vec4 right = texture(DiffuseDepthSampler, texCoord + vec2(oneTexel.x, 0.0));
    vec4 up = texture(DiffuseDepthSampler, texCoord - vec2(0.0, oneTexel.y));
    vec4 down = texture(DiffuseDepthSampler, texCoord + vec2(0.0, oneTexel.y));
    float leftDiff  = pow(abs(center.r - left.r), outline_fading);
    float rightDiff = pow(abs(center.r - right.r), outline_fading);
    float upDiff    = pow(abs(center.r - up.r), outline_fading);
    float downDiff  = pow(abs(center.r - down.r), outline_fading);
    float total = clamp(leftDiff + rightDiff + upDiff + downDiff, 0.0, 1.0);
    float outColor =  center.r + left.r + right.r + up.r + down.r;

    total = min(total * 20.0, 1.0);

    vec4 lineColor = LineColor;
    if (leftDiff > rightDiff) lineColor = LineShadowColor;
    if (upDiff > downDiff) lineColor = LineShadowColor;
    return vec4(lineColor.rgb, total * lineColor.a);
}

float sdBox(in vec2 p, in vec2 b)
{
    vec2 d = abs(p)-b;
    return length(max(d,0.0)) + min(max(d.x,d.y),0.0);
}

float sdScene(vec2 pos, vec2 size) {
    float fade_scale = 1.0 / FadeScale;
    float box = sdBox(pos - (size / 2), size / (vec2(2.3, 2.4)) * fade_scale); // main box
    float box2 = sdBox(pos - vec2(238, 175), vec2(25, 26)); // rotation gizmo
    float box3 = sdBox(pos - vec2(12, 161), vec2(25, 66)); // buttons
    float sub = min(box2, box3);
    return max(-sub, box);
}

void main() {
    float depth = texture(DiffuseDepthSampler, texCoord).r;
    vec3 color = texture(DiffuseSampler0, texCoord).rgb;
    vec4 dither = paletted_dither(texCoord, color, Palette);
    vec4 outline = outline_color();

    vec2 size = textureSize(DiffuseSampler0, 0);
    vec2 tuv = texCoord * size;

    float dist = sdScene(tuv, size);

    // fade out final image
    vec4 fade = vec4(vec3(1.0 - dist * 0.08), 1.0);
    vec4 dither_mask = dither_colors(texCoord, fade.rgb, vec3(0.0), vec3(1.0));

    float alpha = step(depth, 0.99);
    fragColor = vec4(mix(dither.rgb, outline.rgb, outline.a), alpha * dither_mask.r);
}