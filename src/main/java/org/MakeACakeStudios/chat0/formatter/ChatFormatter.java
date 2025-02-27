package org.MakeACakeStudios.chat0.formatter;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatFormatter {

    public static @NotNull Component format(@NotNull Player sender, @NotNull Component component) {
        var original = component;
        for (ChatReplacements config : ChatReplacements.entries) {
            original = config.replace(sender, original);
        }
        return original;
    }
}
