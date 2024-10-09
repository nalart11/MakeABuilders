package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RenameCommand implements CommandExecutor, TabCompleter {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(miniMessage.deserialize("Эту команду может использовать только игрок."));
            return true;
        }

        Player player = (Player) sender;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (player.getInventory().getItem(player.getActiveItemHand()) == null || player.getInventory().getItem(player.getActiveItemHand()).isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<red>Вы должны держать предмет в руке, чтобы его переименовать.</red>"));
            return true;
        }

        ItemMeta meta = item.getItemMeta();

        if (args.length > 1 && args[0].equalsIgnoreCase("name")) {
            StringBuilder newName = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                newName.append(args[i]).append(" ");
            }

            Component parsedName = miniMessage.deserialize(newName.toString().trim());
            meta.displayName(parsedName);
            item.setItemMeta(meta);

            player.sendMessage(miniMessage.deserialize("<green>Название предмета изменено на: </green>").append(item.displayName()));

            return true;
        }

        if (args.length > 2 && args[0].equalsIgnoreCase("lore")) {
            int lineIndex;

            try {
                lineIndex = Integer.parseInt(args[1]) - 1;
            } catch (NumberFormatException e) {
                player.sendMessage(miniMessage.deserialize("<red>Номер строки должен быть числом.</red>"));
                return true;
            }

            if (lineIndex < 0 || lineIndex >= 11) {
                player.sendMessage(miniMessage.deserialize("<red>Номер строки должен быть от 1 до 11.</red>"));
                return true;
            }

            StringBuilder loreText = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                loreText.append(args[i]).append(" ");
            }

            List<Component> lore = meta.lore();
            if (lore == null) {
                lore = new ArrayList<>();
            }

            while (lore.size() <= lineIndex) {
                lore.add(Component.text(""));
            }

            Component parsedLore = miniMessage.deserialize(loreText.toString().trim());
            lore.set(lineIndex, parsedLore);
            meta.lore(lore);
            item.setItemMeta(meta);

            player.sendMessage(miniMessage.deserialize("<green>Лор предмета изменён на: </green>").append(item.displayName()));

            return true;
        }

        player.sendMessage(miniMessage.deserialize("<red>Использование: /rename <name/lore> <аргументы></red>"));
        return true;
    }

    // Реализация автодополнения
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("name", "lore");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("lore")) {
            List<String> loreLines = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                loreLines.add(String.valueOf(i));
            }
            return loreLines;
        }

        return Collections.emptyList();
    }
}
