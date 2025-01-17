package org.MakeACakeStudios.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.MakeACakeStudios.chat.ChatUtils;
import org.MakeACakeStudios.Command;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;

public class SmugCommand implements Command {

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("shrug")
                        .senderType(Player.class)
                        .optional("text", StringParser.greedyStringParser(), DefaultValue.constant(""))
                        .handler(ctx -> handle(ctx.sender(), ctx.get("text"), ":shrug:"))
        );
        manager.command(
                manager.commandBuilder("tableflip")
                        .senderType(Player.class)
                        .optional("text", StringParser.greedyStringParser(), DefaultValue.constant(""))
                        .handler(ctx -> handle(ctx.sender(), ctx.get("text"), ":tableflip:"))
        );
        manager.command(
                manager.commandBuilder("unflip")
                        .senderType(Player.class)
                        .optional("text", StringParser.greedyStringParser(), DefaultValue.constant(""))
                        .handler(ctx -> handle(ctx.sender(), ctx.get("text"), "unflip"))
        );
    }

    private void handle(@NonNull Player sender, String text, String addition) {
        ChatUtils.handleExternalCommandMessage(sender, addition + " " + text);
    }

}
