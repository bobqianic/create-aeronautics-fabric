#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out float vertexDistance;
out vec2 lengthData;
out vec4 vertexColor;

void main() {
    vec4 viewPosition = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPosition;

    vertexDistance = length(viewPosition.xyz);
    lengthData = UV0;
    vertexColor = Color;
}
