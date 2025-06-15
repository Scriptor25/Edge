#version 460 core

layout(location = 0) in vec4 POSITION;

uniform mat4 VIEW;
uniform mat4 PROJECTION;
uniform mat4 TRANSFORM;

void main() {
    gl_Position = PROJECTION * VIEW * TRANSFORM * POSITION;
}
