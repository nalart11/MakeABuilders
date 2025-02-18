package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.chat.ChatUtils;
import org.MakeACakeStudios.storage.DonateStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AutoDonateCommand implements Command {

    private static final Map<String, Integer> EFFECT_PRICES = new HashMap<>() {{
        put("Zeus", 299);
        put("Star", 149);
        put("Sakura", 199);
    }};

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("autodonate")
                        .senderType(CommandSender.class)
                        .required("nickname", StringParser.stringParser())
                        .required("effect", StringParser.stringParser())
                        .handler(ctx -> handleAutoDonate(ctx.sender(), ctx.get("nickname"), ctx.get("effect")))
        );
    }

    private void handleAutoDonate(@NotNull CommandSender sender, String nickname, String effect) {
        if (!EFFECT_PRICES.containsKey(effect)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Ошибка: эффект <yellow>" + effect + "</yellow> не найден!</red>"));
            return;
        }

        int cost = EFFECT_PRICES.get(effect);
        int effectId = DonateStorage.DONATE_EFFECTS.get(effect);

        DonateStorage.instance.addDonationAmount(nickname, cost);
        DonateStorage.instance.addDonation(nickname, effectId);

        Player target = Bukkit.getPlayerExact(nickname);
        String formattedName = ChatUtils.getFormattedPlayerString(nickname, true);
        if (target != null) {
            target.sendMessage(MiniMessage.miniMessage().deserialize("<green>Вы получили эффект <yellow>" + effect + "</yellow>!</green>"));
        } else {
            System.out.println("Игрок " + nickname + " оффлайн. Эффект " + effect + " будет применён при входе.");
        }

        String donateMessage = "<yellow>Игрок " + formattedName + " купил эффект <green>" + effect + "</green> за <aqua>" + cost + "</aqua> рублей!</yellow>";
        ChatUtils.broadcastMessage(donateMessage);
    }
}
