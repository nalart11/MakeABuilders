package org.MakeACakeStudios.donates.effects;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.MakeACakeStudios.MakeABuilders;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ZeusEffect {
    private static final Map<UUID, BukkitRunnable> activeEffects = new HashMap<>();
    private static final Random random = new Random();

    public static void startEffect(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null || !player.isOnline()) return;

        UUID playerUUID = player.getUniqueId();

        if (activeEffects.containsKey(playerUUID)) {
            return;
        }

        BukkitRunnable effectTask = new BukkitRunnable() {
            private double angle = 0;

            @Override
            public void run() {
                Player p = Bukkit.getPlayer(playerUUID);
                if (p != null && p.isOnline()) {
                    double baseY = p.getLocation().getY();
                    double angryY = baseY + 1.8;

                    for (int i = 0; i < 10; i++) {
                        double offsetX = (random.nextDouble() - 0.5) * 1.2;
                        double offsetZ = (random.nextDouble() - 0.5) * 1.2;
                        double ySmoke = baseY + (random.nextDouble() * 0.2);

                        p.getWorld().spawnParticle(Particle.SMOKE,
                                p.getLocation().getX() + offsetX,
                                ySmoke,
                                p.getLocation().getZ() + offsetZ,
                                1, 0, 0, 0, 0);
                    }

                    // Гневный эффект вокруг игрока
                    double radius = 0.3;
                    double x = p.getLocation().getX() + radius * Math.cos(angle);
                    double z = p.getLocation().getZ() + radius * Math.sin(angle);

                    p.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, x, angryY, z, 1, 0, 0, 0, 0);

                    angle += Math.PI / 8;
                    if (angle >= 2 * Math.PI) {
                        angle = 0;
                    }
                } else {
                    stopEffect(playerName);
                }
            }
        };

        effectTask.runTaskTimer(MakeABuilders.instance, 0L, 3L);
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