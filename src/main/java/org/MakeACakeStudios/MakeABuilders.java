package org.MakeACakeStudios;

import org.MakeACakeStudios.chat.ChatHandler;
import org.MakeACakeStudios.commands.*;
import org.MakeACakeStudios.tab.TabList;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class MakeABuilders extends JavaPlugin implements @NotNull Listener {

    private final HashMap<Player, Player> lastMessaged = new HashMap<>();
    private final HashMap<Player, Sound> playerSounds = new HashMap<>();
    private HashMap<Player, List<Location>> locationHistory = new HashMap<>();

    private FileConfiguration config;
    private ChatHandler chatHandler; // Объявите экземпляр ChatHandler
    private TabList tabList;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        chatHandler = new ChatHandler(this); // Инициализируйте экземпляр ChatHandler
        tabList = new TabList(this); // Инициализируем TabList
        getServer().getPluginManager().registerEvents(chatHandler, this); // Используйте chatHandler
        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("goto").setExecutor(new TeleportCommand(this));
        this.getCommand("back").setExecutor(new BackCommand(this));
        this.getCommand("msg").setExecutor(new MessageCommand(this));
        this.getCommand("r").setExecutor(new ReplyCommand(this));
        this.getCommand("msgs").setExecutor(new MessageSoundCommand(this));
        this.getCommand("rename").setExecutor(new RenameCommand());
        this.getCommand("enchant").setExecutor(new EnchantCommand(this));
        getLogger().info("MakeABuilders плагин активирован!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MakeABuilders плагин деактивирован.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loadPlayerSound(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        savePlayerSound(player);
    }

    public void addLocationToHistory(Player player, Location location) {
        locationHistory.computeIfAbsent(player, k -> new ArrayList<>()).add(location);
    }

    public List<Location> getLocationHistory(Player player) {
        return locationHistory.getOrDefault(player, new ArrayList<>());
    }

    public Location getLastLocationInHistory(Player player) {
        List<Location> history = getLocationHistory(player);
        if (!history.isEmpty()) {
            return history.get(history.size() - 1); // Последняя локация
        }
        return null;
    }

    public Location getFirstLocationInHistory(Player player) {
        List<Location> history = getLocationHistory(player);
        if (!history.isEmpty()) {
            return history.get(0); // Первая локация
        }
        return null;
    }

    // Используйте chatHandler для получения префикса игрока
    public String getPlayerPrefix(Player player) {
        return chatHandler.getPlayerPrefix(player); // Замените на вызов метода chatHandler
    }

    // Используйте chatHandler для получения суффикса игрока
    public String getPlayerSuffix(Player player) {
        return chatHandler.getPlayerSuffix(player); // Замените на вызов метода chatHandler
    }

    public void setPlayerSound(Player player, Sound sound) {
        playerSounds.put(player, sound);
        savePlayerSound(player);
    }

    public Sound getPlayerSound(Player player) {
        return playerSounds.getOrDefault(player, Sound.BLOCK_NOTE_BLOCK_BELL);
    }

    public void savePlayerSound(Player player) {
        String path = "players." + player.getUniqueId().toString() + ".sound";
        Sound sound = getPlayerSound(player);
        config.set(path, sound.toString());
        saveConfig();
    }

    public void loadPlayerSound(Player player) {
        String path = "players." + player.getUniqueId().toString() + ".sound";
        if (config.contains(path)) {
            String soundName = config.getString(path);
            Sound sound = Sound.valueOf(soundName);
            setPlayerSound(player, sound);
        }
    }

    public void setLastMessaged(Player sender, Player recipient) {
        lastMessaged.put(recipient, sender);
    }

    public Player getLastMessaged(Player player) {
        return lastMessaged.get(player);
    }
}
