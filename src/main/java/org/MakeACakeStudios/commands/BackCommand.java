package org.MakeACakeStudios.commands;

import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BackCommand implements CommandExecutor {
    private final MakeABuilders plugin;

    public BackCommand(MakeABuilders plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            List<Location> history = plugin.getLocationHistory(player);

            if (history.isEmpty()) {
                player.sendMessage("§cНет сохранённых мест для возврата.");
                return true;
            }

            Location targetLocation;
            if (args.length > 0 && args[0].equalsIgnoreCase("first")) {
                targetLocation = plugin.getFirstLocationInHistory(player);
            } else {
                targetLocation = plugin.getLastLocationInHistory(player);
                history.remove(history.size() - 1); // Удаляем последнюю точку
            }

            if (targetLocation != null) {
                player.teleport(targetLocation);
                player.sendMessage("§aВы вернулись на своё " + (args.length > 0 && args[0].equalsIgnoreCase("first") ? "самое первое" : "предыдущее") + " место.");
            } else {
                player.sendMessage("§cНет сохранённого местоположения для возврата.");
            }
            return true;
        }
        return false;
    }
}