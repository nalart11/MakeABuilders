package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.other.BanExpirationTask;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
    private final BanExpirationTask banExpirationTask;
    private final List<String> timeUnits = Arrays.asList("s", "m", "h", "d", "w", "y", "Fv");
    private TagFormatter tagFormatter;

    public BanCommand(MakeABuilders plugin, PlayerDataStorage playerDataStorage, PunishmentStorage punishmentStorage, MiniMessage miniMessage, BanExpirationTask banExpirationTask, TagFormatter tagFormatter) {
        this.plugin = plugin;
        this.playerDataStorage = playerDataStorage;
        this.punishmentStorage = punishmentStorage;
        this.miniMessage = miniMessage;
        this.banExpirationTask = banExpirationTask;
        this.tagFormatter = tagFormatter;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize("<red>Используйте: /ban <ник> <время> [причина]</red>"));
            return true;
        }

        String playerName = args[0];
        String timeString = args[1];
        long banDuration = parseTimeString(timeString);

        if (banDuration == -1) {
            sender.sendMessage(miniMessage.deserialize("<red>Неправильный формат времени. Пример: 10s, 5m, 2h, Fv (навсегда)</red>"));
            return true;
        }

        Player onlinePlayer = Bukkit.getPlayer(playerName);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        if (onlinePlayer == null && !offlinePlayer.hasPlayedBefore()) {
            sender.sendMessage(miniMessage.deserialize("<red>Игрок не найден или никогда не заходил на сервер.</red>"));
            return true;
        }

        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Не указано";
        String admin = sender.getName();

        if (onlinePlayer != null) {
            String prefix = plugin.getPlayerPrefix(onlinePlayer);
            String suffix = plugin.getPlayerSuffix(onlinePlayer);
            String formattedName = prefix + onlinePlayer.getName() + suffix;

            banPlayer(onlinePlayer, banDuration, reason, admin);

            if (timeString.equals("Fv")) {
                sender.sendMessage(miniMessage.deserialize("<green>✔ Игрок " + formattedName + " был забанен навсегда по причине: " + reason + "</green>"));
            } else {
                sender.sendMessage(miniMessage.deserialize("<green>✔ Игрок " + formattedName + " был забанен на " + timeString + " по причине: " + reason + "</green>"));
            }
        } else {
            String prefix = playerDataStorage.getPlayerPrefixByName(offlinePlayer.getName());
            String suffix = playerDataStorage.getPlayerSuffixByName(offlinePlayer.getName());
            String formattedName = prefix + offlinePlayer.getName() + suffix;

            banOfflinePlayer(offlinePlayer, banDuration, reason, admin);

            Player adminPlayer = Bukkit.getPlayer(admin);

            reason = tagFormatter.format(reason, adminPlayer);

            if (timeString.equals("Fv")) {
                sender.sendMessage(miniMessage.deserialize("<green>✔ Игрок " + formattedName + " был забанен навсегда по причине: " + reason + "</green>"));
            } else {
                sender.sendMessage(miniMessage.deserialize("<green>✔ Игрок " + formattedName + " был забанен на " + timeString + " по причине: " + reason + "</green>"));
            }
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
                    return -1;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void banPlayer(Player player, long duration, String reason, String admin) {
        long banEndTime = (duration == Long.MAX_VALUE) ? Long.MAX_VALUE : System.currentTimeMillis() + duration;

        String formattedEndTime = (banEndTime == Long.MAX_VALUE) ? "навсегда" : formatBanTime(banEndTime);
        String banMessage = buildBanMessage(admin, formattedEndTime, reason);

        banExpirationTask.addPlayerToBanCheck(player.getName());
        punishmentStorage.addBan(player.getName(), admin, reason, String.valueOf(banEndTime)); // В миллисекундах

        Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason, null, admin);

        player.kickPlayer(banMessage);

        Integer BanNumber = punishmentStorage.getBanNumber(player.getName());

        String playerPrefix = plugin.getPlayerPrefix(player);
        String playerSuffix = plugin.getPlayerSuffix(player);
        String playerName = playerPrefix + player.getName() + playerSuffix;

        String adminPrefix = playerDataStorage.getPlayerPrefixByName(admin);
        String adminSuffix = playerDataStorage.getPlayerSuffixByName(admin);
        String adminName = adminPrefix + admin + adminSuffix;

        Player adminPlayer = Bukkit.getPlayer(admin);

        String chatMessage = "<red>[Бан #" + BanNumber + "]</red> Игрок " + playerName + " был забанен " + adminName + " <red>" + formattedEndTime + "</red> по причине: <yellow>" + reason + "</yellow>";
        String finalChatMessage = tagFormatter.format(chatMessage, adminPlayer);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(miniMessage.deserialize(finalChatMessage));
        }
    }

    private void banOfflinePlayer(OfflinePlayer player, long duration, String reason, String admin) {
        long banEndTime = (duration == Long.MAX_VALUE) ? Long.MAX_VALUE : System.currentTimeMillis() + duration;

        banExpirationTask.addPlayerToBanCheck(player.getName());
        punishmentStorage.addBan(player.getName(), admin, reason, String.valueOf(banEndTime));

        Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason, null, admin);

        String formattedEndTime = (banEndTime == Long.MAX_VALUE) ? "навсегда" : formatBanTime(banEndTime);

        Integer BanNumber = punishmentStorage.getBanNumber(player.getName());

        String playerPrefix = playerDataStorage.getPlayerPrefixByName(player.getName());
        String playerSuffix = playerDataStorage.getPlayerSuffixByName(player.getName());
        String playerName = playerPrefix + player.getName() + playerSuffix;

        String adminPrefix = playerDataStorage.getPlayerPrefixByName(admin);
        String adminSuffix = playerDataStorage.getPlayerSuffixByName(admin);
        String adminName = adminPrefix + admin + adminSuffix;

        Player adminPlayer = Bukkit.getPlayer(admin);

        String chatMessage = "<red>[Бан #" + BanNumber + "]</red> Игрок " + playerName + " был забанен " + adminName + " <red>" + formattedEndTime + "</red> по причине: <yellow>" + reason + "</yellow>";
        String finalChatMessage = tagFormatter.format(chatMessage, adminPlayer);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(miniMessage.deserialize(finalChatMessage));
        }
    }

    private String buildBanMessage(String admin, String endTime, String reason) {
        String timeText = endTime.equals("навсегда") ? "§aнавсегда" : "§6до §a" + endTime;

        return String.format("""
        §c[Бан #%d]
        §fВы были забанены администратором §6%s §c%s §fпо причине: §e%s.
        §fЕсли вы считаете что это ошибка вы можете обратиться к администрации: §ahttps://discord.gg/Ac9CSskbTf
        """,
                punishmentStorage.getNextBanNumber(),
                admin,
                timeText,
                reason
        );
    }

    private String formatBanTime(long banEndTime) {
        if (banEndTime == Long.MAX_VALUE) {
            return "навсегда";
        }

        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
        return dateFormat.format(new java.util.Date(banEndTime));
    }
}