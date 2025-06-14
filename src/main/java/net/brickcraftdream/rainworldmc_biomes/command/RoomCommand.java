package net.brickcraftdream.rainworldmc_biomes.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.brickcraftdream.rainworldmc_biomes.BiomeModifier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class RoomCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("modifybiome")
                        .requires(source -> source.hasPermission(2)) // Require permission level 2 (usually for ops)
                        .then(Commands.argument("namespace", StringArgumentType.word())
                                .then(Commands.argument("path", StringArgumentType.word())
                                        .executes(RoomCommand::modifyBiome)
                                )
                        )

        );
        dispatcher.register(
                Commands.literal("createbiome")
                        .requires(source -> source.hasPermission(2)) // Require permission level 2 (usually for ops)
                        .then(Commands.argument("biomeId", StringArgumentType.word())
                                .executes(RoomCommand::createBiome)
                        )
        );
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

