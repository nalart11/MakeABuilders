package org.MakeACakeStudios.commands;

import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class TeleportCommand implements CommandExecutor, TabCompleter {
    private final MakeABuilders plugin;
    private final MiniMessage miniMessage;

    public TeleportCommand(MakeABuilders plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            String worldName = args.length > 0 ? args[0] : player.getWorld().getName();
            int x = 0, y = -60, z = 0;

            if (args.length == 4) {
                try {
                    x = Integer.parseInt(args[1]);
                    y = Integer.parseInt(args[2]);
                    z = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(miniMessage.deserialize("<red>Координаты должны быть числами.</red>"));
                    return true;
                }
            } else if (args.length != 1) {
                player.sendMessage(miniMessage.deserialize("<red>Использование: /goto <world> [x] [y] [z]</red>"));
                return true;
            }

            World world = plugin.getServer().getWorld(worldName);
            if (world != null) {
                plugin.addLocationToHistory(player, player.getLocation());
                player.teleport(new Location(world, x, y, z));
                player.sendMessage(miniMessage.deserialize("<green>Вы были телепортированы в мир " + worldName + "</green>"));
            } else {
                player.sendMessage(miniMessage.deserialize("<red>Мир не найден.</red>"));
                return true;
            }

            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> worldNames = new ArrayList<>();
            for (World world : plugin.getServer().getWorlds()) {
                worldNames.add(world.getName());
            }
            return worldNames;
        }
        return null;
    }
}