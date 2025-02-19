package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class AdminCommand implements Command {

    private final HashMap<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final HashMap<UUID, GameMode> savedGameModes = new HashMap<>();
    private final HashMap<UUID, BossBar> adminBossBars = new HashMap<>();

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("adm", "admin", "admin-mode")
                        .senderType(Player.class)
                        .permission("makeabuilders.admin")
                        .handler(ctx -> {
                            Player player = ctx.sender();
                            Bukkit.getScheduler().runTask(MakeABuilders.instance, () -> toggleAdminMode(player));
                        })
        );
    }

    private void toggleAdminMode(@NotNull Player player) {
        UUID playerId = player.getUniqueId();

        if (savedInventories.containsKey(playerId)) {
            restoreInventory(player);
            player.setGameMode(savedGameModes.get(playerId));
            player.setInvulnerable(false);
            if (savedGameModes.get(playerId) == GameMode.SURVIVAL) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }

            savedInventories.remove(playerId);
            savedGameModes.remove(playerId);

            if (adminBossBars.containsKey(playerId)) {
                adminBossBars.get(playerId).removeAll();
                adminBossBars.remove(playerId);
            }

            player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Вы вышли из <red>админ-режима</red>.</gray>"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
        } else {
            savedInventories.put(playerId, player.getInventory().getContents().clone());
            savedGameModes.put(playerId, player.getGameMode());

            player.getInventory().clear();
            player.setInvulnerable(true);
            player.setAllowFlight(true);
            player.setFlying(true);

            BossBar bossBar = Bukkit.createBossBar("Вы находитесь в режиме §cадминистратора", BarColor.RED, BarStyle.SOLID);
            bossBar.addPlayer(player);
            adminBossBars.put(playerId, bossBar);

            player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Вы вошли в <green>админ-режим</green>.</gray>"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        }
    }

    private void restoreInventory(Player player) {
        UUID playerId = player.getUniqueId();
        if (savedInventories.containsKey(playerId)) {
            player.getInventory().setContents(savedInventories.get(playerId));
        }
    }
}