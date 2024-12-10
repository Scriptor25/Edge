#version 460 core

layout(location = 0) in vec4 POSITION;
layout(location = 2) in vec3 NORMAL;

out vec3 vNormal;
out vec4 vColor;

uniform mat4 VIEW;
uniform mat4 PROJ;
uniform mat4 TRANSFORM;
uniform float TIME;

void main() {
    gl_Position = PROJ * VIEW * TRANSFORM * POSITION;
    vNormal = normalize(mat3(transpose(inverse(TRANSFORM))) * NORMAL);

    float t = modf(round(TIME), 1.0);
    vColor = vec4(t, 0.0, 0.0, 1.0);
}
