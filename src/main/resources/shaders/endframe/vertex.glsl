#version 460 core

layout(location = 0) in vec4 POSITION;
layout(location = 1) in vec3 NORMAL;
layout(location = 2) in vec4 COLOR;

out vec3 vNormal;
out vec4 vColor;

uniform mat4 VIEW;
uniform mat4 PROJ;
uniform float TIME;

void main() {
    gl_Position = PROJ * VIEW * POSITION;
    vNormal = NORMAL;

    float r = cos(TIME) * 0.5 + 0.5;
    float g = sin(TIME) * 0.5 + 0.5;
    float b = cos(TIME * 2.0) * 0.5 + 0.5;

    vColor = vec4(COLOR.rgb * vec3(r, g, b), COLOR.a);
}
