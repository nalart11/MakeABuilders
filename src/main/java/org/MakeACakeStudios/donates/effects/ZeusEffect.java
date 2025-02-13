package org.MakeACakeStudios.donates.effects;

import org.MakeACakeStudios.donates.EffectManager;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.MakeACakeStudios.MakeABuilders;

import java.util.Random;

public class ZeusEffect {
    private static final String TARGET_PLAYER_NAME = "Nalart11_";
    private static double angle = 0;
    private static final Random random = new Random();

    public static void startEffect() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(TARGET_PLAYER_NAME);
                if (player != null && player.isOnline()) {
                    double baseY = player.getLocation().getY();
                    double angryY = baseY + 1.8;

                    for (int i = 0; i < 10; i++) {
                        double offsetX = (random.nextDouble() - 0.5) * 1.2;
                        double offsetZ = (random.nextDouble() - 0.5) * 1.2;
                        double ySmoke = baseY + (random.nextDouble() * 0.2);

                        player.getWorld().spawnParticle(Particle.LARGE_SMOKE,
                                player.getLocation().getX() + offsetX,
                                ySmoke,
                                player.getLocation().getZ() + offsetZ,
                                1, 0, 0, 0, 0);
                    }

                    double radius = 0.3;
                    double x = player.getLocation().getX() + radius * Math.cos(angle);
                    double z = player.getLocation().getZ() + radius * Math.sin(angle);

                    player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, x, angryY, z, 1, 0, 0, 0, 0);

                    angle += Math.PI / 8;
                    if (angle >= 2 * Math.PI) {
                        angle = 0;
                    }
                }
            }
        }.runTaskTimer(MakeABuilders.instance, 0L, 3L);
    }

    public static void register() {
        EffectManager.registerEffect("zeus_effect", ZeusEffect::startEffect);
    }
}
