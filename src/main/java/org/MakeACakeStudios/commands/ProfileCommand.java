package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.storage.ItemStorage;
import org.MakeACakeStudios.storage.PunishmentStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.HashSet;

public class ProfileCommand implements Command, Listener {

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("profile")
                        .senderType(Player.class)
                        .optional("player", OfflinePlayerParser.offlinePlayerParser())
                        .handler(ctx -> {
                            OfflinePlayer targetPlayer = ctx.getOrDefault("player", (OfflinePlayer) ctx.sender());
                            handleMenu((Player) ctx.sender(), targetPlayer);
                        })
        );
    }

    private void handleMenu(@NotNull Player player, @NotNull OfflinePlayer offlinePlayer) {
        if (!offlinePlayer.isOnline() && offlinePlayer.hasPlayedBefore() == false) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Игрок не существует или не заходил на сервер!</red>"));
        } else {
            Bukkit.getScheduler().runTask(MakeABuilders.instance, () -> {
                Inventory inventory = Bukkit.createInventory(null, 27, "Профиль игрока " + offlinePlayer.getName());

                Set<Integer> excludeSlots = new HashSet<>(Set.of(16, 0, 9, 18));

                inventory.setItem(16, ItemStorage.getPlayerHead(offlinePlayer));
                inventory.setItem(0, ItemStorage.getPlaytimeArmor(offlinePlayer));
                inventory.setItem(9, ItemStorage.getDonateItem(offlinePlayer));
                inventory.setItem(18, ItemStorage.getMuteColor(offlinePlayer));
                if (PunishmentStorage.instance.isBanned(offlinePlayer.getName())) {
                    inventory.setItem(23, ItemStorage.getBanBarrier(offlinePlayer));
                    excludeSlots.add(23);
                }
                for (int i = 0; i < 27; i++) {
                    if (!excludeSlots.contains(i)) {
                        inventory.setItem(i, ItemStorage.getPlaceHolderItem());
                    }
                }

                player.openInventory(inventory);
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("Профиль игрока")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().startsWith("Профиль игрока")) {
            event.setCancelled(true);
        }
    }
}