vec2 posterize(vec2 color, float colors) {
    return floor(color * colors) / colors;
}

vec3 posterize(vec3 color, float colors) {
    return floor(color * colors) / colors;
}

vec4 simple_circle(vec2 uv, float radius) {
    float dist = length(uv) - radius;
    float mask = step(dist, 0.0);
    return vec4(mask);
}

// slightly modified from https://github.com/riccardoscalco/glsl-simple-alpha-compositing/blob/master/index.glsl
vec4 alpha_composite(vec4 background, vec4 foreground) {
    return foreground * foreground.a + background * background.a * (1.0 - foreground.a);
}