package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.chat.ChatUtils;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.other.MuteExpirationTask;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.MakeACakeStudios.storage.PunishmentStorage;
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

public class MuteCommand implements Command {

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("mute")
                        .permission("makeabuilders.mute")
                        .required("player", OfflinePlayerParser.offlinePlayerParser())
                        .required("time", StringParser.stringParser())
                        .optional("reason", StringParser.greedyStringParser(), DefaultValue.constant("Не указано."))
                        .handler(ctx -> handle(ctx.sender(), ctx.get("player"), ctx.get("time"), ctx.get("reason")))
                        .build()
        );
    }

    private void handle(@NonNull CommandSender sender, @NotNull OfflinePlayer offlinePlayer, String time, String reason) {
        Player target = Bukkit.getPlayer(offlinePlayer.getUniqueId());

        if (!offlinePlayer.isOnline() && offlinePlayer.hasPlayedBefore() != true) {
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

        if (PunishmentStorage.instance.isMuted(offlinePlayer.getName())) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Игрок уже замьючен.</red>"));
        } else if (target != null) {
            if (sender instanceof Player) {
                mutePlayer(target, parseTimeString(time), reason, sender.getName());
            }
            else {
                mutePlayer(target, parseTimeString(time), reason, "console");
            }
        } else {
            if (sender instanceof Player) {
                muteOfflinePlayer(offlinePlayer, parseTimeString(time), reason, sender.getName());
            }
            else {
                muteOfflinePlayer(offlinePlayer, parseTimeString(time), reason, "<gradient:#FF3D4D:#FCBDBD>console.mkbuilders.ru</gradient>");
            }
        }
    }

    public static int extractNumber(String str) {
        str = str.replaceFirst("^([-]?)\\D*", "$1").replaceAll("[^\\d-]", "");
        if (str.isEmpty() || str.equals("-")) return 0;
        return Integer.parseInt(str);
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

    private void mutePlayer(Player player, long duration, String reason, String admin) {
        long muteEndTime = (duration == Long.MAX_VALUE) ? Long.MAX_VALUE : System.currentTimeMillis() + duration;
        String endTime = (muteEndTime == Long.MAX_VALUE) ? "Fv" : String.valueOf(muteEndTime);

        MuteExpirationTask.instance.addPlayerToMuteCheck(player.getName());
        PunishmentStorage.instance.addMute(player.getName(), admin, reason, endTime);

        String formattedName = ChatUtils.getFormattedPlayerString(player.getName(), true);

        if (!admin.equals("console")) {
            Player sender = Bukkit.getPlayer(admin);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>✔ Игрок " + formattedName + " был замьючен.</green>"));
        } else {
            System.out.println("✔ Игрок " + player.getName() + " был замьючен.");
        }

        String message = duration == Long.MAX_VALUE
                ? "<red>Вы были замьючены навсегда по причине: </red><gold>" + reason + "</gold>"
                : "<red>Вы были замьючены на " + (duration / 1000) + " секунд по причине:</red><gold> " + reason + "</gold>";

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        player.sendMessage(MiniMessage.miniMessage().deserialize(TagFormatter.format(message)));
    }

    private void muteOfflinePlayer(OfflinePlayer player, long duration, String reason, String admin) {
        long muteEndTime = (duration == Long.MAX_VALUE) ? Long.MAX_VALUE : System.currentTimeMillis() + duration;
        String endTime = (muteEndTime == Long.MAX_VALUE) ? "Fv" : String.valueOf(muteEndTime);

        MuteExpirationTask.instance.addPlayerToMuteCheck(player.getName());
        PunishmentStorage.instance.addMute(player.getName(), admin, reason, endTime);

        String formattedName = ChatUtils.getFormattedPlayerString(player.getName(), true);

        if (!admin.equals("console")) {
            Player sender = Bukkit.getPlayer(admin);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>✔ Игрок " + formattedName + " был замьючен.</green>"));
        } else {
            System.out.println("✔ Игрок " + player.getName() + " был замьючен.");
        }
    }
}
