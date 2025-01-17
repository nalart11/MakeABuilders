package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.storage.MailStorage;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.MakeACakeStudios.storage.PunishmentStorage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

public class StatusCommand implements Command {

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("status")
                        .senderType(Player.class)
                        .literal("check")
                        .handler(ctx -> handleCheck(ctx.sender()))
                        .build()
        );

        manager.command(
                manager.commandBuilder("status")
                        .senderType(Player.class)
                        .literal("mail")
                        .handler(ctx -> handleMail(ctx.sender()))
                        .build()
        );

        manager.command(
                manager.commandBuilder("status")
                        .senderType(Player.class)
                        .literal("player")
                        .handler(ctx -> handlePlayer(ctx.sender()))
                        .build()
        );

        manager.command(
                manager.commandBuilder("status")
                        .senderType(Player.class)
                        .literal("punishments")
                        .handler(ctx -> handlePunishments(ctx.sender()))
                        .build()
        );
    }

    private void handleCheck(@NotNull CommandSender sender) {
        Player player = (Player) sender;

        boolean mailConnected = MailStorage.instance.isConnected();
        boolean playerNameConnected = PlayerDataStorage.instance.isConnected();
        boolean punishmentConnected = PunishmentStorage.instance.isConnected();

        player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Статус соединения с базами данных:</gray>"));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>MailStorage</yellow> -> " + (mailConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>PlayerDataStorage</yellow> -> " + (playerNameConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>PunishmentStorage</yellow> -> " + (punishmentConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
    }

    private void handleMail(@NotNull CommandSender sender) {
        Player player = (Player) sender;

        boolean mailConnected = MailStorage.instance.isConnected();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Статус соединения с MailStorage:</gray>"));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>MailStorage</yellow> -> " + (mailConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
    }

    private void handlePlayer(@NotNull CommandSender sender) {
        Player player = (Player) sender;

        boolean playerNameConnected = PlayerDataStorage.instance.isConnected();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Статус соединения с PlayerDataStorage:</gray>"));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>PlayerDataStorage</yellow> -> " + (playerNameConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
    }

    private void handlePunishments(@NotNull CommandSender sender) {
        Player player = (Player) sender;

        boolean punishmentConnected = PunishmentStorage.instance.isConnected();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Статус соединения с PunishmentStorage:</gray>"));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>PunishmentStorage</yellow> -> " + (punishmentConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
    }
}
