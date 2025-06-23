#version 150

uniform sampler2D textureSampler; // The texture to process
uniform vec3 targetColor;         // The color to replace (RGB)
uniform vec3 replacementColor;    // The replacement color (RGB)
uniform float tolerance;          // Tolerance for color matching

in vec2 fragUV;
out vec4 fragColor;

void main() {
    vec4 texColor = texture(textureSampler, fragUV);

    // Check if the color matches the target color within the tolerance
    if (distance(texColor.rgb, targetColor) <= tolerance) {
        fragColor = vec4(replacementColor, texColor.a); // Replace color
    } else {
        //fragColor = texColor; // Keep original color
        fragColor = vec4(replacementColor, texColor.a);
    }
}