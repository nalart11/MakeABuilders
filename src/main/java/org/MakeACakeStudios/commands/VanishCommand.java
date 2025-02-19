package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VanishCommand implements Command {

    private static final Set<UUID> vanishedPlayers = new HashSet<>();

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("vanish", "v")
                        .senderType(Player.class)
                        .permission("makeabuilders.vanish")
                        .handler(ctx -> {
                            Player player = ctx.sender();
                            Bukkit.getScheduler().runTask(MakeABuilders.instance, () -> toggleVanish(player));
                        })
        );
    }

    private void toggleVanish(@NotNull Player player) {
        UUID playerId = player.getUniqueId();
        String parsedMessage = "";

        if (vanishedPlayers.contains(playerId)) {
            vanishedPlayers.remove(playerId);
            List<String> joinMessages = MakeABuilders.instance.config.getStringList("Messages.Join");
            String rawMessage = ChatUtils.getRandomMessage(joinMessages);
            if (rawMessage != null) {
                parsedMessage = rawMessage.replace("<player>", ChatUtils.getFormattedPlayerString(player.getName(), true));
            }
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(MakeABuilders.instance, player);
                online.sendMessage(MiniMessage.miniMessage().deserialize(parsedMessage));
            }
            player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Вы <green>снова видимы</green> для всех игроков.</gray>"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
        } else {
            vanishedPlayers.add(playerId);
            List<String> quitMessages = MakeABuilders.instance.config.getStringList("Messages.Quit");
            String rawMessage = ChatUtils.getRandomMessage(quitMessages);
            if (rawMessage != null) {
                parsedMessage = rawMessage.replace("<player>", ChatUtils.getFormattedPlayerString(player.getName(), true));
            }
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.isOp()) {
                    online.hidePlayer(MakeABuilders.instance, player);
                }
                online.sendMessage(MiniMessage.miniMessage().deserialize(parsedMessage));
            }
            player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Вы <red>исчезли</red>! Теперь вас не видно для обычных игроков.</gray>"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        }
    }

    public static boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }
}