package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.storage.DonateStorage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;

public class DonateCommand implements Command {
    private final DonateStorage storage = new DonateStorage();

    private static final Map<String, Integer> DONATE_EFFECTS = new HashMap<>() {{
        put("Zeus", 1);
        put("Star", 2);
        put("Sakura", 3);
        put("Vanila", 4);
    }};

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
                        .literal("add")
                        .literal("amount")
                        .required("player", PlayerParser.playerParser())
                        .required("amount", IntegerParser.integerParser())
                        .handler(ctx -> handleAddDonate(ctx.sender(), ctx.get("player"), ctx.get("amount")))
        );

        manager.command(
                manager.commandBuilder("donate")
                        .senderType(Player.class)
                        .literal("remove")
                        .literal("amount")
                        .required("player", PlayerParser.playerParser())
                        .required("amount", IntegerParser.integerParser())
                        .handler(ctx -> handleRemoveDonate(ctx.sender(), ctx.get("player"), ctx.get("amount")))
        );

        manager.command(
                manager.commandBuilder("donate")
                        .senderType(Player.class)
                        .literal("add")
                        .literal("effect")
                        .required("player", PlayerParser.playerParser())
                        .required("effect", StringParser.stringParser())
                        .handler(ctx -> handleAddEffect(ctx.sender(), ctx.get("player"), ctx.get("effect")))
        );

        manager.command(
                manager.commandBuilder("donate")
                        .senderType(Player.class)
                        .literal("remove")
                        .literal("effect")
                        .required("player", PlayerParser.playerParser())
                        .required("effect", StringParser.stringParser())
                        .handler(ctx -> handleRemoveEffect(ctx.sender(), ctx.get("player"), ctx.get("effect")))
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

    private static final String DONATE_PERMISSION = "MakeABuilders.donates"; // Пермишен для /donate add/remove

    private void handleAddDonate(@NotNull Player sender, @NotNull Player target, int amount) {
        if (!sender.hasPermission(DONATE_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>У вас нет прав на использование этой команды!</red>"));
            return;
        }

        storage.addDonationAmount(target.getName(), amount);
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Добавлено <yellow>" + amount + "</yellow> донатов игроку <yellow>" + target.getName() + "</yellow></green>"));
        target.sendMessage(MiniMessage.miniMessage().deserialize("<green>Вам добавлено <yellow>" + amount + "</yellow> донатов!</green>"));
    }

    private void handleRemoveDonate(@NotNull Player sender, @NotNull Player target, int amount) {
        if (!sender.hasPermission(DONATE_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>У вас нет прав на использование этой команды!</red>"));
            return;
        }

        storage.removeDonationAmount(target.getName(), amount);
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Убрано <yellow>" + amount + "</yellow> донатов у игрока <yellow>" + target.getName() + "</yellow></red>"));
        target.sendMessage(MiniMessage.miniMessage().deserialize("<red>У вас списано <yellow>" + amount + "</yellow> донатов!</red>"));
    }

    private void handleAddEffect(@NotNull Player sender, @NotNull Player target, String effect) {
        if (!sender.hasPermission(DONATE_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>У вас нет прав на использование этой команды!</red>"));
            return;
        }

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
        if (!sender.hasPermission(DONATE_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>У вас нет прав на использование этой команды!</red>"));
            return;
        }

        Integer effectId = DONATE_EFFECTS.get(effect);
        if (effectId == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Такого эффекта не существует!</red>"));
            return;
        }

        storage.removeDonation(target.getName(), effectId);
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Удалён донатный эффект <yellow>" + effect + "</yellow> у игрока <yellow>" + target.getName() + "</yellow></red>"));
        target.sendMessage(MiniMessage.miniMessage().deserialize("<red>У вас удалён донатный эффект <yellow>" + effect + "</yellow>!</red>"));
    }
}
