#version 150

in vec4 Position;

uniform mat4 ProjMat;

out vec2 texCoord;

void main() {
    texCoord = Position.xy;
    gl_Position = ProjMat * Position;
}