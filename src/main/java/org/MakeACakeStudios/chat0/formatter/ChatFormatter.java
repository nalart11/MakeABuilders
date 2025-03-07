package org.MakeACakeStudios.chat0.formatter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.player.NicknameBuilder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static org.MakeACakeStudios.utils.Formatter.*;

public class ChatFormatter {

    public static @NotNull Component format(@NotNull Player sender, @NotNull String message, @NotNull Player viewer) {
        var original = MentionFormatter.matchAndReplace(sender, pre(message));
        for (ChatReplacements config : ChatReplacements.entries) {
            original = config.replace(sender, original);
        }
        return formatMessage(sender, original, viewer);
    }

    public static @NotNull Component pre(@NotNull String message) {
        var temp = MiniMessageMarkdownTagsTranslator.markdown(message);
        return MiniMessage.miniMessage().deserialize(temp);
    }

    public static @NotNull Component formatMessage(@NotNull Player sender, @NotNull Component text, @NotNull Player viewer) {
        var displayName = NicknameBuilder.displayName(sender, true, true);
        return single(displayName, Component.space(), gray(">"), Component.space(), text);
    }
}
