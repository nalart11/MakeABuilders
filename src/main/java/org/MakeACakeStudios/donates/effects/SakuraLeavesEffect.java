package org.MakeACakeStudios.donates.effects;

import org.MakeACakeStudios.donates.EffectManager;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.MakeACakeStudios.MakeABuilders;

public class SakuraLeavesEffect {
    private static final String TARGET_PLAYER_NAME = "Nalart11_";

    public static void startEffect() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(TARGET_PLAYER_NAME);
                if (player != null && player.isOnline()) {
                    player.getWorld().spawnParticle(Particle.CHERRY_LEAVES, player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
                }
            }
        }.runTaskTimer(MakeABuilders.instance, 0L, 5L);
    }

    public static void register() {
        EffectManager.registerEffect("sakura_leaves", SakuraLeavesEffect::startEffect);
    }
}
