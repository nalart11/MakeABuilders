package org.MakeACakeStudios.other;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.MakeACakeStudios.storage.PunishmentStorage;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BanExpirationTask extends BukkitRunnable {

    private final PunishmentStorage punishmentStorage;
    private final Set<String> bannedPlayers = new HashSet<>();
    private final MiniMessage miniMessage;

    public BanExpirationTask(PunishmentStorage punishmentStorage, MiniMessage miniMessage) {
        this.punishmentStorage = punishmentStorage;
        this.miniMessage = miniMessage;
    }

    public void addPlayerToBanCheck(String playerName) {
        bannedPlayers.add(playerName);
    }

    public void removePlayerFromBanCheck(String playerName) {
        bannedPlayers.remove(playerName);
    }

    @Override
    public void run() {
        Iterator<String> iterator = bannedPlayers.iterator();

        while (iterator.hasNext()) {
            String playerName = iterator.next();
            long banEndTime = punishmentStorage.getBanEndTime(playerName);

            if (banEndTime != Long.MAX_VALUE && banEndTime <= System.currentTimeMillis()) {
                iterator.remove();
                punishmentStorage.pardonPlayer(playerName);
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    removePlayerFromBanCheck(playerName);
                }
            }
        }
    }
}