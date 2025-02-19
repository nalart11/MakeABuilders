package org.MakeACakeStudios.donates.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.MakeACakeStudios.MakeABuilders;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class BirthdayEffect {
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
                if (p == null || !p.isOnline()) {
                    stopEffect(playerName);
                    return;
                }

                Location baseLocation = p.getEyeLocation().add(0, 0.8, 0);
                Location additionalLocation = p.getLocation().add(0, 0.5, 0);

                double offsetX = ThreadLocalRandom.current().nextDouble(-0.4, 0.4);
                double offsetZ = ThreadLocalRandom.current().nextDouble(-0.4, 0.4);

                Location particleBaseLocation = baseLocation.add(offsetX, 0, offsetZ);
                Location particleAdditionalLocation = additionalLocation.add(offsetX, 0, offsetZ);

                spawnNoteParticle(p, particleBaseLocation);
                spawnTotemParticle(p, particleAdditionalLocation);
            }
        };

        effectTask.runTaskTimer(MakeABuilders.instance, 0L, 5L);
        activeEffects.put(playerUUID, effectTask);
    }

    private static void spawnNoteParticle(Player player, Location location) {
        player.getWorld().spawnParticle(
                Particle.NOTE,
                location.getX(), location.getY(), location.getZ(),
                1,
                0, 0, 0
        );
    }

    private static void spawnTotemParticle(Player player, Location location) {
        player.getWorld().spawnParticle(
                Particle.TOTEM_OF_UNDYING,
                location.getX(), location.getY(), location.getZ(),
                5,
                0, 0, 0,
                0.02
        );
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
