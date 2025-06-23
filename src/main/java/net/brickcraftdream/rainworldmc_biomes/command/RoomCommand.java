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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

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
    }

    private static class CommandSuggestion implements SuggestionProvider<CommandSourceStack> {
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            for(String warpName : ConfigManagerServer.getAllWarpNames("warps.json")) {
                builder.suggest(warpName);
            }
            return builder.buildFuture();
        }
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

