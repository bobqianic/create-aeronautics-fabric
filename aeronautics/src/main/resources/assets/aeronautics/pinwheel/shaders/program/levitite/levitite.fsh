#include veil:fog
#include veil:color

uniform sampler2D Sampler0;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D Noise;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform mat4 ProjMat;
uniform vec2 ScreenSize;

uniform float time;
uniform int onSublevel;
uniform int layerIndex;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in float ghostLayerFullness;
in float ghostNoiseMagnitude;
in float depth;

out vec4 fragColor;

float linearizeDepth(float s) {
    // Same calculation mojang does, to linearize depths using the projection matrix values
    return -ProjMat[3].z /  (s * -2.0 + 1.0 - ProjMat[2].z);
}

vec3 getGhostColorHsv(vec3 hsv, bool forwards)
{
    hsv += forwards ? vec3(-0.45, 0, 0) : vec3(0.25, 0, 0);
    hsv.x = mod(hsv.x+1, 1);
    hsv.z = pow(hsv.z, 0.8);
    return hsv;
}

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    vec3 hsv = rgb2hsv(color.rgb);
    if (layerIndex == 0)
    {
        float s = exp(-ghostNoiseMagnitude);
        hsv.z = pow(hsv.z, s);
        hsv.y = pow(hsv.y, s);
        color.rgb = hsv2rgb(hsv);
    }

    if (onSublevel > 0)
    {
        float ghostAlpha = ghostLayerFullness * 0.3;
        if (layerIndex == 0)
        {
            vec3 ghostTotal = (hsv2rgb(getGhostColorHsv(hsv, false))+hsv2rgb(getGhostColorHsv(hsv, true))).rgb;
            color.rgb -= ghostTotal * ghostAlpha * 0.2;
        } else
        {
            hsv = getGhostColorHsv(hsv, layerIndex < 0);
            color.rgb = hsv2rgb(hsv);

            float depthSample = texture(DiffuseDepthSampler, gl_FragCoord.xy / ScreenSize).r;
            float depth2 = linearizeDepth(depthSample);

            float fade = smoothstep(-2, 4, (depth2-depth)*16);
            if (fade < 0.001)
            discard;

            color.w *= ghostAlpha*fade;
        }
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}