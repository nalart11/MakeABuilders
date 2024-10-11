package org.MakeACakeStudios.tab;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.MakeACakeStudios.MakeABuilders;
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
        String serverTagline = "<i>Делаем здесь что то every day...</i>";

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String prefix = plugin.getPlayerPrefix(onlinePlayer);
            String suffix = plugin.getPlayerSuffix(onlinePlayer);

            Component formattedName = miniMessage.deserialize(prefix + onlinePlayer.getName() + suffix);

            String legacyName = LegacyComponentSerializer.legacySection().serialize(formattedName);

            onlinePlayer.setPlayerListName(legacyName);
        }

        int ping = player.getPing();
        double tpsValue = getServerTPS();
//        double msptValue = getMSPT();

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

//        String msptColor;
//        if (msptValue > 45) {
//            msptColor = "<gradient:#FF0000:#FF7E7E>";
//        } else if (msptValue > 35) {
//            msptColor = "<gradient:#DFFF00:#F3FF7E>";
//        } else {
//            msptColor = "<gradient:#00FF1A:#7EFF91>";
//        }

        String additionalTagline = "<i>/goto существует...</i>";

        Component header = miniMessage.deserialize("\n" + serverName + "\n" +
                "<gray>   " + serverTagline + "   </gray>\n");
        Component footer = miniMessage.deserialize("\n" +
                "<gray>Пинг</gray><yellow>:</yellow> " + pingColor + ping + "<reset> | " +
                 "<gray>TPS</gray><yellow>:</yellow> " + tpsColor + String.format("%.1f", tpsValue) + "<reset>"+ /*" | " +
                 "<gray>Mspt</gray><yellow>:</yellow> " + msptColor + String.format("%.2f", msptValue) + "<reset>" + */"\n" +
                "<gray>" + additionalTagline + "</gray>" + "\n");

        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    private double getServerTPS() {
        double[] tps = Bukkit.getServer().getTPS();
        if (tps[0] > 20.0) {
            return 20.0;
        } else {
            return tps[0];
        }
    }

//    private double getMSPT() {
//        long[] tickTimes = Bukkit.getServer().getTickTimes();
//
//        double totalTickTime = 0;
//        int ticksToAverage = 5;
//
//        for (int i = 0; i < ticksToAverage; i++) {
//            totalTickTime += tickTimes[i];
//        }
//
//        double averageTickTime = totalTickTime / ticksToAverage;
//
//        return averageTickTime / 1_000_000.0;
//    }
}
