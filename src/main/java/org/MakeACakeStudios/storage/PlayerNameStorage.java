package org.MakeACakeStudios.storage;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.MakeACakeStudios.MakeABuilders;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class PlayerNameStorage {

    private final MakeABuilders plugin;
    private final FileConfiguration config;
    private static final Map<String, Integer> groupWeights = new HashMap<>();

    static {
        groupWeights.put("iam", 5);
        groupWeights.put("javadper", 4);
        groupWeights.put("yosya", 4);
        groupWeights.put("admin", 3);
        groupWeights.put("developer", 2);
        groupWeights.put("moderator", 1);
        groupWeights.put("sponsor", 1);
    }

    public PlayerNameStorage(MakeABuilders plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public String getPlayerPrefix(Player player) {

        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        String highestGroup = "default";
        int highestWeight = 0;

        if (user != null) {
            for (Node node : user.getNodes()) {
                if (node instanceof InheritanceNode) {
                    InheritanceNode inheritanceNode = (InheritanceNode) node;
                    String groupName = inheritanceNode.getGroupName();
                    if (groupWeights.containsKey(groupName) && groupWeights.get(groupName) > highestWeight) {
                        highestWeight = groupWeights.get(groupName);
                        highestGroup = groupName;
                    }
                }
            }
        }

        String prefix;
        switch (highestGroup) {
            case "iam":
                prefix = "<yellow>\uD83D\uDC51</yellow> <gradient:#DEA903:#FFEDB5>";
                break;
            case "javadper":
                prefix = "<blue>\uD83D\uDEE0</blue> <gradient:#E0E0E0:#808080>";
                break;
            case "yosya":
                prefix = "<light_purple>\uD83D\uDC08</light_purple> <gradient:#DD00CC:#FFC8F6>";
                break;
            case "admin":
                prefix = "<blue>\uD83D\uDDE1</blue> <gradient:#FF2323:#FF7878>";
                break;
            case "developer":
                prefix = "<blue>\uD83D\uDEE0</blue> <gradient:#141378:#97ABFF>";
                break;
            case "moderator":
                prefix = "<red>\uD83D\uDD31</red> <gradient:#23DBFF:#C8E9FF>";
                break;
            case "sponsor":
                prefix = "<gold>$</gold> <gradient:#00A53E:#C8FFD4>";
                break;
            default:
                prefix = "";
        }

        setPlayerPrefix(player.getName(), prefix);
        return prefix;
    }

    public String getPlayerSuffix(Player player) {

        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        String highestGroup = "default";
        int highestWeight = 0;

        if (user != null) {
            for (Node node : user.getNodes()) {
                if (node instanceof InheritanceNode) {
                    InheritanceNode inheritanceNode = (InheritanceNode) node;
                    String groupName = inheritanceNode.getGroupName();
                    if (groupWeights.containsKey(groupName) && groupWeights.get(groupName) > highestWeight) {
                        highestWeight = groupWeights.get(groupName);
                        highestGroup = groupName;
                    }
                }
            }
        }

        String suffix = "";
        if (highestGroup.equals("default")) {
            suffix = "";
        } else {
            suffix = "</gradient>";
        }

        setPlayerSuffix(player.getName(), suffix);
        return suffix;
    }

    public String getPlayerPrefixByName(String playerName) {
        if (config.contains("players." + playerName + ".prefix")) {
            return config.getString("players." + playerName + ".prefix");
        }

        return "";
    }

    public String getPlayerSuffixByName(String playerName) {
        if (config.contains("players." + playerName + ".suffix")) {
            return config.getString("players." + playerName + ".suffix");
        }

        return "";
    }


    public void setPlayerPrefix(String playerName, String prefix) {
        config.set("players." + playerName + ".prefix", prefix);
        saveConfig();
    }

    public void setPlayerSuffix(String playerName, String suffix) {
        config.set("players." + playerName + ".suffix", suffix);
        saveConfig();
    }

    private void saveConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось сохранить конфигурацию!", e);
        }
    }
}
