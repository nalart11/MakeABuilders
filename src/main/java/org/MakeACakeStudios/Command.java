package org.MakeACakeStudios;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

public interface Command {
    default void register(LegacyPaperCommandManager<CommandSender> manager) {
        throw new IllegalArgumentException("command not implemented");
    }
}