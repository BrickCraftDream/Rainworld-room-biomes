#version 150

vec4 palettize() {
    return vec4(0.0, 0.0, 0.0, 1.0);
}

vec4 inventory_palettize(float distance) {
    if(distance >= 0) {
        return vec4(0.0, 1.0, 0.0, 1.0);
    }
    else {
        return vec4(0.0, 0.0, 0.0, 1.0);
    }
}

vec4 addPostProcessingToColor(vec4 color, float distance) {
    if(distance >= 500) {
        return vec4(0.25, 0.5, 0.75, 1.0);
    }
    else {
        return color;
    }
}

//0: entity_cutout
//1: entity_cutout_no_cull
//2: entity_cutout_no_cull_z_offset
//3: entity_no_outline
//4: entity_solid
//5: entity_translucent
//6: entity_translucent_cull
//7: glint
//8: glint_translucent
vec4 addPostProcessingToColorType(vec4 color, float distance, int type) {
    float offset = 1000;
    if(type == 0) {
        //if(distance < offset && distance > 100) {
        //    return vec4(1, 0, 0, 1.0);
        //}
        //else {
            return color;
        //}
    }
    else if(type == 1) {
        //if(distance >= offset) {
        //    return vec4(0, 1, 0, 1.0);
        //}
        //else {
            return color;
        //}
    }
    else if(type == 2) {
        //if(distance >= offset) {
        //    return vec4(0, 0, 1, 1.0);
        //}
        //else {
            return color;
        //}
    }
    else if(type == 3) {
        //if(distance >= offset) {
        //    return vec4(1, 1, 0, 1.0);
        //}
        //else {
            return color;
        //}
    }
    else if(type == 4) {
        //if(distance >= offset) {
        //    return vec4(1, 0, 1, 1.0);
        //}
        //else {
            return color;
        //}
    }
    else if(type == 5) {
        //if(distance >= offset) {
        //    return vec4(0, 1, 1, 1.0);
        //}
        //else {
            return color;
        //}
    }
    else if(type == 6) {
        //if(distance >= offset) {
        //    return vec4(1, 1, 1, 1.0);
        //}
        //else {
            return color;
        //}
    }
    else if(type == 7) {
        //if(distance >= offset) {
        //    return vec4(1, 0.5, 0.75, 1.0);
        //}
        //else {
            return color;
        //}
    }
    else if(type == 8) {
        //if(distance >= offset) {
        //    return vec4(0.25, 0.5, 0.75, 1.0);
        //}
        //else {
            return color;
        //}
    }
    else {
        return color;
    }
}