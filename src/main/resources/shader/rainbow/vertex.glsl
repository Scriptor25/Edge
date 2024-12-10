#version 460 core

layout(location = 0) in vec4 POSITION;
layout(location = 2) in vec3 NORMAL;

out vec3 vNormal;
out vec4 vColor;

uniform mat4 VIEW;
uniform mat4 PROJ;
uniform mat4 TRANSFORM;
uniform float TIME;

const vec3 PALETTE[] = vec3[](
vec3(0.0, 0.0, 1.0),
vec3(0.0, 0.5, 1.0),
vec3(0.0, 1.0, 1.0),
vec3(0.0, 1.0, 0.5),
vec3(0.0, 1.0, 0.0),
vec3(0.5, 1.0, 0.0),
vec3(1.0, 1.0, 0.0),
vec3(1.0, 0.5, 0.0),
vec3(1.0, 0.0, 0.0),
vec3(1.0, 0.0, 0.5),
vec3(1.0, 0.0, 1.0),
vec3(0.5, 0.0, 1.0)
);

void main() {
    gl_Position = PROJ * VIEW * TRANSFORM * POSITION;
    vNormal = normalize(mat3(transpose(inverse(TRANSFORM))) * NORMAL);

    int index = int(TIME * 10.0) % 12;
    vColor = vec4(PALETTE[index], 1.0);
}
