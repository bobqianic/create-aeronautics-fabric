uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform vec2 OutSize;

in vec2 texCoord;

out vec4 fragColor;

const float offset = 2;

void main() {
    vec2 halfpixel = 0.5 / OutSize;

    vec4 ogDepth = texture(DiffuseDepthSampler, texCoord);

    if (ogDepth.r < 1.0) {
        gl_FragDepth = ogDepth.r;
        fragColor = vec4(vec3(1.0), texture(DiffuseSampler0, texCoord).a);
        return;
    }

    vec4 depth1 = (texture(DiffuseDepthSampler, texCoord - vec2(halfpixel.x, 0.0) * offset));
    vec4 depth2 = (texture(DiffuseDepthSampler, texCoord + vec2(halfpixel.x, 0.0) * offset));
    vec4 depth3 = (texture(DiffuseDepthSampler, texCoord + vec2(0.0, halfpixel.y) * offset));
    vec4 depth4 = (texture(DiffuseDepthSampler, texCoord - vec2(0.0, halfpixel.y) * offset));

    vec4 depthSum = vec4(0.0);
    float components = 0.0;
    if (depth1.r < 1.0) {
        depthSum += depth1;
        components += 1.0;
    }
    if (depth2.r < 1.0) {
        depthSum += depth2;
        components += 1.0;
    }
    if (depth3.r < 1.0) {
        depthSum += depth3;
        components += 1.0;
    }
    if (depth4.r < 1.0) {
        depthSum += depth4;
        components += 1.0;
    }

    if (components == 0.0) {
        gl_FragDepth = 1.0;
    } else {
        gl_FragDepth = max(0.0, depthSum.r / components + 0.0005);
    }

    // Now, use the alpha in the fragment texture to represent the "strength".
    // Every pixel we go out, let's 75% the strength.
    // This will give us a nice blur effect.
    vec4 color = texture(DiffuseSampler0, texCoord);
    float existingStrength = color.a;

    float averageStrength = 0.0;

    float strength1 = texture(DiffuseSampler0, texCoord - vec2(halfpixel.x, 0.0) * offset).a;
    float strength2 = texture(DiffuseSampler0, texCoord + vec2(halfpixel.x, 0.0) * offset).a;
    float strength3 = texture(DiffuseSampler0, texCoord + vec2(0.0, halfpixel.y) * offset).a;
    float strength4 = texture(DiffuseSampler0, texCoord - vec2(0.0, halfpixel.y) * offset).a;

    if (depth1.r < 1.0) {
        averageStrength += strength1;
    }
    if (depth2.r < 1.0) {
        averageStrength += strength2;
    }
    if (depth3.r < 1.0) {
        averageStrength += strength3;
    }
    if (depth4.r < 1.0) {
        averageStrength += strength4;
    }

    if (components == 0.0) {
        fragColor = vec4(0.0);
    } else {
        fragColor = vec4(averageStrength / components * 0.85);
    }
}