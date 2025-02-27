package org.MakeACakeStudios.chat0;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.ChatUtils;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.commands.VanishCommand;
import org.MakeACakeStudios.player.NicknameBuilder;
import org.MakeACakeStudios.storage.PunishmentStorage;
import org.MakeACakeStudios.utils.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

import static org.MakeACakeStudios.utils.Formatter.*;

public class ChatHandler implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();
        player.sendMessage(gradient("FF3D4D","FCBDBD","С возвращением!"));

        if (VanishCommand.isVanished(player)) {
            e.joinMessage(null);
            return;
        }

        var message = this.getRandomMessage(
                MakeABuilders.instance.config.getStringList("Messages.Join"),
                NicknameBuilder.displayName(player, true, true)
        );

        e.joinMessage(message);

        this.listMails(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        var player = e.getPlayer();

        if (VanishCommand.isVanished(player)) {
            e.quitMessage(null);
            return;
        }

        var message = this.getRandomMessage(
                MakeABuilders.instance.config.getStringList("Messages.Join"),
                NicknameBuilder.displayName(player, true, true)
        );

        e.quitMessage(message);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(@NotNull AsyncChatEvent e) {
        e.setCancelled(true);

        var player = e.getPlayer();
        boolean muted = PunishmentStorage.instance.isMuted(player.getName());
        var displayName = NicknameBuilder.displayName(player, true, true);

        if (muted) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
            player.sendMessage(red("Вы замьючены и не можете отправлять сообщения."));
            return;
        }

        var plainMessage = Formatter.flatten(e.message());
        var message = this.formatMessage(player, e.message());

        if (AdminChat.isUsing(player)) {
            AdminChat.sendMessage(player, plainMessage);
            return;
        }

        var finalMessage = single(displayName, text(" > "), message);
        Bukkit.getOnlinePlayers().forEach(players -> players.sendMessage(finalMessage));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(@NotNull PlayerDeathEvent e) {
        var player = e.getPlayer();
        var killer = player.getKiller();
        var displayName = NicknameBuilder.displayName(player, true, true);
        Component originalDeathMessage = e.deathMessage();

        if (originalDeathMessage != null) {
            Component formattedDeathMessage = originalDeathMessage
                    .replaceText(builder -> builder
                            .matchLiteral(player.getName())
                            .replacement(displayName)
                    );

            if (killer != null) {
                formattedDeathMessage = formattedDeathMessage.replaceText(builder -> builder
                        .matchLiteral(killer.getName())
                        .replacement(NicknameBuilder.displayName(killer, true, true))
                );
            }

            e.deathMessage(formattedDeathMessage);
        }
    }

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent e) {
        var player = e.getPlayer();
        Component originalMessage = e.message();

        if (originalMessage != null) {
            Component customAdvancementMessage = originalMessage.replaceText(builder ->
                    builder.matchLiteral(player.getName()).replacement(NicknameBuilder.displayName(player, true, true))
            );
            e.message(customAdvancementMessage);
        }
    }

    private Component getRandomMessage(List<String> list, Component player) {
        if (list == null || list.isEmpty()) return null;
        String message = list.get(new Random().nextInt(list.size()));
        return MiniMessage.miniMessage().deserialize(message, TagResolver.resolver("player", Tag.inserting(player)));
    }

    private void listMails(@NotNull Player player) {
        List<String[]> playerMessages = MakeABuilders.instance.getMailStorage().getMessages(player.getName());
        if (!playerMessages.isEmpty()) {
            if (playerMessages.size() % 10 == 1) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<click:run_command:/mailcheck><hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы открыть непрочитанное сообщение.'><green>У вас есть <yellow>" + playerMessages.size() + "</yellow> непрочитанное сообщение.</green></hover></click>"));
            } else if (playerMessages.size() % 10 == 2 || playerMessages.size() % 10 == 3 || playerMessages.size() % 10 == 4) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<click:run_command:/mailcheck><hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы открыть непрочитанные сообщения.'><green>У вас есть <yellow>" + playerMessages.size() + "</yellow> непрочитанных сообщения.</green></hover></click>"));
            } else {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<click:run_command:/mailcheck><hover:show_text:'Нажмите <green>ЛКМ</green>, чтобы открыть непрочитанные сообщения.'><green>У вас есть <yellow>" + playerMessages.size() + "</yellow> непрочитанных сообщений.</green></hover></click>"));
            }
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
        }
    }

    public static @NotNull Component formatMessage(@NotNull OfflinePlayer player, @NotNull Component c) {
        var string = Formatter.flatten(c);
        string = TagFormatter.format(string);
        string = ChatUtils.replaceLocationTag(player, string);
        string = ChatUtils.replaceMentions(player, string);
        return MiniMessage.miniMessage().deserialize(string);
    }
}
