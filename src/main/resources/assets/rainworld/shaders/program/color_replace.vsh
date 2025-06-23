#version 150

in vec3 Position;
in vec2 UV;

out vec2 fragUV;

void main() {
    fragUV = UV;
    gl_Position = vec4(Position, 1.0);
}