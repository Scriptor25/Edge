#version 460 core

layout(location = 0) out vec4 COLOR;

in vec3 vPosition;
in vec3 vNormal;

uniform float TIME;
uniform vec3 SUN_DIRECTION;

void main() {
    int time = int(TIME * 2.0) % 5;
    int pos = 4 - (int(vPosition.x - 0.5) % 5);
    vec3 color = vec3((pos == time ? 0.7 : 0.8));

    float t = max(0.1, dot(vNormal, normalize(SUN_DIRECTION)));
    COLOR = vec4(color * t, 1.0);
}
