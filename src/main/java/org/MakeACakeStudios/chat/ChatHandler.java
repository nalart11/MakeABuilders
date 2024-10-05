package org.MakeACakeStudios.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChatHandler implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final FileConfiguration config;

    public ChatHandler(MakeABuilders makeABuilders) {
        this.config = makeABuilders.getConfig(); // Инициализируем config с помощью метода getConfig() плагина
    }

    // Получение случайного сообщения для события
    private String getRandomMessage(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return null; // Если сообщения не заданы, ничего не выводим
        }
        Random random = new Random();
        return messages.get(random.nextInt(messages.size())); // Выбираем случайное сообщение
    }

    // Обработчик события входа игрока
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        List<String> joinMessages = config.getStringList("Messages.Join");
        String rawMessage = getRandomMessage(joinMessages);

        if (rawMessage != null) {
            String prefix = getPlayerPrefix(player);
            String suffix = getPlayerSuffix(player);

            // Заменяем <player> на префикс, имя игрока и суффикс
            String parsedMessage = rawMessage.replace("<player>", prefix + player.getName() + suffix);
            Component joinMessage = miniMessage.deserialize(parsedMessage);
            event.joinMessage(joinMessage); // Устанавливаем сообщение о входе
        }
    }

    // Обработчик события выхода игрока
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        List<String> quitMessages = config.getStringList("Messages.Quit");
        String rawMessage = getRandomMessage(quitMessages);

        if (rawMessage != null) {
            String prefix = getPlayerPrefix(player);
            String suffix = getPlayerSuffix(player);

            // Заменяем <player> на префикс, имя игрока и суффикс
            String parsedMessage = rawMessage.replace("<player>", prefix + player.getName() + suffix);
            Component quitMessage = miniMessage.deserialize(parsedMessage);
            event.quitMessage(quitMessage); // Устанавливаем сообщение о выходе
        }
    }

    private static final Map<String, Integer> groupWeights = new HashMap<>();

    static {
        // Задаем веса для каждой группы
        groupWeights.put("iam", 5);
        groupWeights.put("javadper", 4);
        groupWeights.put("admin", 3);
        groupWeights.put("developer", 2);
        groupWeights.put("moderator", 1);
    }

    // Метод для получения префикса на основе группы игрока
    public String getPlayerPrefix(Player player) {
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        String highestGroup = "default";
        int highestWeight = 0;

        if (user != null) {
            for (Node node : user.getNodes()) {
                if (node instanceof InheritanceNode) {
                    InheritanceNode inheritanceNode = (InheritanceNode) node;
                    String groupName = inheritanceNode.getGroupName();
                    // Проверяем, есть ли группа в мапе и ее вес
                    if (groupWeights.containsKey(groupName) && groupWeights.get(groupName) > highestWeight) {
                        highestWeight = groupWeights.get(groupName);
                        highestGroup = groupName; // Запоминаем группу с наибольшим весом
                    }
                }
            }
        }

        // Возвращаем префикс на основе группы с наибольшим весом
        switch (highestGroup) {
            case "iam":
                return "<yellow>\uD83D\uDC51</yellow> <gradient:#ae00cb:#fc002d>";
            case "javadper":
                return "<blue>\uD83D\uDEE0</blue> <gradient:#E0E0E0:#808080>";
            case "admin":
                return "<red>\uD83D\uDEE1</red> <gradient:#FF2323:#FF7878>";
            case "developer":
                return "<blue>\uD83D\uDEE0</blue> <gradient:#141378:#97ABFF>";
            case "moderator":
                return "<red>\uD83D\uDEE1</red> <gradient:#23DBFF:#C8E9FF>";
            default:
                return "";
        }
    }


    // Метод для получения суффикса на основе группы игрока
    public String getPlayerSuffix(Player player) {
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            // Получаем все узлы наследования игрока
            for (Node node : user.getNodes()) {
                if (node instanceof InheritanceNode) {
                    InheritanceNode inheritanceNode = (InheritanceNode) node;
                    String groupName = inheritanceNode.getGroupName();
                    if (!groupName.equals("default")) {
                        return "</gradient>";
                    }
                }
            }
        }
        return "";
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getDisplayName();
        String message = event.getMessage();

        System.out.println(playerName + " → " + message);

        if (message.contains(":loc:")) {
            int x = player.getLocation().getBlockX();
            int y = player.getLocation().getBlockY();
            int z = player.getLocation().getBlockZ();

            String worldName = player.getWorld().getName();

            String color;
            switch (worldName) {
                case "world":
                    color = "<green>";
                    break;
                case "world_nether":
                    color = "<red>";
                    break;
                case "world_the_end":
                    color = "<light_purple>";
                    break;
                default:
                    color = "<white>";
            }

            String location = color + "<click:run_command:'/goto " + worldName + " " + x + " " + y + " " + z + "'>["
                    + x + "x/" + y + "y/" + z + "z, " + worldName + "]</click><reset>";

            message = message.replace(":loc:", location);
        }

        String prefix = getPlayerPrefix(player);
        String suffix = getPlayerSuffix(player);

        String formattedMessage = prefix + playerName + suffix + " > " + message;

        Component parsedMessage = miniMessage.deserialize(formattedMessage);

        // Здесь добавляем доступ к серверу через Bukkit
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(parsedMessage);
        }

        event.setCancelled(true);
    }
}
