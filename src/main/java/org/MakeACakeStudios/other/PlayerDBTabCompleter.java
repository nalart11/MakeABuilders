package org.MakeACakeStudios.other;

import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerDBTabCompleter implements TabCompleter {

    private final PlayerDataStorage playerDataStorage;

    public PlayerDBTabCompleter(PlayerDataStorage playerDataStorage) {
        this.playerDataStorage = playerDataStorage;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partialName = args[0].toLowerCase();

            List<String> playerNames = playerDataStorage.getAllPlayerNames();

            return playerNames.stream()
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
