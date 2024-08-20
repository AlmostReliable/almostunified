package com.almostreliable.unified.core;

import com.almostreliable.unified.BuildConfig;
import com.almostreliable.unified.unification.worldgen.WorldStripper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class AlmostUnifiedCommands {

    private static final String RADIUS = "radius";

    private AlmostUnifiedCommands() {}

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        var radiusArgument = Commands.argument(RADIUS, IntegerArgumentType.integer(0, 16 * 8))
                .executes(AlmostUnifiedCommands::onStripCommand);
        var stripSubCommand = Commands.literal("strip")
                .requires(source -> source.hasPermission(2))
                .then(radiusArgument)
                .executes(AlmostUnifiedCommands::onStripCommand);
        var mainCommand = Commands.literal(BuildConfig.MOD_ID)
                .then(stripSubCommand)
                .executes(AlmostUnifiedCommands::onHelpCommand);

        dispatcher.register(mainCommand);
    }

    private static int onHelpCommand(CommandContext<CommandSourceStack> context) {
        //@formatter:off
        context.getSource().sendSuccess(() -> Component.literal(""), false);
        context.getSource().sendSuccess(() -> Component.literal("Almost Unified Commands").withStyle(ChatFormatting.GOLD), false);
        context.getSource().sendSuccess(() -> Component.literal("--------------------"), false);
        context.getSource().sendSuccess(() -> Component.literal("strip <radius> - strips everything in the specified radius except for Bedrock and ores"), false);
        //@formatter:on

        return Command.SINGLE_SUCCESS;
    }

    private static int onStripCommand(CommandContext<CommandSourceStack> context) {
        var player = context.getSource().getPlayer();
        var level = context.getSource().getLevel();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be used in-game!"));
            return 0;
        }

        try {
            var radius = context.getArgument(RADIUS, Integer.class);
            WorldStripper.stripWorld(player, level, radius);
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Please provide a valid radius!").withStyle(ChatFormatting.DARK_RED)
            );
            return 0;
        }

        return Command.SINGLE_SUCCESS;
    }
}
