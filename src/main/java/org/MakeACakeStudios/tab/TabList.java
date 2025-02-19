package org.MakeACakeStudios.tab;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.commands.VanishCommand; // Импортируем команду vanish
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TabList {

    private final MakeABuilders plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public TabList(MakeABuilders plugin) {
        this.plugin = plugin;
        startTabUpdater();
    }

    private void startTabUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateTab(player);
                }
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    private void updateTab(Player player) {
        String serverName = "<b><gradient:#FF3D4D:#FCBDBD>MakeABuilders</gradient></b>";
        String serverTagline = "<i>Делаем здесь что-то every day...</i>";

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String badge = PlayerDataStorage.instance.getHighestBadge(onlinePlayer.getName());
            String prefix = plugin.getPlayerPrefix(onlinePlayer);
            String suffix = plugin.getPlayerSuffix(onlinePlayer);

            Component formattedName;
            if (badge.equals("")) {
                formattedName = miniMessage.deserialize(prefix + onlinePlayer.getName() + suffix);
            } else {
                formattedName = miniMessage.deserialize(badge + " " + prefix + onlinePlayer.getName() + suffix);
            }

            String legacyName = LegacyComponentSerializer.legacySection().serialize(formattedName);

            onlinePlayer.setPlayerListName(legacyName);
        }

        int ping = player.getPing();
        double tpsValue = getServerTPS();

        String pingColor;
        if (ping > 250) {
            pingColor = "<gradient:#FF0000:#FF7E7E>";
        } else if (ping > 150) {
            pingColor = "<gradient:#DFFF00:#F3FF7E>";
        } else {
            pingColor = "<gradient:#00FF1A:#7EFF91>";
        }

        String tpsColor;
        if (tpsValue < 15) {
            tpsColor = "<gradient:#FF0000:#FF7E7E>";
        } else if (tpsValue < 18.5) {
            tpsColor = "<gradient:#DFFF00:#F3FF7E>";
        } else {
            tpsColor = "<gradient:#00FF1A:#7EFF91>";
        }

        // 📌 Подсчёт игроков онлайн БЕЗ ваниша
        int visiblePlayers = (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> !VanishCommand.isVanished(p))
                .count();

        String additionalTagline;
        if (visiblePlayers % 10 == 1 && visiblePlayers % 100 != 11) {
            additionalTagline = "<color:#00FF1A>" + visiblePlayers + "</color> игрок онлайн";
        } else if (visiblePlayers % 10 >= 2 && visiblePlayers % 10 <= 4 &&
                (visiblePlayers % 100 < 10 || visiblePlayers % 100 >= 20)) {
            additionalTagline = "<color:#00FF1A>" + visiblePlayers + "</color> игрока онлайн";
        } else {
            additionalTagline = "<color:#00FF1A>" + visiblePlayers + "</color> игроков онлайн";
        }

        Component header = miniMessage.deserialize("\n" + serverName + "\n" +
                "<gray>   " + serverTagline + "   </gray>\n");
        Component footer = miniMessage.deserialize("\n" +
                "Пинг<yellow>:</yellow> " + pingColor + ping + " мс<reset> | " +
                "TPS<yellow>:</yellow> " + tpsColor + String.format("%.1f", tpsValue) + "<reset>" + "\n" + additionalTagline + "\n");

        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    private double getServerTPS() {
        double[] tps = Bukkit.getServer().getTPS();
        return Math.min(tps[0], 20.0);
    }
}
