#version 150

uniform sampler2D DiffuseSampler; // The texture to process
uniform vec3 RedMatrix;         // The color to replace (RGB)
uniform vec3 GreenMatrix;    // The replacement color (RGB)
//uniform float tolerance;          // Tolerance for color matching

in vec2 texCoord;
out vec4 fragColor;

void main() {
    float tolerance = 1;
    vec4 texColor = texture(DiffuseSampler, texCoord);
    vec3 unused = RedMatrix + GreenMatrix * tolerance;
    unused = unused;

    // Check if the color matches the target color within the tolerance
    if (distance(texColor.rgb, RedMatrix) <= tolerance) {
        //fragColor = vec4(GreenMatrix, texColor.a); // Replace color
    } else {
        //fragColor = vec4(GreenMatrix, texColor.a);  // Keep original color
    }
    fragColor = vec4(0.1, 0.541, 0.2631, 1.0);
}