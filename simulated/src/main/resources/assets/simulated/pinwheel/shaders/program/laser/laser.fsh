#include veil:fog

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;

in float vertexDistance;
// x always     (length + 0.5) / length
// y ranges [0, (length + 0.5) / length]
in vec2 lengthData;
in vec4 vertexColor;

out vec4 fragColor;

// #veil:normal
const vec3 normal = vec3(0.0, 0.0, -1.0);

void main() {
    // #veil:albedo
    float endTaper = (lengthData.x - 1.0) / (lengthData.y - 1.0);
    vec4 color = vertexColor;
    color.a *= (1.0 - max(endTaper, 0.0));

    fragColor = color * ColorModulator * linear_fog_fade(vertexDistance, FogStart, FogEnd);
}