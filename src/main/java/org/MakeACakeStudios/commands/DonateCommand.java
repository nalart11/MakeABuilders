package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.donates.EffectManager;
import org.MakeACakeStudios.storage.DonateStorage;
import org.MakeACakeStudios.storage.ItemStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;
import java.util.HashSet;
import java.util.Set;

import static org.MakeACakeStudios.storage.DonateStorage.DONATE_EFFECTS;

public class DonateCommand implements Command, @NotNull Listener {
    private final DonateStorage storage = new DonateStorage();

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("donate")
                        .senderType(Player.class)
                        .handler(ctx -> handleGetDonate(ctx.sender()))
        );

        manager.command(
                manager.commandBuilder("donate")
                        .senderType(Player.class)
                        .required("player", PlayerParser.playerParser())
                        .handler(ctx -> handleGetDonate(ctx.sender(), ctx.get("player")))
        );

        manager.command(
                manager.commandBuilder("donate")
                        .senderType(Player.class)
                        .literal("menu")
                        .handler(ctx -> handleMenu(ctx.sender()))
        );

        manager.command(
                manager.commandBuilder("donate")
                        .senderType(Player.class)
                        .permission("makeabuilders.donates")
                        .literal("add")
                        .literal("amount")
                        .required("player", PlayerParser.playerParser())
                        .required("amount", IntegerParser.integerParser())
                        .handler(ctx -> handleAddDonate(ctx.sender(), ctx.get("player"), ctx.get("amount")))
        );

        manager.command(
                manager.commandBuilder("donate")
                        .senderType(Player.class)
                        .permission("makeabuilders.donates")
                        .literal("remove")
                        .literal("amount")
                        .required("player", PlayerParser.playerParser())
                        .required("amount", IntegerParser.integerParser())
                        .handler(ctx -> handleRemoveDonate(ctx.sender(), ctx.get("player"), ctx.get("amount")))
        );

        manager.command(
                manager.commandBuilder("donate")
                        .senderType(Player.class)
                        .permission("makeabuilders.donates")
                        .literal("add")
                        .literal("effect")
                        .required("player", PlayerParser.playerParser())
                        .required("effect", StringParser.stringParser())
                        .handler(ctx -> handleAddEffect(ctx.sender(), ctx.get("player"), ctx.get("effect")))
        );

        manager.command(
                manager.commandBuilder("donate")
                        .senderType(Player.class)
                        .permission("makeabuilders.donates")
                        .literal("remove")
                        .literal("effect")
                        .required("player", PlayerParser.playerParser())
                        .required("effect", StringParser.stringParser())
                        .handler(ctx -> handleRemoveEffect(ctx.sender(), ctx.get("player"), ctx.get("effect")))
        );

        manager.command(
                manager.commandBuilder("donate")
                        .senderType(Player.class)
                        .literal("toggle")
                        .required("effect", StringParser.stringParser())
                        .handler(ctx -> {
                            Player player = ctx.sender();
                            toggleEffect(player, ctx.get("effect"));
                        })
        );

    }


    private void handleGetDonate(@NotNull Player sender) {
        int amount = storage.getTotalDonations(sender.getName());
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Ваша сумма донатов: <yellow>" + amount + "</yellow></green>"));
    }

    private void handleGetDonate(@NotNull Player sender, @NotNull Player target) {
        int amount = storage.getTotalDonations(target.getName());
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Сумма донатов игрока <yellow>" + target.getName() + "</yellow>: <yellow>" + amount + "</yellow></green>"));
    }

    private void handleAddDonate(@NotNull Player sender, @NotNull Player target, int amount) {
        storage.addDonationAmount(target.getName(), amount);
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Добавлено <yellow>" + amount + "</yellow> рублей игроку <yellow>" + target.getName() + "</yellow></green>"));
        target.sendMessage(MiniMessage.miniMessage().deserialize("<green>Вам добавлено <yellow>" + amount + "</yellow> рублей!</green>"));
    }

    private void handleRemoveDonate(@NotNull Player sender, @NotNull Player target, int amount) {
        storage.removeDonationAmount(target.getName(), amount);
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Убрано <yellow>" + amount + "</yellow> рублей у игрока <yellow>" + target.getName() + "</yellow></red>"));
        target.sendMessage(MiniMessage.miniMessage().deserialize("<red>У вас списано <yellow>" + amount + "</yellow> рублей!</red>"));
    }

    private void handleAddEffect(@NotNull Player sender, @NotNull Player target, String effect) {
        Integer effectId = DONATE_EFFECTS.get(effect);
        if (effectId == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Такого эффекта не существует!</red>"));
            return;
        }

        storage.addDonation(target.getName(), effectId);
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Добавлен донатный эффект <yellow>" + effect + "</yellow> игроку <yellow>" + target.getName() + "</yellow></green>"));
        target.sendMessage(MiniMessage.miniMessage().deserialize("<green>Вам добавлен донатный эффект <yellow>" + effect + "</yellow>!</green>"));
    }

    private void handleRemoveEffect(@NotNull Player sender, @NotNull Player target, String effect) {
        Integer effectId = DONATE_EFFECTS.get(effect);
        if (effectId == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Такого эффекта не существует!</red>"));
            return;
        }

        storage.removeDonation(target.getName(), effectId);
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Удалён донатный эффект <yellow>" + effect + "</yellow> у игрока <yellow>" + target.getName() + "</yellow></red>"));
        target.sendMessage(MiniMessage.miniMessage().deserialize("<red>У вас удалён донатный эффект <yellow>" + effect + "</yellow>!</red>"));
    }

    private void handleMenu(@NotNull Player sender) {
        int donate = DonateStorage.instance.getTotalDonations(sender.getName());

        if (donate != 0) {
            Bukkit.getScheduler().runTask(MakeABuilders.instance, () -> {
                Inventory inventory = Bukkit.createInventory(null, 54, "Меню донатов");

                Set<Integer> excludeSlots = new HashSet<>(Set.of(4, 19, 20, 21));
                for (int i = 0; i < 54; i++) {
                    if (!excludeSlots.contains(i)) {
                        inventory.setItem(i, ItemStorage.getPlaceHolderItem());
                    }
                }
                inventory.setItem(4, ItemStorage.getDonateCake(sender));
                inventory.setItem(19, ItemStorage.getSakuraEffect(sender));
                inventory.setItem(20, ItemStorage.getZeusEffect(sender));
                inventory.setItem(21, ItemStorage.getStarEffect(sender));

                sender.openInventory(inventory);
            });
        } else {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>У вас нету эффектов, чтобы их можно было отобразить в меню донатов :(</red>"));
        }
    }

    private void toggleEffect(@NotNull Player player, @NotNull String effect) {
        Integer effectId = DONATE_EFFECTS.get(effect);
        if (effectId == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Такого эффекта не существует!</red>"));
            return;
        }

        boolean isEnabled = EffectManager.getEnabledEffectsForPlayer(player.getName()).contains(effectId);

        if (isEnabled) {
            EffectManager.stopEffectForDonation(effectId, player.getName());
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Эффект " + effect + " выключен!</red>"));
        } else {
            EffectManager.startEffectForDonation(effectId, player.getName());
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Эффект " + effect + " включен!</green>"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Меню донатов")) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();

            if (event.getClick().isLeftClick() || event.getClick().isRightClick()) {
                if (slot == 19) {
                    if (DonateStorage.instance.hasDonation(player.getName(), 3)) {
                        boolean isSakuraEnabled = EffectManager.getEnabledEffectsForPlayer(player.getName()).contains(3);

                        if (isSakuraEnabled) {
                            EffectManager.stopEffectForDonation(3, player.getName());
                        } else {
                            EffectManager.startEffectForDonation(3, player.getName());
                        }
                        event.getInventory().setItem(19, ItemStorage.getSakuraEffect(player));
                        player.updateInventory();
                    }
                } else if (slot == 20) {
                    if (DonateStorage.instance.hasDonation(player.getName(), 1)) {
                        boolean isZeusEnabled = EffectManager.getEnabledEffectsForPlayer(player.getName()).contains(1);

                        if (isZeusEnabled) {
                            EffectManager.stopEffectForDonation(1, player.getName());
                        } else {
                            EffectManager.startEffectForDonation(1, player.getName());
                        }
                        event.getInventory().setItem(20, ItemStorage.getZeusEffect(player));
                        player.updateInventory();
                    }
                } else if (slot == 21) {
                    if (DonateStorage.instance.hasDonation(player.getName(), 2)) {
                        boolean isStarEnabled = EffectManager.getEnabledEffectsForPlayer(player.getName()).contains(2);

                        if (isStarEnabled) {
                            EffectManager.stopEffectForDonation(2, player.getName());
                        } else {
                            EffectManager.startEffectForDonation(2, player.getName());
                        }
                        event.getInventory().setItem(21, ItemStorage.getStarEffect(player));
                        player.updateInventory();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().startsWith("Меню донатов")) {
            event.setCancelled(true);
        }
    }
}
