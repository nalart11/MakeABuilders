package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.MakeACakeStudios.storage.PunishmentStorage;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BanCommand implements CommandExecutor {
    private final MakeABuilders plugin;
    private final PlayerDataStorage playerDataStorage;
    private final PunishmentStorage punishmentStorage;
    private final MiniMessage miniMessage;
    private final List<String> timeUnits = Arrays.asList("s", "m", "h", "d", "w", "y", "Fv");

    public BanCommand(MakeABuilders plugin, PlayerDataStorage playerDataStorage, PunishmentStorage punishmentStorage, MiniMessage miniMessage) {
        this.plugin = plugin;
        this.playerDataStorage = playerDataStorage;
        this.punishmentStorage = punishmentStorage;
        this.miniMessage = miniMessage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize("<red>Используйте: /ban <ник> <время> [причина]</red>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Игрок не найден.</red>"));
            return true;
        }

        String timeString = args[1];
        long banDuration = parseTimeString(timeString);
        if (banDuration == -1) {
            sender.sendMessage(miniMessage.deserialize("<red>Неправильный формат времени. Пример: 10s, 5m, 2h, Fv (навсегда)</red>"));
            return true;
        }

        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Не указано";

        banPlayer(target, banDuration, reason, sender.getName());

        return false;
    }

    private long parseTimeString(String timeString) {
        if (timeString.equalsIgnoreCase("Fv")) {
            return Long.MAX_VALUE;
        }

        try {
            char unit = timeString.charAt(timeString.length() - 1);
            long time = Long.parseLong(timeString.substring(0, timeString.length() - 1));

            switch (unit) {
                case 's':
                    return TimeUnit.SECONDS.toMillis(time);
                case 'm':
                    return TimeUnit.MINUTES.toMillis(time);
                case 'h':
                    return TimeUnit.HOURS.toMillis(time);
                case 'd':
                    return TimeUnit.DAYS.toMillis(time);
                case 'w':
                    return TimeUnit.DAYS.toMillis(time * 7);
                case 'y':
                    return TimeUnit.DAYS.toMillis(time * 365);
                default:
                    return -1;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void banPlayer(Player player, long duration, String reason, String admin) {
        long muteEndTime = (duration == Long.MAX_VALUE) ? Long.MAX_VALUE : System.currentTimeMillis() + duration;
        String endTime = (muteEndTime == Long.MAX_VALUE) ? "Fv" : String.valueOf(muteEndTime);

        punishmentStorage.addBan(player.getName(), admin, reason, endTime);
        Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason, null, admin);
        player.kickPlayer("Вы были забанены.");
    }
}