#include veil:color_utilities
#include veil:fog

in float vertexDistance;
in float litFrac;
in vec2 texCoord0;
//in vec2 texCoord1;
in vec4 vertexColor;
in vec4 vertexLight;
out vec4 fragColor;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform sampler2D TextureSheet;

void main() {
    vec4 colA = texture(TextureSheet, texCoord0) * vertexLight;
    vec4 colB = texture(TextureSheet, texCoord0 + vec2(16., 0) / textureSize(TextureSheet, 0));
    vec4 col = mix(colB, colA, litFrac) * vertexColor * ColorModulator;
    fragColor = linear_fog(col, vertexDistance, FogStart, FogEnd, FogColor);
}