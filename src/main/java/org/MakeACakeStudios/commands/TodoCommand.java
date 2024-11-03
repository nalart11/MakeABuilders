package org.MakeACakeStudios.commands;

import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.storage.TodoStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TodoCommand implements CommandExecutor, Listener {
    private final MakeABuilders plugin;
    private final TodoStorage todoStorage;

    private final Map<Player, TodoState> playerTodoState = new HashMap<>();

    public TodoCommand(MakeABuilders plugin) {
        this.plugin = plugin;
        this.todoStorage = new TodoStorage(plugin.getDataFolder().getAbsolutePath());
        plugin.getServer().getPluginManager().registerEvents(this, plugin); // Регистрация слушателя событий чата
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
            // Начинаем процесс добавления задачи
            playerTodoState.put(player, new TodoState());
            player.sendMessage("Введите название задачи:");
            return true;
        }

        return false;
    }

    // Слушатель для событий чата, чтобы обрабатывать этапы добавления задач
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Проверяем, ожидаем ли мы ввода от игрока
        if (playerTodoState.containsKey(player)) {
            event.setCancelled(true); // Отключаем вывод сообщения в чат

            TodoState state = playerTodoState.get(player);

            if (state.getTitle() == null) {
                // Сохраняем название задачи
                state.setTitle(event.getMessage());
                player.sendMessage("Теперь введите описание задачи:");
            } else {
                // Сохраняем описание задачи и добавляем её в базу
                state.setDescription(event.getMessage());
                todoStorage.addTask(state.getTitle(), state.getDescription()); // Используем TodoStorage
                player.sendMessage("Задача успешно добавлена!");
                playerTodoState.remove(player); // Убираем игрока из отслеживания
            }
        }
    }

    private void showTasks(Player player) {
        List<String[]> tasks = todoStorage.getTasks();

        if (tasks.isEmpty()) {
            player.sendMessage("Список задач пуст.");
            return;
        }

        player.sendMessage("Ваши задачи:");
        for (String[] task : tasks) {
            String title = task[0];
            String description = task[1];

            player.sendMessage("----------------");
            player.sendMessage("Название: " + title);
            player.sendMessage("Описание: " + description);
            player.sendMessage("----------------");
        }
    }

    // Вспомогательный класс для хранения состояния добавления задачи
    private static class TodoState {
        private String title;
        private String description;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
