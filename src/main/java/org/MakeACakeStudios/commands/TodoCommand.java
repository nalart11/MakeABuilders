package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.storage.TodoStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TodoCommand implements CommandExecutor {
    private final MakeABuilders plugin;
    private final TodoStorage todoStorage;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public TodoCommand(MakeABuilders plugin) {
        this.plugin = plugin;
        this.todoStorage = new TodoStorage(plugin.getDataFolder().getAbsolutePath());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эту команду может выполнить только игрок.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showTasks(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("add")) {
            if (args.length < 3) {
                player.sendMessage("Использование: /todo add <название> <описание>");
                return true;
            }

            // Сбор названия и описания задачи
            String title = args[1];
            String description = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);

            todoStorage.addTask(title, description);
            player.sendMessage("Задача успешно добавлена!");
            return true;
        }

        return false;
    }

    private void showTasks(Player player) {
        List<String[]> tasks = todoStorage.getTasks();

        if (tasks.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<yellow>Список задач пуст.</yellow>"));
            return;
        }

        player.sendMessage(miniMessage.deserialize("<yellow>Ваши задачи:</yellow>"));
        for (String[] task : tasks) {
            String id = task[0];
            String title = task[1];
            String description = task[2];

            player.sendMessage(miniMessage.deserialize("<gray>----------------<gray>"));
            player.sendMessage(miniMessage.deserialize("<yellow>Название: </yellow><green>" + title + "</green>"));
            player.sendMessage(miniMessage.deserialize("<yellow>Описание: </yellow><green>" + description + "</green>"));
            player.sendMessage(miniMessage.deserialize("<yellow>id: </yellow><red>" + id + "</red>"));
            player.sendMessage(miniMessage.deserialize("<gray>----------------<gray>"));
        }
    }
}
