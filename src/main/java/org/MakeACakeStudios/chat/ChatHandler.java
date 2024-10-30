package org.MakeACakeStudios.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.commands.MuteCommand;
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
    private final TagFormatter tagFormatter;

    public ChatHandler(MakeABuilders makeABuilders) {
        this.config = makeABuilders.getConfig();
        this.plugin = makeABuilders;
        this.playerNameStorage = new PlayerNameStorage(plugin);
        this.tagFormatter = new TagFormatter();
    }

    private String getRandomMessage(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return messages.get(random.nextInt(messages.size()));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.sendMessage(miniMessage.deserialize("<gradient:#FF3D4D:#FCBDBD>С возвращением!</gradient>"));

        String prefix = playerNameStorage.getPlayerPrefix(player);
        String suffix = playerNameStorage.getPlayerSuffix(player);
        List<String> joinMessages = config.getStringList("Messages.Join");
        String rawMessage = getRandomMessage(joinMessages);

        if (rawMessage != null) {
            String parsedMessage = rawMessage.replace("<player>", prefix + player.getName() + suffix);
            Component joinMessage = miniMessage.deserialize(parsedMessage);
            event.joinMessage(joinMessage);

            List<String[]> playerMessages = plugin.getMailStorage().getMessages(player.getName());
            if (!playerMessages.isEmpty()) {
                Sound selectedSound = plugin.getPlayerSound(player);
                player.playSound(player.getLocation(), selectedSound, 1.0F, 1.0F);
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
            String parsedMessage = rawMessage.replace("<player>", prefix + player.getName() + suffix);
            Component quitMessage = miniMessage.deserialize(parsedMessage);
            event.quitMessage(quitMessage);
        }
    }

    public Map<String, List<String>> getMessages() {
        return messages;
    }

    private static final Map<String, Integer> groupWeights = new HashMap<>();

    static {
        groupWeights.put("iam", 5);
        groupWeights.put("javadper", 4);
        groupWeights.put("yosya", 4);
        groupWeights.put("admin", 3);
        groupWeights.put("developer", 2);
        groupWeights.put("moderator", 1);
        groupWeights.put("sponsor", 0);
    }

    private final Map<Integer, String> chatMessages = new HashMap<>();
    private final Map<Integer, Player> messageOwners = new HashMap<>();
    private int messageIdCounter = 0;

    public void deleteMessage(int messageId) {
        if (chatMessages.containsKey(messageId)) {
            // Логируем удаление сообщения
            plugin.getLogger().info("Deleting message with ID: " + messageId);

            // Заменяем сообщение текстом "<сообщение удалено>"
            chatMessages.put(messageId, "<gray><i><сообщение удалено></i></gray>");
            reloadChat();
        } else {
            plugin.getLogger().info("Message with ID " + messageId + " does not exist.");
        }
    }

    private void clearChat() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < 100; i++) {
                player.sendMessage("");
            }
        }
    }

    private void reloadChat() {
        clearChat(); // Очистка чата перед обновлением

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(miniMessage.deserialize("<gray>Чат обновлен</gray>"));
            chatMessages.forEach((id, message) -> {
                Component parsedMessage = miniMessage.deserialize(message);
                player.sendMessage(parsedMessage);
            });
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getDisplayName();
        String message = event.getMessage();

        // Генерация уникального идентификатора сообщения
        int messageId = messageIdCounter++;
        String formattedMessage = tagFormatter.format(message, player);

        // Добавляем кнопку удаления с `id` и показываем `id` в сообщении
        String prefix = playerNameStorage.getPlayerPrefix(player);
        String suffix = playerNameStorage.getPlayerSuffix(player);
        String deleteButton = "<click:run_command:/delete " + messageId + "><red>[✖]</red></click> ";
        String finalMessage = "<gray>[ID:" + messageId + "]</gray> " + deleteButton + prefix + playerName + suffix + " > " + formattedMessage;

        // Сохраняем сообщение с `id` в HashMap
        chatMessages.put(messageId, finalMessage);
        messageOwners.put(messageId, player);

        // Отправка сообщения всем игрокам
        Component parsedMessage = miniMessage.deserialize(finalMessage);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(parsedMessage);
        }

        event.setCancelled(true);
    }
}