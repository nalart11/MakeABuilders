package org.MakeACakeStudios.other;

import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PunishmentTabCompleter implements TabCompleter {

    private final List<String> timeUnits = Arrays.asList("s", "m", "h", "d", "w", "y");
    private final PlayerDataStorage playerDataStorage;

    public PunishmentTabCompleter(PlayerDataStorage playerDataStorage) {
        this.playerDataStorage = playerDataStorage;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String partialName = args[0].toLowerCase();

            List<String> playerNames = playerDataStorage.getAllPlayerNames();

            return playerNames.stream()
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            String input = args[1];

            if (input.length() == 0) {
                suggestions.add("Fv");
            } else if (input.equalsIgnoreCase("Fv")) {
                return Collections.singletonList("Fv");
            } else {
                char lastChar = input.charAt(input.length() - 1);
                if (Character.isDigit(lastChar)) {
                    for (String unit : timeUnits) {
                        suggestions.add(input + unit);
                    }
                } else if (timeUnits.contains(String.valueOf(lastChar))) {
                    return Collections.emptyList();
                }
            }

            return suggestions;
        }

        return Collections.emptyList();
    }
}
