#veil:buffer veil:camera VeilCamera

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV2;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;
uniform int FogShape;
uniform sampler2D Sampler2;
uniform float CameraY;

out vec2 texCoord;
out vec2 shadowTexCoord;
out vec4 vertexColor;
out float planeY;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    texCoord = UV0;
    shadowTexCoord = UV2;
    planeY = Position.y + VeilCamera.CameraPosition.y;
}
