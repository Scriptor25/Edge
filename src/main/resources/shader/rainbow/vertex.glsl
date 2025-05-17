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
vec3(0.0, 0.0, 1.0), // red
vec3(0.0, 0.5, 1.0), // orange
vec3(0.0, 1.0, 1.0), // yellow
vec3(0.0, 1.0, 0.5), // lemon
vec3(0.0, 1.0, 0.0), // green
vec3(0.5, 1.0, 0.0), // turqise
vec3(1.0, 1.0, 0.0), // cyan
vec3(1.0, 0.5, 0.0), // dark turqise
vec3(1.0, 0.0, 0.0), // blue
vec3(1.0, 0.0, 0.5), // violett
vec3(1.0, 0.0, 1.0), // pink
vec3(0.5, 0.0, 1.0)
);

void main() {
    gl_Position = PROJ * VIEW * TRANSFORM * POSITION;
    vNormal = normalize(mat3(transpose(inverse(TRANSFORM))) * NORMAL);

    float findex = TIME * 10.0;
    int index = int(findex);
    float t = findex - floor(findex);

    vec3 color_curr = PALETTE[index % PALETTE.length()];
    vec3 color_next = PALETTE[(index + 1) % PALETTE.length()];
    vec3 color = mix(color_curr, color_next, t);

    vColor = vec4(color, 1.0);
}
