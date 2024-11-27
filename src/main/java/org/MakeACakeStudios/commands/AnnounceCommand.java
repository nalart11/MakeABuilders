package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AnnounceCommand implements CommandExecutor {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final MakeABuilders plugin;
    private final TagFormatter tagFormatter;
    private final PlayerDataStorage playerDataStorage;
    
    public AnnounceCommand(MakeABuilders plugin, PlayerDataStorage playerDataStorage) {
        this.plugin = plugin;
        this.playerDataStorage = playerDataStorage;
        this.tagFormatter = new TagFormatter(playerDataStorage);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cИспользуйте: /announce <сообщение>");
            return false;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            MuteCommand muteCommand = (MuteCommand) plugin.getCommand("mute").getExecutor();

            if (muteCommand.isMuted(player)) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
                player.sendMessage(miniMessage.deserialize("<red>Вы замьючены и не можете отправлять сообщения.</red>"));
                return true;
            }
        }

        String message = String.join(" ", args);
        message = tagFormatter.format(message, (Player) sender);

        String senderDisplayName;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String prefix = plugin.getPlayerNameStorage().getPlayerPrefix(player);
            String suffix = plugin.getPlayerNameStorage().getPlayerSuffix(player);
            senderDisplayName = prefix + player.getName() + suffix;
        } else {
            String prefix = "<blue>\uD83D\uDCBB</blue> <gradient:#FF3D4D:#FCBDBD>";
            String suffix = "</gradient>";
            senderDisplayName = prefix + "MakeABuilders.console" + suffix;
        }

        String finalMessage = "<gray><----------------></gray>\n\n" +
                "<bold><red>!!СРОЧНОЕ ОПОВЕЩЕНИЕ!!</red></bold>\n" +
                "<gray>Инициатор: <yellow>" + senderDisplayName + "</yellow></gray>\n" +
                "<gray>Сообщение: </gray>" +
                "<white>" + message + "</white>\n\n" +
                "<gray><----------------></gray>";

        Component parsedMessage = miniMessage.deserialize(finalMessage);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(parsedMessage);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2.0f, 2.0f);
        }

        return true;
    }
}
