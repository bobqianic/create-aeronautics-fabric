layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;
layout(location = 3) in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float Scroll;
uniform float CutoffY;

out vec2 texCoord0;
out vec4 vertexColor;
out float vertY;
out float directionless;

const vec3 UP = vec3(0.0, 1.0, 0.0);

float pixelAlign(float a) {
    return floor(a * 16.0) / 16.0;
}

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    directionless = abs(dot(Normal, UP));

    vec2 uv = UV0 + vec2(0.0, pixelAlign(Scroll * (1.0 - directionless * 0.4)));
    if (directionless != 0.0) {
        uv /= vec2(1.0, 20.0);
    }
    texCoord0 = uv;
    vertexColor = Color * vec4(vec3(1.0), clamp(Position.y - CutoffY, 0.0, 1.0));
    vertY = Position.y;
}
