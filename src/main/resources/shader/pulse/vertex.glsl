#version 460 core

layout(location = 0) in vec4 POSITION;
layout(location = 2) in vec3 NORMAL;

out vec3 vPosition;
out vec3 vNormal;

uniform mat4 VIEW;
uniform mat4 PROJECTION;
uniform mat4 TRANSFORM;

void main() {
    gl_Position = PROJECTION * VIEW * TRANSFORM * POSITION;
    vPosition = POSITION.xyz;
    vNormal = normalize(mat3(transpose(inverse(TRANSFORM))) * NORMAL);
}
