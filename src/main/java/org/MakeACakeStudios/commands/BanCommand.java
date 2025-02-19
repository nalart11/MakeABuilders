package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.ChatUtils;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.other.BanExpirationTask;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.MakeACakeStudios.storage.PunishmentStorage;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;


public class BanCommand implements Command {

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("ban")
                        .permission("makeabuilders.ban")
                        .required("player", OfflinePlayerParser.offlinePlayerParser())
                        .required("time", StringParser.stringParser())
                        .optional("reason", StringParser.greedyStringParser(), DefaultValue.constant("Не указано."))
                        .handler(ctx -> handle(ctx.sender(), ctx.get("player"), ctx.get("time"), ctx.get("reason")))
                        .build()
        );
    }

    private void handle(@NonNull CommandSender sender, @NotNull OfflinePlayer offlinePlayer, String time, String reason) {
        Player target = Bukkit.getPlayer(offlinePlayer.getUniqueId());

        String playerName = offlinePlayer.getName();

        if (PunishmentStorage.instance.isBanned(playerName)) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Игрок уже забанен.</red>"));
            return;
        } else if (!offlinePlayer.isOnline() && offlinePlayer.hasPlayedBefore() != true) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Игрок не найден!</red>"));
            return;
        }

        long checkTime = extractNumber(time);
        long parsedTime = parseTimeString(time);

        if (checkTime <= 0 && !time.equals("Fv")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Введён неправильный формат времени.</red>"));
            return;
        }
        if (parsedTime == -1) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Введён неправильный формат времени.</red>"));
            return;
        }

        if ((target != null && target.getName().equals("Nalart11_")) || offlinePlayer.getName().equals("Nalart11_")) {
            if (sender instanceof Player) {
                if (!sender.getName().equals("Nalart11_")) {
                    banPlayer((Player) sender, parsedTime, reason, sender.getName());
                } else {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Ты не можешь забанить сам себя :3</green>"));
                }
            } else {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Ты не можешь забанить наларта :3</green>"));
            }
        } else if (target != null) {
            if (sender instanceof Player) {
                banPlayer(target, parsedTime, reason, sender.getName());
            }
            else {
                banPlayer(target, parsedTime, reason, "console");
            }
        } else {
            if (sender instanceof Player) {
                banOfflinePlayer(offlinePlayer, parsedTime, reason, sender.getName());
            }
            else {
                banOfflinePlayer(offlinePlayer, parsedTime, reason, "console");
            }
        }
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

        BanExpirationTask.instance.addPlayerToBanCheck(player.getName());
        PunishmentStorage.instance.addBan(player.getName(), admin, reason, String.valueOf(banEndTime));

        Bukkit.getScheduler().runTask(MakeABuilders.instance, () -> {
            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason, null, admin);

            player.kickPlayer(banMessage);
        });

        Integer BanNumber = PunishmentStorage.instance.getBanNumber(player.getName());

        String playerName = ChatUtils.getFormattedPlayerString(player.getName(), true);

        String adminName;
        Player adminPlayer;

        if (!admin.equals("console")) {
            adminName = ChatUtils.getFormattedPlayerString(admin, true);

            adminPlayer = Bukkit.getPlayer(admin);
        } else {
            adminName = "<gradient:#FF3D4D:#FCBDBD>console.mkbuilders.ru</gradient>";
            adminPlayer = Bukkit.getPlayer("Nalart11_");
        }

        String chatMessage = "<red>[Бан #" + BanNumber + "]</red> Игрок " + playerName + " был забанен " + adminName + " <red>" + formattedEndTime + "</red> по причине: <red>" + reason + "</red>";
        String finalChatMessage = TagFormatter.format(chatMessage);

        if (adminPlayer != null) {
            adminPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<green>✔ Игрок " + playerName + " был забанен.</green>"));
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(MiniMessage.miniMessage().deserialize(finalChatMessage));
        }
    }

    private void banOfflinePlayer(OfflinePlayer player, long duration, String reason, String admin) {
        long banEndTime = (duration == Long.MAX_VALUE) ? Long.MAX_VALUE : System.currentTimeMillis() + duration;

        BanExpirationTask.instance.addPlayerToBanCheck(player.getName());
        PunishmentStorage.instance.addBan(player.getName(), admin, reason, String.valueOf(banEndTime));

        Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason, null, admin);

        String formattedEndTime = (banEndTime == Long.MAX_VALUE) ? "навсегда" : formatBanTime(banEndTime);

        Integer BanNumber = PunishmentStorage.instance.getBanNumber(player.getName());

        String playerName = ChatUtils.getFormattedPlayerString(player.getName(), true);

        String adminName;
        Player adminPlayer;

        if (!admin.equals("console")) {
            adminName = ChatUtils.getFormattedPlayerString(admin, true);

            adminPlayer = Bukkit.getPlayer(admin);
        } else {
            adminName = "<gradient:#FF3D4D:#FCBDBD>console.mkbuilders.ru</gradient>";
            adminPlayer = Bukkit.getPlayer("Nalart11_");
        }

        String chatMessage = "<red>[Бан #" + BanNumber + "]</red> Игрок " + playerName + " был забанен " + adminName + " <red>" + formattedEndTime + "</red> по причине: <red>" + reason + "</red>";
        String finalChatMessage = TagFormatter.format(chatMessage);

        if (adminPlayer != null) {
            adminPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<green>✔ Игрок " + playerName + " был забанен.</green>"));
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(MiniMessage.miniMessage().deserialize(finalChatMessage));
        }
    }

    private String buildBanMessage(String admin, String endTime, String reason) {
        String timeText = endTime.equals("навсегда") ? "§cнавсегда" : "§fдо §c" + endTime;

        return String.format("""
        §c[Бан #%d]
        §fВы были забанены администратором §6%s §c%s §fпо причине: §e%s
        §fЕсли вы считаете что это ошибка вы можете обратиться к администрации: §ahttps://discord.gg/Ac9CSskbTf
        """,
                PunishmentStorage.instance.getNextBanNumber(),
                admin,
                timeText,
                TagFormatter.format(reason)
        );
    }

    private String formatBanTime(long banEndTime) {
        if (banEndTime == Long.MAX_VALUE) {
            return "навсегда";
        }

        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
        return dateFormat.format(new java.util.Date(banEndTime));
    }

    public static int extractNumber(String str) {
        str = str.replaceFirst("^([-]?)\\D*", "$1").replaceAll("[^\\d-]", "");
        if (str.isEmpty() || str.equals("-")) return 0;
        return Integer.parseInt(str);
    }
}