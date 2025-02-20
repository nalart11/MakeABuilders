package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.ChatUtils;
import org.MakeACakeStudios.donates.EffectManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VanishCommand implements Command {

    public static final NamespacedKey VANISH_KEY = new NamespacedKey(MakeABuilders.instance, "vanish");
    public static final Set<UUID> vanishedPlayers = new HashSet<>();
    public static final Map<UUID, Set<Integer>> vanishedEffects = new HashMap<>();
    public static final Map<UUID, BossBar> vanishedBossBars = new HashMap<>();

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
        PersistentDataContainer data = player.getPersistentDataContainer();
        String parsedMessage = "";

        if (vanishedPlayers.contains(playerId)) {
            vanishedPlayers.remove(playerId);
            data.set(VANISH_KEY, PersistentDataType.BYTE, (byte) 0);

            Set<Integer> effectsToRestore = vanishedEffects.remove(playerId);
            if (effectsToRestore != null) {
                for (int effect : effectsToRestore) {
                    EffectManager.startEffectForDonation(effect, player.getName());
                }
            }

            if (vanishedBossBars.containsKey(playerId)) {
                vanishedBossBars.get(playerId).removeAll();
                vanishedBossBars.remove(playerId);
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
            data.set(VANISH_KEY, PersistentDataType.BYTE, (byte) 1); // Сохраняем состояние

            Set<Integer> activeEffects = EffectManager.getEnabledEffectsForPlayer(player.getName());
            if (!activeEffects.isEmpty()) {
                vanishedEffects.put(playerId, new HashSet<>(activeEffects));
                for (int effect : activeEffects) {
                    EffectManager.stopEffectForDonation(effect, player.getName());
                }
            }

            BossBar bossBar = Bukkit.createBossBar("Вы находитесь в §bванише", BarColor.WHITE, BarStyle.SOLID);
            bossBar.addPlayer(player);
            vanishedBossBars.put(playerId, bossBar);

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