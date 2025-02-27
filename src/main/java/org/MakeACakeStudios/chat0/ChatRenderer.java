package org.MakeACakeStudios.chat0;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.MakeACakeStudios.utils.Formatter.flatten;

public interface ChatRenderer {
    @CanIgnoreReturnValue
    @Nullable Component render(@NotNull ChatMessage message, @NotNull Player viewer);

    /**
     * Стандартный рендерер чата. Обычное сообщение от игрока.
     */
    class DefaultChatRenderer implements ChatRenderer {
        /**
         * Обрабатывает сообщение так, как его должен видеть данный viewer
         * @param message сообщение
         * @param viewer игрок, от лица которого просматривается сообщение
         * @return отрендеренное сообщение
         */
        @Override
        public @NotNull Component render(@NotNull ChatMessage message, @NotNull Player viewer) {
            return ChatHandler.formatMessage(message.sender(), message.content());
        }
    }

    /**
     * Рендерер админ чата.
     */
    class AdminChatRenderer implements ChatRenderer {
        /**
         * Обрабатывает сообщение так, как его должен видеть данный viewer
         * @param message сообщение
         * @param viewer игрок, от лица которого просматривается сообщение
         * @return отрендеренное сообщение
         */
        @Override
        public @Nullable Component render(@NotNull ChatMessage message, @NotNull Player viewer) {
            if (AdminChat.isAdmin(viewer)) return AdminChat.formatMessage(message.sender(), flatten(message.content()));
            else return null;
        }
    }
}
