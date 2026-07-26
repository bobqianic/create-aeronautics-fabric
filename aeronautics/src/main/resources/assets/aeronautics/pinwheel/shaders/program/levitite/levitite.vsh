#include veil:fog
#include veil:light
#include aeronautics:levitite_utils

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV2;
layout(location = 4) in vec3 Normal;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform int FogShape;

uniform float SableEnableNormalLighting;
uniform float SableSkyLightScale;
uniform mat3 NormalMat;

uniform float time;
uniform int onSublevel;
uniform int layerIndex;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

void main() {

    vec3 pos = Position + ChunkOffset;
    if(onSublevel > 0)
    {
        vec3 localVelocity = getVelocity(pos - offset);
        float l = length(localVelocity);
        localVelocity *= (1-exp(-l/materialTransitionSpeed))/(l+0.001);
        pos += localVelocity * onSublevel * layerIndex * 0.15;
    }
    gl_Position = vec4(pos, 1.0);

    vertexDistance = fog_distance(pos, FogShape);
    vertexColor = Color * minecraft_sample_lightmap(Sampler2, ivec2(UV2 * vec2(1.0, SableSkyLightScale)));
    vertexColor.rgb *= mix(vec3(1.0), vec3(block_brightness(inverse(NormalMat) * (ModelViewMat * vec4(Normal, 0.0)).xyz)), SableEnableNormalLighting);

    texCoord0 = UV0;
}