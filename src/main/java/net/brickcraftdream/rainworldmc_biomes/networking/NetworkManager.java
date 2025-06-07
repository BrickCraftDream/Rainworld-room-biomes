package net.brickcraftdream.rainworldmc_biomes.networking;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes.MOD_ID;

public class NetworkManager {
    public static final ResourceLocation MAIN_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "main_packet");

    public static final ResourceLocation SELECTED_LOCATIONS_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "selected_locations_packet");

    public static final ResourceLocation BIOME_PLACE_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "biome_place_packet");

    public static final ResourceLocation REMOVE_BOX_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "remove_box_packet");

    public static final ResourceLocation CLIENT_BIOME_UPDATE_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "client_biome_update_packet");
    public static final ResourceLocation SERVER_BIOME_UPDATE_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "server_biome_update_packet");

    //Client saves a new biome under a custom name
    //Client triggers an update packet
    //                           -> sent to the server. (ClientBiomeUpdatePacket)
    //                           -> Packet contains the custom name of the biome and the internal name of it, along with the data of the biome (palette, fadepalette, fadestrength, grime, effectcolora, effectcolorb, dangertype)
    //Server receives the packet -> generates a new Image from the biome data, together with the already existing biomes.
    //                           -> adds the biome to the config file
    //Server sends the new Image to all clients.
    //                           -> Clients receive the image and adds it as a dynamic resource
    //Server sends the biome data and the config to all clients.
    //                           -> Clients receive the biome data and changes the appearance of the corresponding biome in the game.
    //                           -> Clients receive the config and store it locally, reapplying the changes each time the game is started.
    public record ClientBiomeUpdatePacket(String customBiomeName, String internalBiomeName, int palette, int fadePalette, float fadeStrength, float grime, int effectColorA, int effectColorB, int dangerType) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ClientBiomeUpdatePacket> ID = new CustomPacketPayload.Type<>(CLIENT_BIOME_UPDATE_PACKET_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientBiomeUpdatePacket> CODEC = StreamCodec.of(
                (buf, packet) -> {
                    ByteBufCodecs.STRING_UTF8.encode(buf, packet.customBiomeName());
                    ByteBufCodecs.STRING_UTF8.encode(buf, packet.internalBiomeName());
                    ByteBufCodecs.INT.encode(buf, packet.palette());
                    ByteBufCodecs.INT.encode(buf, packet.fadePalette());
                    ByteBufCodecs.FLOAT.encode(buf, packet.fadeStrength());
                    ByteBufCodecs.FLOAT.encode(buf, packet.grime());
                    ByteBufCodecs.INT.encode(buf, packet.effectColorA());
                    ByteBufCodecs.INT.encode(buf, packet.effectColorB());
                    ByteBufCodecs.INT.encode(buf, packet.dangerType());
                },
                buf -> new ClientBiomeUpdatePacket(
                        ByteBufCodecs.STRING_UTF8.decode(buf),
                        ByteBufCodecs.STRING_UTF8.decode(buf),
                        ByteBufCodecs.INT.decode(buf),
                        ByteBufCodecs.INT.decode(buf),
                        ByteBufCodecs.FLOAT.decode(buf),
                        ByteBufCodecs.FLOAT.decode(buf),
                        ByteBufCodecs.INT.decode(buf),
                        ByteBufCodecs.INT.decode(buf),
                        ByteBufCodecs.INT.decode(buf)
                )
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }


    public record BiomeUpdatePacket(String customBiomeName, String internalBiomeName,
                                    int palette, int fadePalette, float fadeStrength,
                                    float grime, int effectColorA, int effectColorB,
                                    int dangerType, byte[] imageData)
            implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<BiomeUpdatePacket> ID =
                new CustomPacketPayload.Type<>(SERVER_BIOME_UPDATE_PACKET_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, BiomeUpdatePacket> CODEC = StreamCodec.of(
                (buf, packet) -> {
                    ByteBufCodecs.STRING_UTF8.encode(buf, packet.customBiomeName());
                    ByteBufCodecs.STRING_UTF8.encode(buf, packet.internalBiomeName());
                    ByteBufCodecs.INT.encode(buf, packet.palette());
                    ByteBufCodecs.INT.encode(buf, packet.fadePalette());
                    ByteBufCodecs.FLOAT.encode(buf, packet.fadeStrength());
                    ByteBufCodecs.FLOAT.encode(buf, packet.grime());
                    ByteBufCodecs.INT.encode(buf, packet.effectColorA());
                    ByteBufCodecs.INT.encode(buf, packet.effectColorB());
                    ByteBufCodecs.INT.encode(buf, packet.dangerType());
                    ByteBufCodecs.INT.encode(buf, packet.imageData().length);
                    buf.writeBytes(packet.imageData());
                },
                buf -> {
                    String customName = ByteBufCodecs.STRING_UTF8.decode(buf);
                    String internalName = ByteBufCodecs.STRING_UTF8.decode(buf);
                    int palette = ByteBufCodecs.INT.decode(buf);
                    int fadePalette = ByteBufCodecs.INT.decode(buf);
                    float fadeStrength = ByteBufCodecs.FLOAT.decode(buf);
                    float grime = ByteBufCodecs.FLOAT.decode(buf);
                    int effectColorA = ByteBufCodecs.INT.decode(buf);
                    int effectColorB = ByteBufCodecs.INT.decode(buf);
                    int dangerType = ByteBufCodecs.INT.decode(buf);
                    int imageLength = ByteBufCodecs.INT.decode(buf);
                    byte[] imageData = new byte[imageLength];
                    buf.readBytes(imageData);
                    return new BiomeUpdatePacket(
                            customName, internalName, palette, fadePalette,
                            fadeStrength, grime, effectColorA, effectColorB,
                            dangerType, imageData
                    );
                }
        );

        @Override
        public @NotNull Type<?> type() {
            return ID;
        }
    }



    public record SelectedLocationPayload(GlobalPos firstPos, GlobalPos secondPos, UUID playerName) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SelectedLocationPayload> ID = new CustomPacketPayload.Type<>(SELECTED_LOCATIONS_PACKET_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, SelectedLocationPayload> CODEC = StreamCodec.composite(
                GlobalPos.STREAM_CODEC, SelectedLocationPayload::firstPos,
                GlobalPos.STREAM_CODEC, SelectedLocationPayload::secondPos,
                UUIDUtil.STREAM_CODEC, SelectedLocationPayload::playerName,
                SelectedLocationPayload::new);

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record RemoveBoxPayload(UUID playerName) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RemoveBoxPayload> ID = new CustomPacketPayload.Type<>(REMOVE_BOX_PACKET_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, RemoveBoxPayload> CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, RemoveBoxPayload::playerName,
                RemoveBoxPayload::new);

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record BiomePlacePayload(GlobalPos pos, String biomeNamespace, String biomePath) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<BiomePlacePayload> ID = new CustomPacketPayload.Type<>(BIOME_PLACE_PACKET_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, BiomePlacePayload> CODEC = StreamCodec.composite(
                GlobalPos.STREAM_CODEC, BiomePlacePayload::pos,
                ByteBufCodecs.STRING_UTF8, BiomePlacePayload::biomeNamespace,
                ByteBufCodecs.STRING_UTF8, BiomePlacePayload::biomePath,
                BiomePlacePayload::new);

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record BiomePlacePayload2(List<GlobalPos> pos, String biomeNamespace, String biomePath) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<BiomePlacePayload2> ID = new CustomPacketPayload.Type<>(BIOME_PLACE_PACKET_ID);
        public static final StreamCodec<FriendlyByteBuf, List<GlobalPos>> listCodec = new StreamCodec<>() {
            @Override
            public void encode(FriendlyByteBuf buf, List<GlobalPos> value) {
                buf.writeInt(value.size());
                for (GlobalPos pos : value) {
                    buf.writeGlobalPos(pos);
                }
            }

            @Override
            public @NotNull List<GlobalPos> decode(FriendlyByteBuf buf) {
                int size = buf.readInt();
                List<GlobalPos> positions = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    positions.add(buf.readGlobalPos());
                }
                return positions;
            }
        };


        public static final StreamCodec<RegistryFriendlyByteBuf, BiomePlacePayload2> CODEC = StreamCodec.composite(
                listCodec, BiomePlacePayload2::pos,
                ByteBufCodecs.STRING_UTF8, BiomePlacePayload2::biomeNamespace,
                ByteBufCodecs.STRING_UTF8, BiomePlacePayload2::biomePath,
                BiomePlacePayload2::new);

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}