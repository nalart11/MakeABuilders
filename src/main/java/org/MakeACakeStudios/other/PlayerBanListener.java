package org.MakeACakeStudios.other;

import org.MakeACakeStudios.storage.PunishmentStorage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerBanListener implements Listener {

    private final PunishmentStorage punishmentStorage;

    public PlayerBanListener(PunishmentStorage punishmentStorage) {
        this.punishmentStorage = punishmentStorage;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        String playerName = event.getPlayer().getName();

        long banEndTime = punishmentStorage.getBanEndTime(playerName);
        if (banEndTime > 0) {
            String admin = punishmentStorage.getBanAdmin(playerName);
            String reason = punishmentStorage.getBanReason(playerName);
            int banNumber = punishmentStorage.getBanNumber(playerName);

            String formattedEndTime = (banEndTime == Long.MAX_VALUE)
                    ? "навсегда"
                    : formatBanTime(banEndTime);

            String timeText = formattedEndTime.equals("навсегда")
                    ? "§aнавсегда"
                    : "§fдо §a" + formattedEndTime;

            String banMessage = String.format("""
                §c[Бан #%d]
                §fВы были забанены администратором §6%s §c%s §fпо причине: §e%s.
                §fЕсли вы считаете что это ошибка вы можете обратиться к администрации: §ahttps://discord.gg/Ac9CSskbTf
                """,
                    banNumber,
                    admin,
                    timeText,
                    reason
            );


            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, banMessage);
        }
    }

    private String formatBanTime(long banEndTime) {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
        return dateFormat.format(new java.util.Date(banEndTime));
    }
}
