package org.MakeACakeStudios.other;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.MakeACakeStudios.storage.PunishmentStorage;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MuteExpirationTask extends BukkitRunnable {

    private final PunishmentStorage punishmentStorage;
    private final Set<String> mutedPlayers = new HashSet<>();
    private final MiniMessage miniMessage;

    public MuteExpirationTask(PunishmentStorage punishmentStorage, MiniMessage miniMessage) {
        this.punishmentStorage = punishmentStorage;
        this.miniMessage = miniMessage;
    }

    public void addPlayerToMuteCheck(String playerName) {
        mutedPlayers.add(playerName);
    }

    public void removePlayerFromMuteCheck(String playerName) {
        mutedPlayers.remove(playerName);
    }

    @Override
    public void run() {
        Iterator<String> iterator = mutedPlayers.iterator();

        while (iterator.hasNext()) {
            String playerName = iterator.next();
            long muteEndTime = punishmentStorage.getMuteEndTime(playerName); // Получаем время окончания мьюта

            if (muteEndTime <= System.currentTimeMillis()) {
                iterator.remove(); // Убираем игрока из списка
                punishmentStorage.unmutePlayer(playerName); // Обновляем базу, чтобы снять мьют
                Player player = Bukkit.getPlayer(playerName);
                if (player != null && player.isOnline()) {
                    player.sendMessage(miniMessage.deserialize("<green>Ваш мут истек. Теперь вы можете общаться.<green>"));
                    removePlayerFromMuteCheck(playerName);
                }
            }
        }
    }
}