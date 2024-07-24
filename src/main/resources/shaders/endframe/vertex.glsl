#version 460 core

layout(location = 0) in vec4 POSITION;
layout(location = 1) in vec3 NORMAL;
layout(location = 2) in vec4 COLOR;

out vec3 vNormal;
out vec4 vColor;

uniform mat4 VIEW;
uniform mat4 PROJ;
uniform float TIME;

float map(float x, float xmin, float xmax, float min, float max) {
    return min + (max - min) * (xmax - x) / (xmax - xmin);
}

void main() {
    gl_Position = PROJ * VIEW * POSITION;
    vNormal = NORMAL;

    float r = map(sin(10.0 * TIME), -1.0, 1.0, 0.8, 1.0);
    float g = map(cos(5.0 * TIME), -1.0, 1.0, 0.8, 1.0);
    float b = map(sin(0.6 * TIME), -1.0, 1.0, 0.8, 1.0);

    vColor = vec4(COLOR.rgb * vec3(r, g, b), COLOR.a);
}
