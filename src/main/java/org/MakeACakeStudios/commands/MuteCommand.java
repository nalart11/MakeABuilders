package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.MakeACakeStudios.storage.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MuteCommand implements CommandExecutor {

    private final MakeABuilders plugin;
    private final Map<UUID, Long> mutedPlayers = new HashMap<>(); // Храним муты: ID игрока и время окончания мута
    private final PlayerNameStorage playerNameStorage;
    private final MiniMessage miniMessage;

    public MuteCommand(MakeABuilders plugin, PlayerNameStorage playerNameStorage) {
        this.plugin = plugin;
        this.playerNameStorage = playerNameStorage;
        this.miniMessage = MiniMessage.miniMessage();
        startMuteCheckTask(); // Запускаем проверку мута
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize("<red>Используйте: /mute <ник> <время> [причина]</red>"));
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Игрок не найден.</red>"));
            return false;
        }
//        if (target.equals("Nalart11_")) {
//            sender.sendMessage(miniMessage.deserialize("<red>Вы не можете замутить этого игрока.</red>"));
//            return false;
//        } else {
//            System.out.println("idk why but it's not workin'");
//        }

        String timeString = args[1];
        long muteDuration = parseTimeString(timeString);
        if (muteDuration == -1) {
            sender.sendMessage(miniMessage.deserialize("<red>Неправильный формат времени. Пример: 10s, 5m, 2h, Fv (навсегда)</red>"));
            return false;
        }

        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Не указано";

        mutePlayer(target, muteDuration, reason);

        String prefix = playerNameStorage.getPlayerPrefix(target);
        String suffix = playerNameStorage.getPlayerSuffix(target);
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

    private void mutePlayer(Player player, long duration, String reason) {
        long muteEndTime = (duration == Long.MAX_VALUE) ? Long.MAX_VALUE : System.currentTimeMillis() + duration;
        mutedPlayers.put(player.getUniqueId(), muteEndTime);

        String message = duration == Long.MAX_VALUE
                ? "<red>Вы были замьючены навсегда по причине: </red><gold>" + reason + "</gold>"
                : "<red>Вы были замьючены на " + (duration / 1000) + " секунд по причине:</red><gold> " + reason + "</gold>";

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        player.sendMessage(miniMessage.deserialize(message));
    }

    public boolean isMuted(Player player) {
        return mutedPlayers.containsKey(player.getUniqueId()) && mutedPlayers.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    public void unmutePlayer(Player player) {
        mutedPlayers.remove(player.getUniqueId());
    }

    public void checkMutedPlayers() {
        long currentTime = System.currentTimeMillis();
        mutedPlayers.entrySet().removeIf(entry -> entry.getValue() <= currentTime && entry.getValue() != Long.MAX_VALUE);
    }

    private void startMuteCheckTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkMutedPlayers, 0, 1200); // Проверка каждые 60 секунд
    }
}
