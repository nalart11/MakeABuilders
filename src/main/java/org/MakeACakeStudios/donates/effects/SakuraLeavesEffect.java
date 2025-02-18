package org.MakeACakeStudios.donates.effects;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.MakeACakeStudios.MakeABuilders;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SakuraLeavesEffect {
    private static final Map<UUID, BukkitRunnable> activeEffects = new HashMap<>();

    public static void startEffect(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null || !player.isOnline()) return;

        UUID playerUUID = player.getUniqueId();

        if (activeEffects.containsKey(playerUUID)) {
            return;
        }

        BukkitRunnable effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                Player p = Bukkit.getPlayer(playerUUID);
                if (p != null && p.isOnline()) {
                    p.getWorld().spawnParticle(Particle.CHERRY_LEAVES, p.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
                } else {
                    stopEffect(playerName);
                }
            }
        };

        effectTask.runTaskTimer(MakeABuilders.instance, 0L, 5L);
        activeEffects.put(playerUUID, effectTask);
    }

    public static void stopEffect(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) return;

        UUID playerUUID = player.getUniqueId();
        BukkitRunnable task = activeEffects.remove(playerUUID);
        if (task != null) {
            task.cancel();
        }
    }
}