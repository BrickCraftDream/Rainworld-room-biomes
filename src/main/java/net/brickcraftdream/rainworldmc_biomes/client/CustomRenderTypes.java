package net.brickcraftdream.rainworldmc_biomes.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalDouble;

public class CustomRenderTypes extends RenderType {

    private CustomRenderTypes(String name,
                              VertexFormat format,
                              VertexFormat.Mode mode,
                              int bufferSize,
                              boolean affectsCrumbling,
                              boolean sortOnUpload,
                              Runnable setupState,
                              Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType translucent(ResourceLocation texture) {
        return RenderType.create(
                "rendertype_" + texture.getPath(),
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS,
                256,
                true,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(ShaderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)       // vanilla translucent shader
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TransparencyStateShard.TRANSLUCENT_TRANSPARENCY)      // alpha blending
                        .setCullState(NO_CULL)                               // disable culling
                        .setLightmapState(LIGHTMAP)                          // enable lightmap
                        .setOverlayState(OVERLAY)                            // enable overlay
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)     // only write color, not depth
                        .setOutputState(OutputStateShard.TRANSLUCENT_TARGET)
                        .createCompositeState(true)
        );
    }

    public static RenderType glass(ResourceLocation texture) {
        return RenderType.create(
                "quad_lines",
                DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.QUADS,
                1536,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_LINES_SHADER)
                        .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false)
        );
    }
}
