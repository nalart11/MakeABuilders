package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RenameCommand implements Command {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("rename")
                        .senderType(Player.class)
                        .literal("name")
                        .required("newName", StringParser.greedyStringParser())
                        .handler(ctx -> renameItem(ctx.sender(), ctx.get("newName")))
        );

        manager.command(
                manager.commandBuilder("rename")
                        .senderType(Player.class)
                        .literal("lore")
                        .required("line", IntegerParser.integerParser())
                        .required("text", StringParser.greedyStringParser())
                        .handler(ctx -> changeLore(ctx.sender(), ctx.get("line"), ctx.get("text")))
        );
    }

    private void renameItem(@NotNull Player player, String newName) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<red>Вы должны держать предмет в руке, чтобы его переименовать.</red>"));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        Component parsedName = miniMessage.deserialize(newName);
        meta.displayName(parsedName);
        item.setItemMeta(meta);

        player.sendMessage(miniMessage.deserialize("<green>Название предмета изменено на: </green>").append(item.displayName()));
    }

    private void changeLore(@NotNull Player player, int lineIndex, String loreText) {
        if (lineIndex < 1 || lineIndex > 10) {
            player.sendMessage(miniMessage.deserialize("<red>Номер строки должен быть от 1 до 10.</red>"));
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<red>Вы должны держать предмет в руке, чтобы изменить его лор.</red>"));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        List<Component> lore = meta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        while (lore.size() < lineIndex) {
            lore.add(Component.text(""));
        }

        Component parsedLore = miniMessage.deserialize(loreText);
        lore.set(lineIndex - 1, parsedLore);
        meta.lore(lore);
        item.setItemMeta(meta);

        player.sendMessage(miniMessage.deserialize("<green>Лор предмета изменён:</green>").append(parsedLore));
    }
}
