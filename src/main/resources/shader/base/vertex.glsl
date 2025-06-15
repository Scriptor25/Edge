#version 460 core

layout(location = 0) in vec4 POSITION;
layout(location = 2) in vec3 NORMAL;
layout(location = 3) in vec4 COLOR;

out vec3 vPosition;
out vec3 vNormal;
out vec4 vColor;

uniform mat4 VIEW;
uniform mat4 PROJECTION;
uniform mat4 TRANSFORM;

void main() {
    gl_Position = PROJECTION * VIEW * TRANSFORM * POSITION;

    vPosition = gl_Position.xyz / gl_Position.w;
    vNormal = normalize(mat3(transpose(inverse(TRANSFORM))) * NORMAL);
    vColor = COLOR;
}
