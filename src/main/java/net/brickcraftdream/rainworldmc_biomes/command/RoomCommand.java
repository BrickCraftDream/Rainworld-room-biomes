package net.brickcraftdream.rainworldmc_biomes.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.brickcraftdream.rainworldmc_biomes.BiomeModifier;
import net.brickcraftdream.rainworldmc_biomes.data.storage.ConfigManagerServer;
import net.brickcraftdream.rainworldmc_biomes.image.ImageGenerator;
import net.brickcraftdream.rainworldmc_biomes.networking.BiomeImageProcessorClient;
import net.brickcraftdream.rainworldmc_biomes.templates.JsonExporter;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static net.brickcraftdream.rainworldmc_biomes.Rainworld_MC_Biomes.MOD_ID;
import static net.brickcraftdream.rainworldmc_biomes.client.Rainworld_MC_BiomesClient.hasDecimal;

public class RoomCommand {
    private static final SuggestionProvider<CommandSourceStack> ROOMS = (context, builder) -> {
        for(String warpName : ConfigManagerServer.getAllWarpNames("warps.json")) {
            builder.suggest(warpName);
        }
        return builder.buildFuture();
    };
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        //dispatcher.register(
        //        Commands.literal("modifybiome")
        //                .requires(source -> source.hasPermission(2)) // Require permission level 2 (usually for ops)
        //                .then(Commands.argument("namespace", StringArgumentType.word())
        //                        .then(Commands.argument("path", StringArgumentType.word())
        //                                .executes(RoomCommand::modifyBiome)
        //                        )
        //                )
        //
        //);
        //dispatcher.register(
        //        Commands.literal("createbiome")
        //                .requires(source -> source.hasPermission(2)) // Require permission level 2 (usually for ops)
        //                .then(Commands.argument("biomeId", StringArgumentType.word())
        //                        .executes(RoomCommand::createBiome)
        //                )
        //);
        dispatcher.register(
                Commands.literal("roomwarp")
                        .requires(source -> source.hasPermission(4))
                        .then(
                                Commands.argument("room_name", StringArgumentType.word())
                                .suggests(ROOMS)
                                .executes(RoomCommand::roomwarp)
                        )

        );
        dispatcher.register(
                Commands.literal("biomedebug")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                        .executes(RoomCommand::debugBiome)
        );
    }

    private static class CommandSuggestion implements SuggestionProvider<CommandSourceStack> {
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            for(String warpName : ConfigManagerServer.getAllWarpNames("warps.json")) {
                builder.suggest(warpName);
            }
            return builder.buildFuture();
        }
    }

    private static int debugBiome(CommandContext<CommandSourceStack> context) {
        JsonExporter exporter = new JsonExporter("/assets/rainworld/data/biomes.json");
        Holder<Biome> biomeHolder = context.getSource().getLevel().getBiome(Objects.requireNonNull(context.getSource().getPlayer()).blockPosition());
        Biome biome = biomeHolder.value();
        float temperature = biome.getBaseTemperature();
        String biomeName = biomeHolder.getRegisteredName().replace("rainworld:", "");
        String regionName = null;
        String roomNamePlusScreen = null;
        String roomName = null;
        String screenName = null;
        try {
            regionName = biomeName.substring(0, biomeName.indexOf("."));
        }
        catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Cannot get debug information on non-rainworld biomes."));
            return 0;
        }
        if(hasDecimal(temperature)) {
            context.getSource().sendFailure(
                    Component.literal("Cannot get debug information on non-rainworld biomes."));
            return 0;
        }
        roomNamePlusScreen = biomeName.substring(biomeName.indexOf(".") + 1);
        //long count = roomNamePlusScreen.chars().filter(character -> character == '_').count();
        if(!roomNamePlusScreen.contains("_screen")) {
            roomName = roomNamePlusScreen;
            screenName = "";
        }
        else {
            roomName = roomNamePlusScreen.substring(0, roomNamePlusScreen.indexOf("_screen"));
            screenName = roomNamePlusScreen.substring(roomNamePlusScreen.indexOf("_screen") + 1);
        }

        JsonObject room = exporter.getRoom(regionName, roomName);
        JsonObject screen = null;
        if(!screenName.isEmpty()) {
            screen = exporter.getScreen(regionName, roomName, screenName);
        }
        if(room == null) {
            context.getSource().sendFailure(
                    Component.literal("Something went wrong, room object is null."));
            System.out.println("DEBUG: " + regionName + " " + screenName + " " + roomNamePlusScreen);
            return 0;
        }
        String palette = null;
        String fadePalette = null;
        String fadeStrength = null;
        String grime = null;
        String effectColorA = null;
        String effectColorB = null;
        String dangerType = null;

        try {
            if (screen != null) {
                Map<String, Object> data = JsonExporter.getNodePropertiesStatic(screen);
                if(data.get("palette") != null) {palette = data.get("palette").toString();} else {palette = "/";}
                if(data.get("fade_palette") != null) {fadePalette = data.get("fade_palette").toString();} else {fadePalette = "/";}
                if(data.get("fade_strength") != null) {fadeStrength = data.get("fade_strength").toString();} else {fadeStrength = "/";}
                if(data.get("grime") != null) {grime = data.get("grime").toString();} else {grime = "/";}
                if(data.get("effect_color_a") != null) {effectColorA = data.get("effect_color_a").toString();} else {effectColorA = "/";}
                if(data.get("effect_color_b") != null) {effectColorB = data.get("effect_color_b").toString();} else {effectColorB = "/";}
                if(data.get("danger_type") != null) dangerType = data.get("danger_type").toString();else {dangerType = "/";}
            } else {
                Map<String, Object> data = JsonExporter.getNodePropertiesStatic(room);
                if(data.get("palette") != null) {palette = data.get("palette").toString();} else {palette = "/";}
                if(data.get("fade_palette") != null) {fadePalette = data.get("fade_palette").toString();} else {fadePalette = "/";}
                if(data.get("fade_strength") != null) {fadeStrength = data.get("fade_strength").toString();} else {fadeStrength = "/";}
                if(data.get("grime") != null) {grime = data.get("grime").toString();} else {grime = "/";}
                if(data.get("effect_color_a") != null) {effectColorA = data.get("effect_color_a").toString();} else {effectColorA = "/";}
                if(data.get("effect_color_b") != null) {effectColorB = data.get("effect_color_b").toString();} else {effectColorB = "/";}
                if(data.get("danger_type") != null) dangerType = data.get("danger_type").toString();else {dangerType = "/";}
            }
        }
        catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Something went wrong: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }

        if(palette == null || fadePalette == null || fadeStrength == null || grime == null || effectColorA == null || effectColorB == null || dangerType == null) {
            context.getSource().sendFailure(
                    Component.literal("Something went wrong, at least one of these values is null: " + palette + ", " + fadePalette + ", " + fadeStrength + ", " + grime + ", " + effectColorA + ", " + effectColorB + ", " + dangerType));
            return 0;
        }

        BufferedImage image = null;
        try {
            image = BiomeImageProcessorClient.resourceLocationToBufferedImage(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/dynamic/shader_data.png"));
        } catch (Exception e) {
            System.out.println("Failed to load shader data image: " + e.getMessage());
        }

        Object[] data = new Object[]{"/", "/", "/", "/", "/", "/", "/", "/"};
        if (image != null) {
            data = ImageGenerator.imageToRoom(image, (int) temperature - 2);
        }




        if(palette.length() == 1) palette = palette + "   ";
        if(palette.length() == 2) palette = palette + "  ";

        if(fadePalette.length() == 1) fadePalette = fadePalette + "   ";
        if(fadePalette.length() == 2) fadePalette = fadePalette + "  ";

        fadeStrength = fadeStrength.substring(0, Math.min(fadeStrength.length(), 6));
        if(fadeStrength.length() == 1) fadeStrength = fadeStrength + "       ";
        if(fadeStrength.length() == 2) fadeStrength = fadeStrength + "      ";
        if(fadeStrength.length() == 3) fadeStrength = fadeStrength + "     ";
        if(fadeStrength.length() == 4) fadeStrength = fadeStrength + "   ";
        if(fadeStrength.length() == 5) fadeStrength = fadeStrength + "  ";

        grime = grime.substring(0, Math.min(grime.length(), 6));
        if(grime.length() == 1) grime = grime + "       ";
        if(grime.length() == 2) grime = grime + "      ";
        if(grime.length() == 3) grime = grime + "     ";
        if(grime.length() == 4) grime = grime + "   ";
        if(grime.length() == 5) grime = grime + "  ";

        if(effectColorA.length() == 1) effectColorA = effectColorA + "  ";

        if(effectColorB.length() == 1) effectColorB = effectColorB + "  ";

        switch(dangerType) {
            case "0" -> dangerType = "None             ";
            case "1" -> dangerType = "Rain              ";
            case "2" -> dangerType = "Flood            ";
            case "3" -> dangerType = "Flood and Rain";
            case "4" -> dangerType = "Blizzard         ";
            case "5" -> dangerType = "Aerie Blizzard";
            default -> dangerType =  "unknown         ";
        }

        context.getSource().sendSystemMessage(Component.literal("Debug data for the current biome:").withStyle(ChatFormatting.BOLD));
        context.getSource().sendSystemMessage(Component.literal("Biome Name: " + biomeName));
        context.getSource().sendSystemMessage(Component.literal("Biome Temperature: " + temperature).withStyle(ChatFormatting.LIGHT_PURPLE));
        //context.getSource().sendSystemMessage(Component.literal(""
        //                + Arrays.toString(ImageGenerator.getCoordsFromLinear((int) ((temperature) + 1))) + ", "
        //                + Arrays.toString(ImageGenerator.getCoordsFromLinear((int) ((temperature) + 2))) + ", "
        //                + Arrays.toString(ImageGenerator.getCoordsFromLinear((int) ((temperature) + 3))) + "").withStyle(ChatFormatting.BLUE));
        context.getSource().sendSystemMessage(Component.literal(Arrays.toString(ImageGenerator.getCoordsFromLinear((int) ((temperature - 2) + 1))) + ", ").withStyle(ChatFormatting.AQUA)
                .append(Component.literal(Arrays.toString(ImageGenerator.getCoordsFromLinear((int) ((temperature - 2) + 2))) + ", ").withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal(Arrays.toString(ImageGenerator.getCoordsFromLinear((int) ((temperature - 2) + 3))) + ", ").withStyle(ChatFormatting.BLUE)));
        context.getSource().sendSystemMessage(Component.literal("Data Type        Internal data    Image data"));
        //                                                           "Palette        | 000            |
        //                                                           "Fade Palette   | 000            |
        //                                                           "Fade Strength  | 0.0000         |
        //                                                           "Grime          | 0.0000         |
        //                                                           "Effect Color A | 00             |
        //                                                           "Effect Color B | 00             |
        //                                                           "Danger Type    | Flood and Rain |
        //                                                           "text text text | 0.0000         |
        try {
            context.getSource().sendSystemMessage(Component.literal("Palette            " + palette + "                " + data[0].toString()).withStyle(ChatFormatting.GREEN));
            context.getSource().sendSystemMessage(Component.literal("Fade Palette     " + fadePalette + "                " + data[1].toString()).withStyle(ChatFormatting.GREEN));
            context.getSource().sendSystemMessage(Component.literal("Fade Strength   " + fadeStrength + "            " + data[2].toString()).withStyle(ChatFormatting.GREEN));
            context.getSource().sendSystemMessage(Component.literal("Grime               " + grime + "            " + data[3].toString() + " (broken)").withStyle(ChatFormatting.GREEN));
            context.getSource().sendSystemMessage(Component.literal("Effect Color A   " + effectColorA + "                 " + data[4].toString()).withStyle(ChatFormatting.GREEN));
            context.getSource().sendSystemMessage(Component.literal("Effect Color B   " + effectColorB + "                 " + data[5].toString()).withStyle(ChatFormatting.GREEN));
            context.getSource().sendSystemMessage(Component.literal("Danger Type     " + dangerType + "  " + data[6].toString()).withStyle(ChatFormatting.GREEN));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }

    private static int roomwarp(CommandContext<CommandSourceStack> context) {
        String roomName = StringArgumentType.getString(context, "room_name");
        JsonObject warps = ConfigManagerServer.readConfig("warps.json");

        if (warps == null || warps.entrySet().isEmpty()) {
            context.getSource().sendFailure(
                    Component.literal("No room warps have been created yet. Be the first and add a room with the room tool."));
            return 0;
        }

        if (!warps.has(roomName)) {
            context.getSource().sendFailure(
                    Component.literal("The specified room \"" + roomName + "\" doesn't exist."));
            return 0;
        }

        ConfigManagerServer.WarpData warp = ConfigManagerServer.WarpData.fromJson(warps.getAsJsonObject(roomName));

        try {
            context.getSource().getPlayer().teleportTo(warp.x, warp.y, warp.z);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Something went wrong: " + e.getMessage()));
            return 0;
        }
    }

    private static int modifyBiome(CommandContext<CommandSourceStack> context) {
        String namespace = StringArgumentType.getString(context, "namespace");
        String path = StringArgumentType.getString(context, "path");

        try {
            // Use your existing BiomeModifier class to modify the biome
            BiomeModifier.modifyBiome(namespace, path);

            context.getSource().sendSuccess(() ->
                    Component.literal("Successfully modified biome: " + namespace + ":" + path), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Failed to modify biome: " + e.getMessage()));
            return 0;
        }
    }

    private static int createBiome(CommandContext<CommandSourceStack> context) {
        String biomeId = StringArgumentType.getString(context, "biomeId");

        try {
            // Use your existing BiomeModifier class to create and register the biome
            BiomeModifier.createAndRegisterBiome(context.getSource().getServer(), biomeId);

            context.getSource().sendSuccess(() ->
                    Component.literal("Successfully created biome: " + biomeId), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Failed to create biome: " + e.getMessage()));
            return 0;
        }
    }

}

