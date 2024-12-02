package org.MakeACakeStudios.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.commands.MuteCommand;
import org.MakeACakeStudios.storage.PunishmentStorage;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class ChatHandler implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final FileConfiguration config;
    private final MakeABuilders plugin;
    private final Map<String, List<String>> messages = new HashMap<>();
    private final PlayerDataStorage playerDataStorage;
    private final PunishmentStorage punishmentStorage;
    private final TagFormatter tagFormatter;

    public ChatHandler(MakeABuilders makeABuilders, PunishmentStorage punishmentStorage) {
        this.config = makeABuilders.getConfig();
        this.plugin = makeABuilders;
        this.punishmentStorage = punishmentStorage;
        this.playerDataStorage = new PlayerDataStorage(plugin);
        this.tagFormatter = new TagFormatter(plugin);
    }

    private String getRandomMessage(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return messages.get(random.nextInt(messages.size()));
    }

    private Component getFormattedPlayerName(Player player) {
        String prefix = plugin.getPlayerPrefix(player);
        String suffix = plugin.getPlayerSuffix(player);
        return miniMessage.deserialize(prefix + player.getName() + suffix);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(miniMessage.deserialize("<gradient:#FF3D4D:#FCBDBD>С возвращением!</gradient>"));

        String prefix = plugin.getPlayerPrefix(player);
        String suffix = plugin.getPlayerSuffix(player);
        List<String> joinMessages = config.getStringList("Messages.Join");
        String rawMessage = getRandomMessage(joinMessages);

        if (rawMessage != null) {
            String parsedMessage = rawMessage.replace("<player>", prefix + player.getName() + suffix);
            Component joinMessage = miniMessage.deserialize(parsedMessage);

            int messageId = messageIdCounter++;
            chatMessages.put(messageId, parsedMessage);
            messageOwners.put(messageId, player);

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
        String prefix = plugin.getPlayerPrefix(player);
        String suffix = plugin.getPlayerSuffix(player);
        List<String> quitMessages = config.getStringList("Messages.Quit");
        String rawMessage = getRandomMessage(quitMessages);

        if (rawMessage != null) {
            String parsedMessage = rawMessage.replace("<player>", prefix + player.getName() + suffix);
            Component quitMessage = miniMessage.deserialize(parsedMessage);

            int messageId = messageIdCounter++;
            chatMessages.put(messageId, parsedMessage);
            messageOwners.put(messageId, player);

            event.quitMessage(quitMessage);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Component originalDeathMessage = event.deathMessage();

        if (originalDeathMessage != null) {
            Component customDeathMessage = originalDeathMessage.replaceText(builder ->
                    builder.matchLiteral(player.getName()).replacement(getFormattedPlayerName(player))
            );

            String renderedMessage = miniMessage.serialize(customDeathMessage);

            int messageId = messageIdCounter++;
            chatMessages.put(messageId, renderedMessage);
            messageOwners.put(messageId, player);

            event.deathMessage(customDeathMessage);
        }
    }

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        Component originalMessage = event.message();

        if (originalMessage != null) {
            Component customAdvancementMessage = originalMessage.replaceText(builder ->
                    builder.matchLiteral(player.getName()).replacement(getFormattedPlayerName(player))
            );

            String renderedMessage = miniMessage.serialize(customAdvancementMessage);

            int messageId = messageIdCounter++;
            chatMessages.put(messageId, renderedMessage);
            messageOwners.put(messageId, player);

            event.message(customAdvancementMessage);
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
    private final Map<Integer, String> originalMessages = new HashMap<>();
    private final Map<Integer, Player> messageOwners = new HashMap<>();
    private int messageIdCounter = 0;

    public void deleteMessage(int messageId) {
        if (chatMessages.containsKey(messageId)) {
            plugin.getLogger().info("Deleting message with ID: " + messageId);

            String originalMessage = chatMessages.get(messageId);
            originalMessages.put(messageId, originalMessage);

            Player messageOwner = messageOwners.get(messageId);
            String clickableName;
            if (messageOwner != null) {
                String prefix = plugin.getPlayerPrefix(messageOwner);
                String suffix = plugin.getPlayerSuffix(messageOwner);
                String playerName = messageOwner.getName();

                clickableName = "<click:suggest_command:'/msg " + playerName + " '>"
                        + "<hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы отправить сообщение игроку " + prefix + playerName + suffix + ".'>"
                        + prefix + playerName + suffix + "</hover></click>";
            } else {
                clickableName = "<unknown>";
            }

            String deletedMessageFormatWithRole = "<gray>[ID:" + messageId + "]</gray> "
                    + "<click:run_command:/retmsg " + messageId + "><green>[✔]</green></click> "
                    + clickableName + "> <gray><i><сообщение удалено></i></gray>";

            chatMessages.put(messageId, deletedMessageFormatWithRole);
            reloadChat();
        } else {
            plugin.getLogger().info("Message with ID " + messageId + " does not exist.");
        }
    }

    public boolean restoreMessage(int messageId) {
        if (originalMessages.containsKey(messageId)) {
            String originalMessage = originalMessages.get(messageId);
            chatMessages.put(messageId, originalMessage);
            plugin.getLogger().info("Restoring message with ID: " + messageId);
            reloadChat();
            return true;
        } else {
            plugin.getLogger().info("Original message with ID " + messageId + " does not exist.");
            return false;
        }
    }

    private void reloadChat() {
        clearChat();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String role = playerDataStorage.getPlayerRoleByName(player.getName());

            chatMessages.forEach((id, message) -> {
                String formattedMessage;
                if (role != null && !role.equals("player")) {
                    formattedMessage = message;
                } else {
                    formattedMessage = message.replaceAll("<gray>\\[ID:\\d+\\]</gray> ", "")
                            .replaceAll("<click:run_command:/remmsg \\d+><red>\\[✖]</red></click> ", "")
                            .replaceAll("<click:run_command:/retmsg \\d+><green>\\[✔]</green></click> ", "");
                }

                Component parsedMessage = miniMessage.deserialize(formattedMessage);
                player.sendMessage(parsedMessage);
            });
        }
    }

    private void clearChat() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < 100; i++) {
                player.sendMessage("");
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getDisplayName();
        String message = event.getMessage();

        String muteStatus = punishmentStorage.checkMute(player.getName());

        if (!muteStatus.contains("не замьючен")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
            player.sendMessage(miniMessage.deserialize("<red>Вы замьючены и не можете отправлять сообщения.</red>"));
            event.setCancelled(true);
            return;
        }

        System.out.println(playerName + " → " + message);

        if (message.contains("@")) {
            String[] words = message.split(" ");
            for (String word : words) {
                if (word.startsWith("@")) {
                    String mentionedPlayerName = word.substring(1);
                    Player mentionedPlayer = Bukkit.getPlayerExact(mentionedPlayerName);

                    if (mentionedPlayer != null && mentionedPlayer.isOnline()) {
                        Sound notificationSound = plugin.getPlayerSound(mentionedPlayer);
                        mentionedPlayer.playSound(mentionedPlayer.getLocation(), notificationSound, 1.0f, 1.0f);

                        String mentionedPlayerPrefix = plugin.getPlayerPrefix(mentionedPlayer);
                        String mentionedPlayerSuffix = plugin.getPlayerSuffix(mentionedPlayer);

                        String formattedMention = "<yellow>@" + mentionedPlayerPrefix + mentionedPlayer.getName() + mentionedPlayerSuffix + "</yellow>";
                        message = message.replace(word, formattedMention);
                    } else {
                        String formattedMention = "<yellow>@" + mentionedPlayerName + "</yellow>";
                        message = message.replace(word, formattedMention);
                    }
                }
            }
        }

        int messageId = messageIdCounter++;
        String formattedMessage = tagFormatter.format(message, player);

        String prefix = plugin.getPlayerPrefix(player);
        String suffix = plugin.getPlayerSuffix(player);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (playerDataStorage.getPlayerRoleByName(onlinePlayer.getName()) != null && !playerDataStorage.getPlayerRoleByName(onlinePlayer.getName()).equals("player")) {
                String deleteButton = "<click:run_command:/remmsg " + messageId + "><red>[✖]</red></click> ";
                String messageIdTag = "<gray>[ID:" + messageId + "]</gray> ";

                String clickableName = "<click:suggest_command:'/msg " + player.getName() + " '>"
                        + "<hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы отправить сообщение игроку " + prefix + playerName + suffix + ".'>"
                        + prefix + playerName + suffix + "</hover></click> ";

                String finalMessage = messageIdTag + deleteButton + clickableName + "> " + formattedMessage;

                chatMessages.put(messageId, finalMessage);
                messageOwners.put(messageId, player);

                Component staffMessage = miniMessage.deserialize(finalMessage);
                onlinePlayer.sendMessage(staffMessage);
                }
            else {
                String finalMessage = "<click:suggest_command:'/msg " + player.getName() + " '>"
                        + "<hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы отправить сообщение игроку " + prefix + playerName + suffix + ".'>"
                        + prefix + playerName + suffix + " > " + formattedMessage;

                Component playerMessage = miniMessage.deserialize(finalMessage);
                onlinePlayer.sendMessage(playerMessage);
            }
        }

        event.setCancelled(true);
    }
}