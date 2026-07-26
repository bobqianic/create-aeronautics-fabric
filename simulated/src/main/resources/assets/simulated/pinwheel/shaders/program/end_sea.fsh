uniform float ShadowVolumeSize;
uniform float StartY;
uniform sampler2D SkySampler;
uniform sampler2D ShadowDepthSampler;
uniform sampler2D ShadowStrengthSampler;

in vec2 texCoord;
in vec2 shadowTexCoord;
in vec4 vertexColor;
in float planeY;

out vec4 fragColor;

void main() {
    vec4 color = texture(SkySampler, texCoord);
    float depth = texture(ShadowDepthSampler, shadowTexCoord).r;
    float shadowStrength = texture(ShadowStrengthSampler, shadowTexCoord).a;
    float fragY = (StartY - ShadowVolumeSize) + (depth) * (ShadowVolumeSize - 0.5) + 0.5;

    if (depth < 1.0) {
        vec4 col = (vertexColor * color + vertexColor * 0.025);

        if (fragY < planeY) {
            col.a = 0.0;
        } else {
            float strength = 1.0 - (fragY - planeY) / 10.0;
            strength = clamp(strength, 0.0, 1.0);
            strength = pow(strength, 3.0);
            col.a = max(0.0, col.a * strength) * shadowStrength;
        }

        fragColor = col;
    } else {
        discard;
    }
}
