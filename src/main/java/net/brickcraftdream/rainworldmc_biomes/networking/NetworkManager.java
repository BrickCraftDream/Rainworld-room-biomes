package net.brickcraftdream.rainworldmc_biomes.networking;

import com.google.gson.JsonElement;
import io.netty.buffer.Unpooled;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import static net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes.MOD_ID;

public class NetworkManager {
    public static final ResourceLocation MAIN_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "main_packet");

    public static final ResourceLocation SELECTED_LOCATIONS_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "selected_locations_packet");

    public static final ResourceLocation BIOME_PLACE_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "biome_place_packet");

    public static final ResourceLocation REMOVE_BOX_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "remove_box_packet");

    public static final ResourceLocation CLIENT_BIOME_UPDATE_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "client_biome_update_packet");
    public static final ResourceLocation SERVER_BIOME_UPDATE_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "server_biome_update_packet");

    public static final ResourceLocation BIOME_UPDATE_DATA_REQUEST_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "biome_update_data_request_packet");

    public static final ResourceLocation BIOME_SYNC_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "biome_sync_packet");

    public static final ResourceLocation BIOME_CACHE_UPDATE_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "biome_cache_update_packet");

    public static final ResourceLocation BIOME_SYNC_FROM_CLIENT_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "biome_sync_from_client_packet");

    public static final ResourceLocation I_ASK_FOR_THE_SHADER_DATA_CUS_I_DONT_HAVE_IT_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "i_ask_for_the_shader_data_cus_i_dont_have_it_packet");

    public static final ResourceLocation A_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "a_packet_id");

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

    public record HeyTheBiomeIsPlacedYouCanDiscardYourSelectionPacket(String handshake) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<HeyTheBiomeIsPlacedYouCanDiscardYourSelectionPacket> ID = new CustomPacketPayload.Type<>(A_PACKET_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, HeyTheBiomeIsPlacedYouCanDiscardYourSelectionPacket> CODEC = StreamCodec.of(
                (buf, packet) -> ByteBufCodecs.STRING_UTF8.encode(buf, packet.handshake),
                buf -> new HeyTheBiomeIsPlacedYouCanDiscardYourSelectionPacket(ByteBufCodecs.STRING_UTF8.decode(buf))
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record BiomeSyncFromClientInitializationFromServerPacket(String handshake) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<BiomeSyncFromClientInitializationFromServerPacket> ID = new CustomPacketPayload.Type<>(I_ASK_FOR_THE_SHADER_DATA_CUS_I_DONT_HAVE_IT_PACKET_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, BiomeSyncFromClientInitializationFromServerPacket> CODEC = StreamCodec.of(
                (buf, packet) -> ByteBufCodecs.STRING_UTF8.encode(buf, packet.handshake),
                buf -> new BiomeSyncFromClientInitializationFromServerPacket(ByteBufCodecs.STRING_UTF8.decode(buf))
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record BiomeSyncFromClientPacket(byte[] imageData) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<BiomeSyncFromClientPacket> ID = new CustomPacketPayload.Type<>(BIOME_SYNC_FROM_CLIENT_PACKET_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, BiomeSyncFromClientPacket> CODEC = StreamCodec.of(
                (buf, packet) -> {
                    byte[] imageData = packet.imageData;
                    if(imageData == null) {
                        imageData = new byte[0];
                    }
                    buf.writeInt(imageData.length);
                    buf.writeBytes(imageData);
                },
                buf -> {
                    int imageLength = ByteBufCodecs.INT.decode(buf);
                    byte[] imageData = new byte[imageLength];
                    buf.readBytes(imageData);
                    return new BiomeSyncFromClientPacket(imageData);
                }
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record BiomeCacheUpdatePacket(ResourceKey<Biome> key) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<BiomeCacheUpdatePacket> ID = new CustomPacketPayload.Type<>(BIOME_CACHE_UPDATE_PACKET_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, BiomeCacheUpdatePacket> CODEC = StreamCodec.of(
                (buf, packet) -> buf.writeResourceKey(packet.key),
                buf -> new BiomeCacheUpdatePacket(buf.readResourceKey(Registries.BIOME))
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record BiomeSyncPacket(JsonElement configData, byte[] imageData) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<BiomeSyncPacket> ID = new CustomPacketPayload.Type<>(BIOME_SYNC_PACKET_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, BiomeSyncPacket> CODEC = StreamCodec.of(
                (buf, packet) ->  {
                    byte[] imageData = packet.imageData;
                    if(imageData == null) {
                        imageData = new byte[0];
                    }
                    buf.writeJsonWithCodec(ExtraCodecs.JSON, packet.configData);
                    buf.writeInt(imageData.length);
                    buf.writeBytes(imageData);
                },
                buf -> {
                    JsonElement json = buf.readJsonWithCodec(ExtraCodecs.JSON);
                    int imageLength = ByteBufCodecs.INT.decode(buf);
                    byte[] imageData = new byte[imageLength];
                    buf.readBytes(imageData);
                    return new BiomeSyncPacket(json, imageData);
                }
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record BiomeUpdateDataRequestPacket(List<String> biomeNames, byte[] imageData) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<BiomeUpdateDataRequestPacket> ID = new CustomPacketPayload.Type<>(BIOME_UPDATE_DATA_REQUEST_PACKET_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, BiomeUpdateDataRequestPacket> CODEC = StreamCodec.of(
                (buf, packet) -> {
                    ByteBufCodecs.INT.encode(buf, packet.biomeNames.size());
                    for(String name : packet.biomeNames) {
                        ByteBufCodecs.STRING_UTF8.encode(buf, name);
                    }
                    buf.writeInt(packet.imageData.length);
                    buf.writeBytes(packet.imageData());
                },
                buf -> {
                    int size = ByteBufCodecs.INT.decode(buf);
                    List<String> names = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        names.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                    }
                    int imageLength = ByteBufCodecs.INT.decode(buf);
                    byte[] imageData = new byte[imageLength];
                    buf.readBytes(imageData);
                    return new BiomeUpdateDataRequestPacket(names, imageData);
                }
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


        //public static final StreamCodec<RegistryFriendlyByteBuf, BiomePlacePayload2> CODEC = StreamCodec.composite(
        //        listCodec, BiomePlacePayload2::pos,
        //        ByteBufCodecs.STRING_UTF8, BiomePlacePayload2::biomeNamespace,
        //        ByteBufCodecs.STRING_UTF8, BiomePlacePayload2::biomePath,
        //        BiomePlacePayload2::new);

        public static final StreamCodec<RegistryFriendlyByteBuf, BiomePlacePayload2> CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, BiomePlacePayload2 payload) {
                // Create temp buffer
                FriendlyByteBuf tempBuf = new FriendlyByteBuf(Unpooled.buffer());

                // Encode raw data to temp buffer
                listCodec.encode(tempBuf, payload.pos());
                tempBuf.writeUtf(payload.biomeNamespace());
                tempBuf.writeUtf(payload.biomePath());

                // Compress it
                byte[] rawBytes = tempBuf.array();
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                try (DeflaterOutputStream deflater = new DeflaterOutputStream(byteOut)) {
                    deflater.write(rawBytes);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to compress BiomePlacePayload2", e);
                }

                // Write compressed bytes length + data
                byte[] compressed = byteOut.toByteArray();
                buf.writeVarInt(compressed.length);
                buf.writeBytes(compressed);
            }

            @Override
            public BiomePlacePayload2 decode(RegistryFriendlyByteBuf buf) {
                int length = buf.readVarInt();
                byte[] compressed = new byte[length];
                buf.readBytes(compressed);

                // Decompress
                ByteArrayInputStream byteIn = new ByteArrayInputStream(compressed);
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                try (InflaterInputStream inflater = new InflaterInputStream(byteIn)) {
                    byte[] buffer = new byte[256];
                    int read;
                    while ((read = inflater.read(buffer)) != -1) {
                        byteOut.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to decompress BiomePlacePayload2", e);
                }

                // Read from decompressed buffer
                FriendlyByteBuf tempBuf = new FriendlyByteBuf(Unpooled.wrappedBuffer(byteOut.toByteArray()));
                List<GlobalPos> pos = listCodec.decode(tempBuf);
                String ns = tempBuf.readUtf();
                String path = tempBuf.readUtf();

                return new BiomePlacePayload2(pos, ns, path);
            }
        };

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        public static byte[] splitByteArray(byte[] data, int totalParts, int partIndex) {
            if (partIndex < 0 || partIndex >= totalParts) {
                throw new IllegalArgumentException("Part index must be between 0 and " + (totalParts - 1));
            }

            if (totalParts <= 0) {
                throw new IllegalArgumentException("Total parts must be greater than 0");
            }

            if (data == null || data.length == 0) {
                return new byte[0];
            }

            // Calculate the size of each part
            int baseSize = data.length / totalParts;
            int remainder = data.length % totalParts;

            // Calculate the start position for the requested part
            int startPos = partIndex * baseSize + Math.min(partIndex, remainder);

            // Calculate the size of the requested part (some parts might be 1 byte larger due to remainder)
            int partSize = baseSize + (partIndex < remainder ? 1 : 0);

            // Create and fill the result array
            byte[] result = new byte[partSize];
            System.arraycopy(data, startPos, result, 0, partSize);

            return result;
        }

    }
}