package org.MakeACakeStudios.chat0;

import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Сообщение чата с обработанными тегами.
 */
public class TaggedMessage implements ChatMessage {

    private final @NotNull OfflinePlayer sender;
    private final @NotNull Component content;

    private final @NotNull ChatRenderer renderer;

    public TaggedMessage(@NotNull Player player, @NotNull Component content) {
        this.sender = player;
        this.content = content;
        this.renderer = ChatRenderer.defaultChat;
    }

    @Override
    public @NotNull OfflinePlayer sender() {
        return this.sender;
    }

    /**
     * @return контент сообщения.
     */
    @Override
    public @NotNull Component content() {
        return this.content;
    }

    /**
     * @param viewer
     */
    @Override
    public void render(@NotNull Player viewer) {

    }
}
