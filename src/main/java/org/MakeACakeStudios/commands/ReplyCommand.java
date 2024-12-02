package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReplyCommand implements CommandExecutor {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final MakeABuilders plugin;
    private final TagFormatter tagFormatter;
    private final PlayerDataStorage playerDataStorage;

    public ReplyCommand(MakeABuilders plugin, PlayerDataStorage playerDataStorage) {
        this.plugin = plugin;
        this.playerDataStorage = playerDataStorage;
        this.tagFormatter = new TagFormatter(plugin);
    }

    private String getPlayerPrefix(Player player) {
        return plugin.getPlayerPrefix(player);
    }

    private String getPlayerSuffix(Player player) {
        return plugin.getPlayerSuffix(player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cТолько игроки могут использовать эту команду.");
            return true;
        }

        Player playerSender = (Player) sender;

        Player target = plugin.getLastMessaged(playerSender);
        if (target == null) {
            playerSender.sendMessage(miniMessage.deserialize("<red>Нет игрока, которому можно ответить.</red>"));
            return true;
        }

        if (args.length < 1) {
            playerSender.sendMessage(miniMessage.deserialize("<red>Использование: /r <сообщение></red>"));
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }

        String senderPrefix = getPlayerPrefix(playerSender);
        String senderSuffix = getPlayerSuffix(playerSender);
        String targetPrefix = getPlayerPrefix(target);
        String targetSuffix = getPlayerSuffix(target);

        String formattedMessage = tagFormatter.format(message.toString().trim(), playerSender);
        String finalMessage = senderPrefix + playerSender.getName() + senderSuffix + " <yellow>→</yellow> "
                + targetPrefix + target.getName() + targetSuffix + ": <gray>" + formattedMessage + "</gray>";

        Component parsedMessage = miniMessage.deserialize(finalMessage);

        target.sendMessage(parsedMessage);
        playerSender.sendMessage(parsedMessage);

        Sound selectedSound = plugin.getPlayerSound(target);

        target.playSound(target.getLocation(), selectedSound, 1.0F, 1.0F);

        plugin.setLastMessaged(playerSender, target);

        return true;
    }
}