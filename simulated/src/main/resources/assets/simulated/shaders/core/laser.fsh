#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:fog.glsl>

in float vertexDistance;
in vec2 lengthData;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float endTaper = (lengthData.x - 1.0) / (lengthData.y - 1.0);
    vec4 color = vertexColor;
    color.a *= 1.0 - max(endTaper, 0.0);

    float environmentalFog = linear_fog_value(vertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd);
    float renderDistanceFog = linear_fog_value(vertexDistance, FogRenderDistanceStart, FogRenderDistanceEnd);
    float fogFade = 1.0 - max(environmentalFog, renderDistanceFog);
    fragColor = color * ColorModulator * fogFade;
}
