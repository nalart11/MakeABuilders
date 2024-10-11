package org.MakeACakeStudios.commands;

import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

public class BackCommand implements CommandExecutor {
    private final MakeABuilders plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BackCommand(MakeABuilders plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            List<Location> history = plugin.getLocationHistory(player);

            if (history.isEmpty()) {
                player.sendMessage(miniMessage.deserialize("<red>Нет сохранённых мест для возврата.</red>"));
                return true;
            }

            Location targetLocation;
            if (args.length > 0 && args[0].equalsIgnoreCase("first")) {
                targetLocation = plugin.getFirstLocationInHistory(player);
            } else {
                targetLocation = plugin.getLastLocationInHistory(player);
                history.remove(history.size() - 1);
            }

            if (targetLocation != null) {
                player.teleport(targetLocation);
                player.sendMessage(miniMessage.deserialize("<green>Вы вернулись на своё " + (args.length > 0 && args[0].equalsIgnoreCase("first") ? "самое первое" : "предыдущее") + " место.</green>"));
            } else {
                player.sendMessage(miniMessage.deserialize("<red>Нет сохранённого местоположения для возврата.</red>"));
            }
            return true;
        }
        return false;
    }
}