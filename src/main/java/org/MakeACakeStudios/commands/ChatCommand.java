package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.chat0.AdminChat;
import org.MakeACakeStudios.player.NicknameBuilder;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;

import static org.MakeACakeStudios.utils.Formatter.*;

public class ChatCommand implements Command {

    private enum ChatMode {
        GLOBAL,
        ADMIN
    }

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("chat")
                        .senderType(Player.class)
                        .permission("makeabuilders.admin")
                        .literal("mode")
                        .required("mode", EnumParser.enumParser(ChatMode.class))
                        .handler(ctx -> handle(ctx.sender(), ctx.get("mode")))
        );

        manager.command(
                manager.commandBuilder("ac")
                        .senderType(Player.class)
                        .permission("makeabuilders.admin")
                        .required("message", StringParser.greedyStringParser())
                        .handler(ctx -> handleAdmin(ctx.sender(), ctx.get("message")))
        );

        manager.command(
                manager.commandBuilder("gc")
                        .senderType(Player.class)
                        .required("message", StringParser.greedyStringParser())
                        .handler(ctx -> handleGlobal(ctx.sender(), ctx.get("message")))
        );
    }

    private void handle(Player player, ChatMode mode) {
        String role = PlayerDataStorage.instance.getPlayerRoleByName(player.getName());

        if (mode == ChatMode.ADMIN) {
            if (!AdminChat.isAdmin(player) && !role.equals("custom")) {
                player.sendMessage(red("У вас нет доступа к админскому чату!"));
                return;
            }

            AdminChat.players.remove(player.getUniqueId());
            return;
        }

        AdminChat.toggle(player);
        player.sendMessage(green("Вы переключились в ", yellow("глобальный"), " чат."));
    }

    private void handleAdmin(@NotNull Player sender, @NotNull String text) {
        var message = AdminChat.formatMessage(sender, text);
        for (Player user : AdminChat.collectOnlineUsers()) {
            user.sendMessage(message);
        }
    }

    private void handleGlobal(@NotNull Player player, @NotNull String message) {
        var formatted = MiniMessage.miniMessage().deserialize(TagFormatter.format(message));
        var displayName = NicknameBuilder.displayName(player, false, false).hoverEvent(
                NicknameBuilder.replyHoverLines(player)
        );
        var component = single(displayName, text(" > "), formatted);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(component);
        }
    }
}
