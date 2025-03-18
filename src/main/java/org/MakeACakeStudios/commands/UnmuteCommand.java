package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.chat.ChatUtils;
import org.MakeACakeStudios.other.MuteExpirationTask;
import org.MakeACakeStudios.parsers.AsyncOfflinePlayerParser;
import org.MakeACakeStudios.storage.PunishmentStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

public class UnmuteCommand implements Command {

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("unmute")
                        .permission("makeabuilders.unmute")
                        .required("player", AsyncOfflinePlayerParser.asyncOfflinePlayerParser())
                        .handler(ctx -> handle(ctx.sender(), ctx.get("player")))
                        .build()
        );
    }

    private void handle(@NonNull CommandSender sender, @NotNull OfflinePlayer target) {

        if (target.hasPlayedBefore() != true) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Игрок не найден!</red>"));
            return;
        }

        String playerName = target.getName();

        if (!PunishmentStorage.instance.isMuted(target.getName())) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>✖ Игрок " + playerName + " не замьючен.</red>"));
            return;
        }

        String formattedName = ChatUtils.getFormattedPlayerString(playerName, true);

        PunishmentStorage.instance.unmutePlayer(playerName);
        MuteExpirationTask.instance.removePlayerFromMuteCheck(target.getName());
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>✔ Игрок " + formattedName + " был размъючен.</green>"));

        Player player = Bukkit.getPlayer(target.getUniqueId());
        if (player != null) {
            player.playSound(target.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1.0f, 1.0f);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Вы были размьючены.</green>"));
        }
    }
}
