#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out float vertexDistance;
out vec2 texCoord0;
flat out vec3 flameData;

void main() {
    vec4 viewPosition = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPosition;

    vertexDistance = length(viewPosition.xyz);
    texCoord0 = UV0;
    flameData = Color.rgb;
}
