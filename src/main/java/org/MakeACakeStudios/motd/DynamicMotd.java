package org.MakeACakeStudios.motd;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DynamicMotd implements Listener {
    private final MakeABuilders plugin;
    private final List<List<String>> motdGroups;
    private final Random random;

    public DynamicMotd(MakeABuilders plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.motdGroups = new ArrayList<>();

        for (String key : config.getConfigurationSection("motd").getKeys(false)) {
            List<String> group = config.getStringList("motd." + key);
            motdGroups.add(group);
        }

        this.random = new Random();
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        List<String> randomGroup = motdGroups.get(random.nextInt(motdGroups.size()));

        String combinedMOTD = String.join("\n", randomGroup);

        event.motd(MiniMessage.miniMessage().deserialize(combinedMOTD));
    }
}
