package org.MakeACakeStudios.chat0.formatter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.MakeACakeStudios.player.NicknameBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static org.MakeACakeStudios.utils.Formatter.reset;

public class MentionFormatter {
    public static @NotNull Component matchAndReplace(@NotNull Player sender, @NotNull Component message) {
        var replacements = new ArrayList<TextReplacementConfig>();

        Bukkit.getServer().getOnlinePlayers().stream()
                .filter(sender::canSee)
                .forEach(it ->
                    replacements.add(TextReplacementConfig.builder()
                            .match("@" + it.getName())
                            .replacement(component -> {
                                var text = reset(Component.text("@" + it.getName()));
                                text = text.color(TextColor.color(0xEFF1D4));
                                if (it.getName().equals(sender.getName())) {
                                    text = text.color(NamedTextColor.YELLOW);
                                }
                                var hoverEventLines = NicknameBuilder.hoverLines(it);
                                text = text.hoverEvent(hoverEventLines);
                                it.playSound(it.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
                                return text;
                            })
                            .build())
                );
        for (TextReplacementConfig config : replacements) {
            message = message.replaceText(config);
        }
        return message;
    }
}
