package org.MakeACakeStudios.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.space;
import static org.MakeACakeStudios.utils.Formatter.*;
import static org.MakeACakeStudios.utils.Formatter.text;

public class NicknameBuilder {
    public static @NotNull Component displayName(@NotNull OfflinePlayer player, boolean withBadge, boolean withHover) {
        var base = createDisplayName(player, withBadge);
        if (withHover) { base = base.hoverEvent(hoverLines(player)); }
        return base;
    }

    public static @NotNull Component createDisplayName(@NotNull OfflinePlayer player, boolean withBadge) {
        final MiniMessage miniMessage = MiniMessage.miniMessage();
        var playerName = player.getName();
        var prefix = PlayerDataStorage.instance.getPlayerPrefixByName(playerName);
        var suffix = PlayerDataStorage.instance.getPlayerSuffixByName(playerName);
        var component = miniMessage.deserialize(prefix + playerName + suffix);
        if (withBadge) {
            var badge = miniMessage.deserialize(PlayerDataStorage.instance.getHighestBadge(playerName));
            return single(badge, space(), component);
        }
        return component;
    }

    public static @NotNull HoverEvent<Component> hoverLines(@NotNull OfflinePlayer player) {
        var component = createDisplayName(player, false);
        return HoverEvent.showText(
                single(
                        text("Нажмите "),
                        green("ЛКМ"),
                        text(" чтобы открыть профиль игрока "),
                        component,
                        text(".")
                )
        );
    }

    public static @NotNull HoverEvent<Component> replyHoverLines(@NotNull OfflinePlayer player) {
        return HoverEvent.showText(suggestCommand(
                single(
                        text("Нажмите "),
                        green("ЛКМ"),
                        text(" чтобы отправить сообщение игроку "),
                        NicknameBuilder.displayName(player, false, false)
                ), "/msg " + player.getName())
        );
    }
}
