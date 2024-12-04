package org.MakeACakeStudios.other;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.MakeACakeStudios.storage.PunishmentStorage;

import java.util.ArrayList;
import java.util.List;

public class PardonTabCompleter implements TabCompleter {

    private final PunishmentStorage punishmentStorage;

    public PardonTabCompleter(PunishmentStorage punishmentStorage) {
        this.punishmentStorage = punishmentStorage;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return getBannedPlayers();
        }
        return new ArrayList<>();
    }

    private List<String> getBannedPlayers() {
        return punishmentStorage.getBannedPlayerNames();
    }
}
