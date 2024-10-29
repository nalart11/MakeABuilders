package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VersionCommand implements CommandExecutor {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage(miniMessage.deserialize("<gray>----------------</gray>\n\n" +
                    "<gradient:#FF3D4D:#FCBDBD>MakeABuilders</gradient> <yellow>plugin</yellow>\n\n" +
                    "<yellow>Version: </yellow> <green>beta 0.3</green>\n" +
                    "<yellow>Code name:</yellow> <green>Matcha</green>\n\n\n" +
                    "<gray>© 2024 MakeACake Studios Ltd.</gray>\n" +
                    "<gray>----------------</gray>"
                    )
            );
            return true; // Успех
        } else {
            sender.sendMessage("Эта команда доступна только игрокам.");
            return true; // Команда была обработана, но не выполнена
        }
    }
}
