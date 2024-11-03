package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.storage.MailStorage;
import org.MakeACakeStudios.storage.PlayerNameStorage;
import org.MakeACakeStudios.storage.TodoStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatusCommand implements CommandExecutor {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final MailStorage mailStorage;
    private final PlayerNameStorage playerNameStorage;
    private final TodoStorage todoStorage;

    public StatusCommand(MakeABuilders plugin, MailStorage mailStorage, PlayerNameStorage playerNameStorage, TodoStorage todoStorage) {
        this.mailStorage = mailStorage;
        this.playerNameStorage = playerNameStorage;
        this.todoStorage = todoStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        boolean mailConnected = mailStorage.isConnected();
        boolean playerNameConnected = playerNameStorage.isConnected();
        boolean todoConnected = todoStorage.isConnected();

        if (args.length == 0) {
            sender.sendMessage(miniMessage.deserialize("<yellow>Статус соединения с базами данных:</yellow>\n"));
            sender.sendMessage(miniMessage.deserialize("<yellow>MailStorage</yellow> -> " + (mailConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
            sender.sendMessage(miniMessage.deserialize("<yellow>PlayerNameStorage</yellow> -> " + (playerNameConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
            sender.sendMessage(miniMessage.deserialize("<yellow>TodoStorage</yellow> -> " + (todoConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "check":
                sender.sendMessage(miniMessage.deserialize("<yellow>Статус соединения с базами данных:</yellow>\n"));
                sender.sendMessage(miniMessage.deserialize("<yellow>MailStorage</yellow> -> " + (mailConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
                sender.sendMessage(miniMessage.deserialize("<yellow>PlayerNameStorage</yellow> -> " + (playerNameConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
                sender.sendMessage(miniMessage.deserialize("<yellow>TodoStorage</yellow> -> " + (todoConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
                break;

            case "mail":
                sender.sendMessage(miniMessage.deserialize("<yellow>MailStorage</yellow> ->" + (mailConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
                break;

            case "prefix":
                sender.sendMessage(miniMessage.deserialize("<yellow>PlayerNameStorage</yellow> -> " + (playerNameConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
                break;

            case "todo":
                sender.sendMessage(miniMessage.deserialize("<yellow>TodoStorage</yellow> -> " + (todoConnected ? "<green>Подключено</green>" : "<red>Не подключено</red>")));
                break;

            default:
                break;
        }

        return true;
    }
}