package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.other.MuteExpirationTask;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.MakeACakeStudios.storage.PunishmentStorage;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MuteCommand implements CommandExecutor {

    private final MakeABuilders plugin;
    private final PunishmentStorage punishmentStorage;
    private final MiniMessage miniMessage;
    private final MuteExpirationTask muteExpirationTask;
    private final List<String> timeUnits = Arrays.asList("s", "m", "h", "d", "w", "y", "Fv");

    public MuteCommand(MakeABuilders plugin, PunishmentStorage punishmentStorage, MuteExpirationTask muteExpirationTask) {
        this.plugin = plugin;
        this.punishmentStorage = punishmentStorage;
        this.muteExpirationTask = muteExpirationTask;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize("<red>Используйте: /mute <ник> <время> [причина]</red>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Игрок не найден.</red>"));
            return true;
        }

        String timeString = args[1];
        long muteDuration = parseTimeString(timeString);
        if (muteDuration == -1) {
            sender.sendMessage(miniMessage.deserialize("<red>Неправильный формат времени. Пример: 10s, 5m, 2h, Fv (навсегда)</red>"));
            return true;
        }

        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Не указано";

        mutePlayer(target, muteDuration, reason, sender.getName());

        String prefix = plugin.getPlayerPrefix(target);
        String suffix = plugin.getPlayerSuffix(target);
        String formattedName = prefix + target.getName() + suffix;

        if (timeString.equals("Fv")) {
            sender.sendMessage(miniMessage.deserialize("<green>✔ Игрок " + formattedName + " был замьючен навсегда" + " по причине: " + reason + "</green>"));
        } else {
            sender.sendMessage(miniMessage.deserialize("<green>✔ Игрок " + formattedName + " был замьючен на " + timeString + " по причине: " + reason + "</green>"));
        }

        return true;
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
                    return -1; // неправильный формат
            }
        } catch (NumberFormatException e) {
            return -1; // ошибка в формате
        }
    }

    private void mutePlayer(Player player, long duration, String reason, String admin) {
        long muteEndTime = (duration == Long.MAX_VALUE) ? Long.MAX_VALUE : System.currentTimeMillis() + duration;
        String endTime = (muteEndTime == Long.MAX_VALUE) ? "Fv" : String.valueOf(muteEndTime);

        muteExpirationTask.addPlayerToMuteCheck(player.getName());
        punishmentStorage.addMute(player.getName(), admin, reason, endTime);

        String message = duration == Long.MAX_VALUE
                ? "<red>Вы были замьючены навсегда по причине: </red><gold>" + reason + "</gold>"
                : "<red>Вы были замьючены на " + (duration / 1000) + " секунд по причине:</red><gold> " + reason + "</gold>";

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        player.sendMessage(miniMessage.deserialize(message));
    }

    public boolean isMuted(Player player) {
        String muteStatus = punishmentStorage.checkMute(player.getName());
        return !muteStatus.contains("не замьючен"); // Returns true if player is muted
    }
}
