package org.MakeACakeStudios.chat0;

import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface ChatMessage {
    @NotNull OfflinePlayer sender();
    @NotNull Component content();

    void render(@NotNull Player viewer);
}
