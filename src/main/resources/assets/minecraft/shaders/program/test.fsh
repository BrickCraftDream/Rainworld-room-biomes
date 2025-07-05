#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;
in vec3 fragNormal;

uniform vec2 InSize;

uniform vec3 Gray;
uniform vec3 RedMatrix;
uniform vec3 GreenMatrix;
uniform vec3 BlueMatrix;
uniform vec3 Offset;
uniform vec3 ColorScale;
uniform float Saturation;
uniform mat4 colors1;
uniform mat4 colors2;
uniform mat4 colors3;
uniform mat4 colors4;
uniform mat4 colors5;
uniform mat4 colors6;
uniform mat4 colors7;
uniform mat4 colors8;
uniform mat4 colors9;

uniform mat4 data1;
uniform mat4 data2;
uniform mat4 data3;
uniform mat4 data4;
uniform mat4 data5;
uniform mat4 data6;
uniform mat4 data7;
uniform mat4 data8;
uniform mat4 data9;
uniform mat4 data10;
uniform mat4 data11;
uniform mat4 data12;
uniform mat4 data13;
uniform mat4 data14;
uniform mat4 data15;
uniform mat4 data16;
uniform mat4 data17;
uniform mat4 data18;
uniform mat4 data19;
uniform mat4 data20;
uniform mat4 data21;
uniform mat4 data22;
uniform mat4 data23;
uniform mat4 data24;
uniform mat4 data25;
uniform mat4 data26;
uniform mat4 data27;
uniform mat4 data28;
uniform mat4 data29;
uniform mat4 data30;
uniform mat4 data31;
uniform mat4 data32;
uniform mat4 data33;
uniform mat4 data34;
uniform mat4 data35;
uniform mat4 data36;
uniform mat4 data37;
uniform mat4 data38;
uniform mat4 data39;
uniform mat4 data40;
uniform mat4 data41;
uniform mat4 data42;
uniform mat4 data43;
uniform mat4 data44;
uniform mat4 data45;


vec3 getColor2(int index) {
    if (index < 4) return data1[index].rgb;
    else if (index < 8) return data2[index - 4].rgb;
    else if (index < 12) return data3[index - 8].rgb;
    else if (index < 16) return data4[index - 12].rgb;
    else if (index < 20) return data5[index - 16].rgb;
    else if (index < 24) return data6[index - 20].rgb;
    else if (index < 28) return data7[index - 24].rgb;
    else if (index < 32) return data8[index - 28].rgb;
    else if (index < 36) return data9[index - 32].rgb;
    else if (index < 40) return data10[index - 36].rgb;
    else if (index < 44) return data11[index - 40].rgb;
    else if (index < 48) return data12[index - 44].rgb;
    else if (index < 52) return data13[index - 48].rgb;
    else if (index < 56) return data14[index - 52].rgb;
    else if (index < 60) return data15[index - 56].rgb;
    else if (index < 64) return data16[index - 60].rgb;
    else if (index < 68) return data17[index - 64].rgb;
    else if (index < 72) return data18[index - 68].rgb;
    else if (index < 76) return data19[index - 72].rgb;
    else if (index < 80) return data20[index - 76].rgb;
    else if (index < 84) return data21[index - 80].rgb;
    else if (index < 88) return data22[index - 84].rgb;
    else if (index < 92) return data23[index - 88].rgb;
    else if (index < 96) return data24[index - 92].rgb;
    else if (index < 100) return data25[index - 96].rgb;
    else if (index < 104) return data26[index - 100].rgb;
    else if (index < 108) return data27[index - 104].rgb;
    else if (index < 112) return data28[index - 108].rgb;
    else if (index < 116) return data29[index - 112].rgb;
    else if (index < 120) return data30[index - 116].rgb;
    else if (index < 124) return data31[index - 120].rgb;
    else if (index < 128) return data32[index - 124].rgb;
    else if (index < 132) return data33[index - 128].rgb;
    else if (index < 136) return data34[index - 132].rgb;
    else if (index < 140) return data35[index - 136].rgb;
    else if (index < 144) return data36[index - 140].rgb;
    else if (index < 148) return data37[index - 144].rgb;
    else if (index < 152) return data38[index - 148].rgb;
    else if (index < 156) return data39[index - 152].rgb;
    else if (index < 160) return data40[index - 156].rgb;
    else if (index < 164) return data41[index - 160].rgb;
    else if (index < 168) return data42[index - 164].rgb;
    else if (index < 172) return data43[index - 168].rgb;
    else if (index < 176) return data44[index - 172].rgb;
    else return data45[index - 176].rgb;
}

vec4 fragColor;

out vec4 pixelColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);

    float tolerance = 0.01;
    if (distance(color.rgb, RedMatrix) <= tolerance) {
        fragColor = vec4(GreenMatrix, 1.0);
    } else {
        vec4 finalColor = vec4(color.rgb + (GreenMatrix / 10000000) + (BlueMatrix / 100000000), 1.0);
        fragColor = finalColor;
    }

    vec3 color1 = color.rgb;
    float R = color1.r * 255.0;
    float G = color1.g * 255.0;
    float B = color1.b * 255.0;

    float Y = 0.299 * R + 0.587 * G + 0.114 * B;
    float Z = (R + B) * 0.5;

    const float LOW = 4.0;

    if (B > 10 || G > LOW) {
        fragColor = color;
    }
    else {
        //if(R >= 149 && R <= 177) {
        //    fragColor = vec4(getColor2(int(R + 1)), 1.0);
        //}
        //else if (R >= 119 && R <= 148) {
        //    fragColor = vec4(getColor2(int(R + 1)), 1.0);
        //}
        //else if (R >= 90 && R <= 118) {
        //    fragColor = vec4(getColor2(int(R + 1)), 1.0);
        //}
        //else if (R >= 1 && R <= 30) {
        //    fragColor = vec4(getColor2(int(R - 1)), 1.0);
        //}
         if (R < 186){
            fragColor = vec4(getColor2(int(R)), 1.0);
        }
        else {
            fragColor = color;
        }
    }

    // I HATE to do this, but compiler optimization is a crybaby that doesn't like anything that isn't contributing to the final result
    pixelColor = fragColor + vec4((color.rgb / 100000000) + (RedMatrix / 10000000) + (GreenMatrix / 10000000) + (BlueMatrix / 100000000) + (colors7[0].rgb / 1000000) + (data35[0].rgb / 100000) + (data36[0].rgb / 100000) + (data37[0].rgb / 100000) + (data38[0].rgb / 100000) + (data39[0].rgb / 100000) + (data40[0].rgb / 100000) + (data41[0].rgb / 100000) + (data42[0].rgb / 100000) + (data43[0].rgb / 100000) + (data44[0].rgb / 100000) + (data45[0].rgb / 100000), 0.0);// + vec4(colors1[0] / 1000000 + colors2[0] / 1000000 + colors3[0] / 1000000 + colors4[0] / 1000000 + colors5[0] / 1000000 + colors6[0] / 1000000 + colors7[0] / 1000000 + colors8[0] / 1000000 + colors9[0] / 1000000, 1.0);
}