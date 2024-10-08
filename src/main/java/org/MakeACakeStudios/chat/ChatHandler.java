package org.MakeACakeStudios.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.storage.PlayerNameStorage;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class ChatHandler implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final FileConfiguration config;
    private final MakeABuilders plugin;
    private final Map<String, List<String>> messages = new HashMap<>();
    private final PlayerNameStorage playerNameStorage;

    public ChatHandler(MakeABuilders makeABuilders) {
        this.config = makeABuilders.getConfig(); // Инициализируем config
        this.plugin = makeABuilders; // Присваиваем экземпляр основного класса
        this.playerNameStorage = new PlayerNameStorage(plugin);
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

        // Получаем префикс и суффикс из хранилища
        String prefix = playerNameStorage.getPlayerPrefix(player);
        String suffix = playerNameStorage.getPlayerSuffix(player);
        List<String> joinMessages = config.getStringList("Messages.Join");
        String rawMessage = getRandomMessage(joinMessages);

        if (rawMessage != null) {

            // Заменяем <player> на префикс, имя игрока и суффикс
            String parsedMessage = rawMessage.replace("<player>", prefix + player.getName() + suffix);
            Component joinMessage = miniMessage.deserialize(parsedMessage);
            event.joinMessage(joinMessage); // Устанавливаем сообщение о входе

            // Получаем сообщения для игрока
            List<String[]> playerMessages = plugin.getMailStorage().getMessages(player.getName());
            if (!playerMessages.isEmpty()) {
                player.sendMessage(miniMessage.deserialize("<green>У вас есть <yellow>" + playerMessages.size() + "</yellow> непрочитанных сообщений.</green>"));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String prefix = playerNameStorage.getPlayerPrefix(player);
        String suffix = playerNameStorage.getPlayerSuffix(player);
        List<String> quitMessages = config.getStringList("Messages.Quit");
        String rawMessage = getRandomMessage(quitMessages);

        if (rawMessage != null) {
            // Заменяем <player> на префикс, имя игрока и суффикс
            String parsedMessage = rawMessage.replace("<player>", prefix + player.getName() + suffix);
            Component quitMessage = miniMessage.deserialize(parsedMessage);
            event.quitMessage(quitMessage); // Устанавливаем сообщение о выходе
        }
    }

    public Map<String, List<String>> getMessages() {
        return messages;
    }

    private static final Map<String, Integer> groupWeights = new HashMap<>();

    static {
        groupWeights.put("iam", 5);
        groupWeights.put("javadper", 4);
        groupWeights.put("admin", 3);
        groupWeights.put("developer", 2);
        groupWeights.put("moderator", 1);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getDisplayName();
        String message = event.getMessage();

        System.out.println(playerName + " → " + message);

        if (message.contains("@")) {
            String[] words = message.split(" ");
            for (String word : words) {
                if (word.startsWith("@")) {
                    String mentionedPlayerName = word.substring(1); // Получаем ник упомянутого игрока
                    Player mentionedPlayer = Bukkit.getPlayer(mentionedPlayerName);

                    if (mentionedPlayer != null && mentionedPlayer.isOnline()) {
                        // Получаем звук для упомянутого игрока
                        Sound notificationSound = plugin.getPlayerSound(mentionedPlayer);
                        // Воспроизводим звук для упомянутого игрока
                        mentionedPlayer.playSound(mentionedPlayer.getLocation(), notificationSound, 1.0f, 1.0f);

                        // Получаем префикс и суффикс упомянутого игрока
                        String mentionedPlayerPrefix = playerNameStorage.getPlayerPrefix(mentionedPlayer);
                        String mentionedPlayerSuffix = playerNameStorage.getPlayerSuffix(mentionedPlayer);

                        // Форматируем упоминание
                        String formattedMention = "<yellow>@</yellow>" + mentionedPlayerPrefix + mentionedPlayer.getName() + mentionedPlayerSuffix;

                        // Заменяем упоминание в сообщении
                        message = message.replace(word, formattedMention);
                    }
                }
            }
        }

        if (message.contains(":loc:")) {
            int x = player.getLocation().getBlockX();
            int y = player.getLocation().getBlockY();
            int z = player.getLocation().getBlockZ();

            String worldName = player.getWorld().getName();

            String color;
            switch (worldName) {
                case "world":
                    color = "<gradient:#00FF1A:#7EFF91>";
                    break;
                case "world_nether":
                    color = "<gradient:#FF0000:#FF7E7E>";
                    break;
                case "world_the_end":
                    color = "<gradient:#ED00FF:#DE7EFF>";
                    break;
                default:
                    color = "<white>";
            }

            String location = color + "<click:run_command:'/goto " + worldName + " " + x + " " + y + " " + z + "'>["
                    + x + "x/" + y + "y/" + z + "z, " + worldName + "]</click><reset>";

            message = message.replace(":loc:", location);
        }
        if (message.contains(":cry:")) {
            message = message.replace(":cry:", "<yellow>☹</yellow><aqua>,</aqua>");
        }
        if (message.contains(":skull:")) {
            message = message.replace(":skull:", "☠");
        }
        if (message.contains(":skulley:")) {
            message = message.replace(":skulley:", "<red>☠</red>");
        }
        if (message.contains("<3") || message.contains(":heart:")) {
            message = message.replace("<3", "<red>❤</red>")
                    .replace(":heart:", "<red>❤</red>");
        }
        if (message.contains(":fire:")) {
            message = message.replace(":fire:", "<color:#FF7800>\uD83D\uDD25</color>");
        }
        if (message.contains(":star:")) {
            message = message.replace(":star:", "<yellow>★</yellow>");
        }
        if (message.contains(":stop:")) {
            message = message.replace(":stop:", "<red>⚠</red>");
        }
        if (message.contains(":sun:")) {
            message = message.replace(":sun:", "<yellow>☀</yellow>");
        }
        if (message.contains(":mail:")) {
            message = message.replace(":mail:", "✉");
        }
        if (message.contains(":happy:")) {
            message = message.replace(":happy:", "☺");
        }
        if (message.contains(":sad:")) {
            message = message.replace(":sad:", "☹");
        }
        if (message.contains(":umbrella:")) {
            message = message.replace(":umbrella:", "☂");
        }

        String prefix = playerNameStorage.getPlayerPrefix(player);
        String suffix = playerNameStorage.getPlayerSuffix(player);

        String formattedMessage = prefix + playerName + suffix + " > " + message;

        Component parsedMessage = miniMessage.deserialize(formattedMessage);

        // Здесь добавляем доступ к серверу через Bukkit
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(parsedMessage);
        }

        event.setCancelled(true);
    }
}