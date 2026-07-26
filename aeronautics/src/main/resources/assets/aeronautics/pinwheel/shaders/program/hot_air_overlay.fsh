uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform vec4 ColorModulator;
uniform float Scroll;

in vec2 texCoord0;
in vec4 vertexColor;
in float vertY;
in float directionless;

out vec4 fragColor;

void main() {
    vec2 uv = texCoord0;
    vec4 color;

    if (directionless > 0) {
        color = texture(Sampler1, uv) * vertexColor;
        if (color.a < 0.01) {
            discard;
        }
        fragColor = color * ColorModulator;
        return;
    }

    color = texture(Sampler0, uv) * vertexColor;
    if (color.a < 0.01) {
        discard;
    }

    fragColor = color * ColorModulator;
}
