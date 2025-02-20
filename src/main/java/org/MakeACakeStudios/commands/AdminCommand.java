package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class AdminCommand implements Command, Listener {

    private final HashMap<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final HashMap<UUID, GameMode> savedGameModes = new HashMap<>();
    private final HashMap<UUID, BossBar> adminBossBars = new HashMap<>();

    private static final NamespacedKey ADMIN_MODE_KEY = new NamespacedKey(MakeABuilders.instance, "admin_mode");

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
        Bukkit.getPluginManager().registerEvents(this, MakeABuilders.instance);
    }

    private void toggleAdminMode(@NotNull Player player) {
        UUID playerId = player.getUniqueId();
        PersistentDataContainer data = player.getPersistentDataContainer();

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

            data.remove(ADMIN_MODE_KEY);

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

            data.set(ADMIN_MODE_KEY, PersistentDataType.BYTE, (byte) 1);

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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer data = player.getPersistentDataContainer();

        if (data.has(ADMIN_MODE_KEY, PersistentDataType.BYTE)) {
            player.setInvulnerable(true);
            player.setAllowFlight(true);
            player.setFlying(true);

            BossBar bossBar = Bukkit.createBossBar("Вы находитесь в режиме §cадминистратора", BarColor.RED, BarStyle.SOLID);
            bossBar.addPlayer(player);
            adminBossBars.put(player.getUniqueId(), bossBar);

            player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Вы всё ещё в <green>админ-режиме</green>.</gray>"));
        }
    }
}
