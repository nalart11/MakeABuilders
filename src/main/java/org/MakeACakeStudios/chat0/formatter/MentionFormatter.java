package org.MakeACakeStudios.chat0.formatter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

public class MentionFormatter {

    private static final @NotNull Pattern mentionPattern = Pattern.compile("(?<=\\s|^)@(\\w{1,32})\\b");
    private static final @NotNull TextReplacementConfig.Builder config = TextReplacementConfig.builder().match(mentionPattern);

    public static @NotNull Component matchAndReplace(@NotNull Player sender, @NotNull Component message) {
        var string = PlainTextComponentSerializer.plainText().serialize(message);
        var matcher = mentionPattern.matcher(string);
        var replacements = new ArrayList<TextReplacementConfig>();
        while (matcher.find()) {
            var username = matcher.group(1);
            Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
            Player mentioned = players.stream()
                    .filter(player -> player.getName().equalsIgnoreCase(username))
                    .findFirst()
                    .orElse(null);
            if (mentioned != null) {
                replacements.add(TextReplacementConfig.builder().matchLiteral("@" + mentioned.getName()).replacement())
            }
        }

    }

    /**
     * Создаёт "упоминание". Упомянутому игроку воспроизводится звук.
     * @param player упомянутый
     * @return кликабельный Component
     */
    public static @NotNull Component createMention(@NotNull Player player) {
        var component = Component.text();
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
    }
}
