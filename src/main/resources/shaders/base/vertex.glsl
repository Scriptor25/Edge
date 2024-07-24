#version 460 core

layout(location = 0) in vec4 POSITION;
layout(location = 1) in vec3 NORMAL;
layout(location = 2) in vec4 COLOR;

out vec3 vNormal;
out vec4 vColor;

uniform mat4 VIEW;
uniform mat4 PROJ;

void main() {
    gl_Position = PROJ * VIEW * POSITION;
    vNormal = NORMAL;
    vColor = COLOR;
}
