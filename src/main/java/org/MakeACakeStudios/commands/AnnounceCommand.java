package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
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
    
    public AnnounceCommand(MakeABuilders plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cИспользуйте: /announce <сообщение>");
            return false;
        }

        String message = String.join(" ", args);

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
