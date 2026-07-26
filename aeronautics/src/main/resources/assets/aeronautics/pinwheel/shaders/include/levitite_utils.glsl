uniform vec3 offset;
uniform vec3 linearVelocity;
uniform vec3 angularVelocity;
uniform vec3 sublevelPosition;
uniform mat3 currentOrientation;
uniform float materialTransitionSpeed;
uniform mat3 materialMatrixSlow;
uniform mat3 materialMatrixFast;
vec3 getLocalPosition(vec3 relativePos)
{
    return relativePos - offset;
}
vec3 getVelocity(vec3 pos)
{
    return linearVelocity - cross(pos,angularVelocity);
}
vec3 getGlobalPosition(vec3 localPos)
{
    return currentOrientation*localPos+sublevelPosition;
}
vec3 getDragState(vec3 vel)
{
    float x2 = dot(vel,vel)/(materialTransitionSpeed*materialTransitionSpeed);
    float Q = exp(-1.5*x2);
    float Q2 = -0.446260320297; // = -2*exp(-1.5)

    float h0 = dot(vel,materialMatrixFast*vel);
    float h1 = dot(vel,materialMatrixSlow*vel);
    float h10 = h1 - h0;

    float peak = Q2*h10+h0;

    float D = peak>0 ? ((1-3*x2)*Q*h10+h0)/peak : 0; //derivative of drag function x*(exp(-1.5*x^2/s^2)*h10+h0) normalized to unit magnitude

    D = max(D,0);

    return vec3(Q,1-Q,D);
}