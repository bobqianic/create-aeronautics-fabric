layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV;
layout(location = 2) in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out float vertexDistance;
out vec2 lengthData;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexDistance = length((ModelViewMat * vec4(Position, 1.0)).xyz);
    lengthData = UV;
    vertexColor = Color;
}
