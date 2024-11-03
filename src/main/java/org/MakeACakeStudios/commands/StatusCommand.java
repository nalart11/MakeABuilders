package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.storage.MailStorage;
import org.MakeACakeStudios.storage.PlayerNameStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatusCommand implements CommandExecutor {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final MailStorage mailStorage;
    private final PlayerNameStorage playerNameStorage;

    public StatusCommand(MakeABuilders plugin, MailStorage mailStorage, PlayerNameStorage playerNameStorage) {
        this.mailStorage = mailStorage;
        this.playerNameStorage = playerNameStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эту команду можно использовать только игрокам.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Использование: /status <check|mail|prefix>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "check":
                boolean mailConnected = mailStorage.isConnected();
                boolean playerNameConnected = playerNameStorage.isConnected();

                sender.sendMessage(miniMessage.deserialize("<yellow>Статус соединения с базами данных:</yellow>\n"));
                sender.sendMessage(miniMessage.deserialize("<yellow>MailStorage</yellow> -> " + (mailConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
                sender.sendMessage(miniMessage.deserialize("<yellow>PlayerNameStorage</yellow> -> " + (playerNameConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
                break;

            case "mail":
                sender.sendMessage(miniMessage.deserialize("<yellow>MailStorage</yellow> ->" + (mailStorage.isConnected() ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
                break;

            case "prefix":
                sender.sendMessage(miniMessage.deserialize("<yellow>PlayerNameStorage</yellow> -> " + (playerNameStorage.isConnected() ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
                break;

            default:
                sender.sendMessage("Неизвестный аргумент. Используйте: /status <check|mail|prefix>");
                break;
        }

        return true;
    }
}