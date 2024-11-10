package org.MakeACakeStudios.commands;

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
            showSingleTask(player, 0); // Показываем первую задачу
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

        if (args[0].equalsIgnoreCase("view")) {
            if (args.length < 2) {
                player.sendMessage("Использование: /todo view <номер>");
                return true;
            }

            try {
                int taskIndex = Integer.parseInt(args[1]) - 1;
                showSingleTask(player, taskIndex);
            } catch (NumberFormatException e) {
                player.sendMessage("Неправильный формат номера задачи.");
            }
            return true;
        }

        return false;
    }

    private void showSingleTask(Player player, int taskIndex) {
        List<String[]> tasks = todoStorage.getTasks();

        if (tasks.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<yellow>Список задач пуст.</yellow>"));
            return;
        }

        if (taskIndex < 0 || taskIndex >= tasks.size()) {
            player.sendMessage(miniMessage.deserialize("<red>Задача с таким номером не существует.</red>"));
            return;
        }

        // Отображаем конкретную задачу
        sendFormattedTaskWithIndex(player, tasks, taskIndex);
    }

    private void sendFormattedTaskWithIndex(Player player, List<String[]> tasks, int currentIndex) {
        String[] task = tasks.get(currentIndex);
        String id = task[0];
        String title = task[1];
        String description = task[2];

        // Отображаем задачу
        player.sendMessage(miniMessage.deserialize("<gray>----------------<gray>"));
        player.sendMessage(miniMessage.deserialize("<yellow>Название: </yellow><green>" + title + "</green>"));
        player.sendMessage(miniMessage.deserialize("<yellow>Описание: </yellow><green>" + description + "</green>"));
        player.sendMessage(miniMessage.deserialize("<yellow>id: </yellow><red>" + id + "</red>"));
        player.sendMessage(miniMessage.deserialize("<gray>----------------<gray>"));

        // Отображаем индексы для навигации между задачами
        displayTaskIndices(player, tasks.size(), currentIndex);
    }

    private void displayTaskIndices(Player player, int taskCount, int currentIndex) {
        if (taskCount <= 1) return; // Индексы показываем только если задач больше одной

        StringBuilder indices = new StringBuilder();

        if (taskCount == 3 || taskCount == 4 || (taskCount == 5 && currentIndex == 2)) {
            for (int i = 0; i < taskCount; i++) {
                if (i == currentIndex) {
                    indices.append("<color:#fc8803>[")
                            .append(i + 1)
                            .append("]</color> ");
                } else {
                    indices.append("<click:run_command:/todo view ")
                            .append(i + 1)
                            .append("><yellow>[")
                            .append(i + 1)
                            .append("]</yellow></click> ");
                }
            }
        } else if (currentIndex <= 1) {
            for (int i = 0; i < Math.min(3, taskCount); i++) {
                if (i == currentIndex) {
                    indices.append("<color:#fc8803>[")
                            .append(i + 1)
                            .append("]</color> ");
                } else {
                    indices.append("<click:run_command:/todo view ")
                            .append(i + 1)
                            .append("><yellow>[")
                            .append(i + 1)
                            .append("]</yellow></click> ");
                }
            }
            if (taskCount > 3) {
                indices.append("... ");
                indices.append("<click:run_command:/todo view ")
                        .append(taskCount)
                        .append("><yellow>[")
                        .append(taskCount)
                        .append("]</yellow></click> ");
            }
        } else if (currentIndex < taskCount - 2) {
            indices.append("<click:run_command:/todo view 1><yellow>[1]</yellow></click> ");
            indices.append("... ");
            indices.append("<click:run_command:/todo view ")
                    .append(currentIndex)
                    .append("><yellow>[")
                    .append(currentIndex)
                    .append("]</yellow></click> ");
            indices.append("<color:#fc8803>[")
                    .append(currentIndex + 1)
                    .append("]</color> ");
            indices.append("<click:run_command:/todo view ")
                    .append(currentIndex + 2)
                    .append("><yellow>[")
                    .append(currentIndex + 2)
                    .append("]</yellow></click> ");
            indices.append("... ");
            indices.append("<click:run_command:/todo view ")
                    .append(taskCount)
                    .append("><yellow>[")
                    .append(taskCount)
                    .append("]</yellow></click> ");
        } else {
            indices.append("<click:run_command:/todo view 1><yellow>[1]</yellow></click> ");
            indices.append("... ");
            for (int i = taskCount - 3; i < taskCount; i++) {
                if (i == currentIndex) {
                    indices.append("<color:#fc8803>[")
                            .append(i + 1)
                            .append("]</color> ");
                } else {
                    indices.append("<click:run_command:/todo view ")
                            .append(i + 1)
                            .append("><yellow>[")
                            .append(i + 1)
                            .append("]</yellow></click> ");
                }
            }
        }

        player.sendMessage(miniMessage.deserialize(indices.toString()));
    }
}