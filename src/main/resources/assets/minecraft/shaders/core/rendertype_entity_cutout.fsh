#version 150

#moj_import <fog.glsl>
#moj_import <palettize.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
//uniform mat4 colors1;
//uniform mat4 colors2;
//uniform mat4 colors3;
//uniform mat4 colors4;
//uniform mat4 colors5;
//uniform mat4 colors6;
//uniform mat4 colors7;
//uniform mat4 colors8;
//uniform mat4 colors9;

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

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }

    if(vertexDistance < 1000 && vertexDistance > 100) {

        vec3 color1 = color.rgb;
        float R = color1.r * 255.0;
        float G = color1.g * 255.0;
        float B = color1.b * 255.0;

        float Y = 0.299 * R + 0.587 * G + 0.114 * B;
        float Z = (R + B) * 0.5;

        const float LOW = 4.0;
        const float HIGH = 256.0;

        /*
        if(B > 10 || G > LOW || R >= HIGH) {
            color *= vertexColor * ColorModulator;
            color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
            color *= lightMapColor;
            fragColor = color;
        }
        else {

            if (R == 91 || R == 92 || R == 93 || R == 94 || R == 95)
            fragColor = vec4(getColor(0), 1.0);
            else if (R == 121 || R == 122 || R == 123 || R == 124 || R == 125)
            fragColor = vec4(getColor(1), 1.0);
            else if (R == 151 || R == 152 || R == 153 || R == 154 || R == 155)
            fragColor = vec4(getColor(2), 1.0);

            else if (R == 96 || R == 97 || R == 98 || R == 99 || R == 100)
            fragColor = vec4(getColor(3), 1.0);
            else if (R == 126 || R == 127 || R == 128 || R == 129 || R == 130)
            fragColor = vec4(getColor(4), 1.0);
            else if (R == 156 || R == 157 || R == 158 || R == 159 || R == 160)
            fragColor = vec4(getColor(5), 1.0);

            else if (R == 101 || R == 102 || R == 103 || R == 104 || R == 105)
            fragColor = vec4(getColor(6), 1.0);
            else if (R == 131 || R == 132 || R == 133 || R == 134 || R == 135)
            fragColor = vec4(getColor(7), 1.0);
            else if (R == 161 || R == 162 || R == 163 || R == 164 || R == 165)
            fragColor = vec4(getColor(8), 1.0);

            else if (R == 106 || R == 107 || R == 108 || R == 109 || R == 110)
            fragColor = vec4(getColor(9), 1.0);
            else if (R == 136 || R == 137 || R == 138 || R == 139 || R == 140)
            fragColor = vec4(getColor(10), 1.0);
            else if (R == 166 || R == 167 || R == 168 || R == 169 || R == 170)
            fragColor = vec4(getColor(11), 1.0);

            else if (R == 111 || R == 112 || R == 113 || R == 114 || R == 115)
            fragColor = vec4(getColor(12), 1.0);
            else if (R == 141 || R == 142 || R == 143 || R == 144 || R == 145)
            fragColor = vec4(getColor(13), 1.0);
            else if (R == 171 || R == 172 || R == 173 || R == 174 || R == 175)
            fragColor = vec4(getColor(14), 1.0);

            else if (R == 116 || R == 117 || R == 118 || R == 119 || R == 120)
            fragColor = vec4(getColor(15), 1.0);
            else if (R == 146 || R == 147 || R == 148 || R == 149 || R == 150)
            fragColor = vec4(getColor(16), 1.0);
            else if (R == 176 || R == 177 || R == 178 || R == 179 || R == 180)
            fragColor = vec4(getColor(17), 1.0);

            else if (R == 181 || R == 182 || R == 183 || R == 184 || R == 185)
            fragColor = vec4(getColor(17), 1.0);

            // Dark colors
            else if (R == 1 || R == 2 || R == 3 || R == 4 || R == 5)
            fragColor = vec4(getColor(18), 1.0);
            else if (R == 31 || R == 32 || R == 33 || R == 34 || R == 35)
            fragColor = vec4(getColor(19), 1.0);
            else if (R == 61 || R == 62 || R == 63 || R == 64 || R == 65)
            fragColor = vec4(getColor(20), 1.0);

            else if (R == 6 || R == 7 || R == 8 || R == 9 || R == 10)
            fragColor = vec4(getColor(21), 1.0);
            else if (R == 36 || R == 37 || R == 38 || R == 39 || R == 40)
            fragColor = vec4(getColor(22), 1.0);
            else if (R == 66 || R == 67 || R == 68 || R == 69 || R == 70)
            fragColor = vec4(getColor(23), 1.0);

            else if (R == 11 || R == 12 || R == 13 || R == 14 || R == 15)
            fragColor = vec4(getColor(24), 1.0);
            else if (R == 41 || R == 42 || R == 43 || R == 44 || R == 45)
            fragColor = vec4(getColor(25), 1.0);
            else if (R == 71 || R == 72 || R == 73 || R == 74 || R == 75)
            fragColor = vec4(getColor(26), 1.0);

            else if (R == 16 || R == 17 || R == 18 || R == 19 || R == 20)
            fragColor = vec4(getColor(27), 1.0);
            else if (R == 46 || R == 47 || R == 48 || R == 49 || R == 50)
            fragColor = vec4(getColor(28), 1.0);
            else if (R == 76 || R == 77 || R == 78 || R == 79 || R == 80)
            fragColor = vec4(getColor(29), 1.0);

            else if (R == 21 || R == 22 || R == 23 || R == 24 || R == 25)
            fragColor = vec4(getColor(30), 1.0);
            else if (R == 51 || R == 52 || R == 53 || R == 54 || R == 55)
            fragColor = vec4(getColor(31), 1.0);
            else if (R == 81 || R == 82 || R == 83 || R == 84 || R == 85)
            fragColor = vec4(getColor(32), 1.0);

            else if (R == 26 || R == 27 || R == 28 || R == 29 || R == 30)
            fragColor = vec4(getColor(33), 1.0);
            else if (R == 56 || R == 57 || R == 58 || R == 59 || R == 60)
            fragColor = vec4(getColor(34), 1.0);
            else if (R == 86 || R == 87 || R == 88 || R == 89 || R == 90)
            fragColor = vec4(getColor(35), 1.0);

            else
            fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);

            //fragColor *= vertexColor * ColorModulator;
            //fragColor.rgb = mix(overlayColor.rgb, fragColor.rgb, overlayColor.a);
            //fragColor *= lightMapColor;
        }
        //fragColor = color;//linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);//vec4(1, 0, 0, 1.0);
        */
        if(B > 10 || G > LOW) {
            color *= vertexColor * ColorModulator;
            color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
            color *= lightMapColor;
            fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
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
                fragColor = vec4(getColor2(int(R - 1)), 1.0);
            }
            else {
                fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
            }
            fragColor *= vertexColor * ColorModulator;
            fragColor.rgb = mix(overlayColor.rgb, fragColor.rgb, overlayColor.a);
            fragColor *= lightMapColor;
        }
    }
    else {
        color *= vertexColor * ColorModulator;
        color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
        color *= lightMapColor;
        fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
    }

}
