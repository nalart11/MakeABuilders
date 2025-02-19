package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.ChatUtils;
import org.MakeACakeStudios.donates.EffectManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VanishCommand implements Command {

    private static final Set<UUID> vanishedPlayers = new HashSet<>();
    private static final Map<UUID, Set<Integer>> vanishedEffects = new HashMap<>();

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

            Set<Integer> effectsToRestore = vanishedEffects.remove(playerId);
            if (effectsToRestore != null) {
                for (int effect : effectsToRestore) {
                    EffectManager.startEffectForDonation(effect, player.getName());
                }
            }

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

            Set<Integer> activeEffects = EffectManager.getEnabledEffectsForPlayer(player.getName());
            if (!activeEffects.isEmpty()) {
                vanishedEffects.put(playerId, new HashSet<>(activeEffects)); // Сохраняем текущие эффекты
                for (int effect : activeEffects) {
                    EffectManager.stopEffectForDonation(effect, player.getName());
                }
            }

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