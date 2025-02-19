package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.chat.AdminChat;
import org.MakeACakeStudios.chat.ChatUtils;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;

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
            if (!role.equals("owner") && !role.equals("admin") && !role.equals("moderator") && !role.equals("developer") && !role.equals("custom")) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>У вас нет доступа к админскому чату!</red>"));
                return;
            }

            AdminChat.toggleAdminChat(player);
            return;
        }

        AdminChat.removeFromAdminChat(player);
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Вы переключились в <yellow>глобальный</yellow> чат.</green>"));
    }

    private void handleAdmin(Player player, String message) {
        AdminChat.sendAdminMessage(player, message);
    }

    private void handleGlobal(Player player, String message) {
        ChatUtils.handleExternalCommandMessage(player, message);
    }
}
