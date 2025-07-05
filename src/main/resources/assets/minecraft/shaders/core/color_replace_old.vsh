#version 150

in vec4 Position;

uniform mat4 ProjMat;

out vec2 fragUV;

void main() {
    fragUV = Position.xy;
    gl_Position = ProjMat * Position;
}