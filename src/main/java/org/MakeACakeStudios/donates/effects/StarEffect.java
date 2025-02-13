package org.MakeACakeStudios.donates.effects;

import org.MakeACakeStudios.donates.EffectManager;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.MakeACakeStudios.MakeABuilders;

public class StarEffect {
    private static final String TARGET_PLAYER_NAME = "Nalart11_"; // Имя игрока, на которого действует эффект

    public static void startEffect() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(TARGET_PLAYER_NAME);
                if (player == null || !player.isOnline()) {
                    return; // Если игрок оффлайн, просто пропускаем тик
                }

                // Создаём эффект над игроком
                player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 2, 0), 5, 0.5, 0.5, 0.5, 0.1);
            }
        }.runTaskTimer(MakeABuilders.instance, 0L, 5L); // Запускаем каждые 5 тиков (0.25 секунды)
    }

    public static void register() {
        EffectManager.registerEffect("firework", StarEffect::startEffect);
    }
}
