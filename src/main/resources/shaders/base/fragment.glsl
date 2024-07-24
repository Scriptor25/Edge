#version 460 core

layout(location = 0) out vec4 COLOR;

in vec3 vNormal;
in vec4 vColor;

uniform vec3 SUN_DIRECTION;

void main() {
    float t = max(0.1, dot(vNormal, normalize(SUN_DIRECTION)));
    COLOR = vec4(vColor.rgb * t, vColor.a);
}