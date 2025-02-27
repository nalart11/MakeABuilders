package org.MakeACakeStudios.chat0.tag;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface ViewerAwareTagRenderer {
    @NotNull Component render(@NotNull Player viewer, @NotNull Player sender);
}
