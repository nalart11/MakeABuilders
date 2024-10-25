package org.MakeACakeStudios;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.commands.*;
import org.MakeACakeStudios.motd.DynamicMotd;
import org.MakeACakeStudios.other.EmptyTabCompleter;
import org.MakeACakeStudios.tab.TabList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.MakeACakeStudios.chat.ChatHandler;
import org.MakeACakeStudios.storage.*;
import org.MakeACakeStudios.other.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class MakeABuilders extends JavaPlugin implements @NotNull Listener {

    private final HashMap<Player, Player> lastMessaged = new HashMap<>();
    private final HashMap<Player, Sound> playerSounds = new HashMap<>();
    private HashMap<Player, List<Location>> locationHistory = new HashMap<>();

    private FileConfiguration config;
    private ChatHandler chatHandler;
    private TabList tabList;
    private MailStorage mailStorage;
    private PlayerNameStorage playerNameStorage;
    private DynamicMotd dynamicMotd;
    private MuteCommand muteCommand;

    private Connection connection;  // Для подключения к базе данных

    public Connection getConnection() {
        return connection;  // Add this method
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        connectToDatabase();  // Подключение к базе данных

        playerNameStorage = new PlayerNameStorage(this);
        MiniMessage miniMessage = MiniMessage.miniMessage();
        mailStorage = new MailStorage(connection);  // Передаем подключение в MailStorage
        chatHandler = new ChatHandler(this);
        tabList = new TabList(this);
        dynamicMotd = new DynamicMotd(this);
        muteCommand = new MuteCommand(this, playerNameStorage);

        getServer().getPluginManager().registerEvents(new DynamicMotd(this), this);
        getServer().getPluginManager().registerEvents(chatHandler, this);
        getServer().getPluginManager().registerEvents(this, this);

        this.getCommand("goto").setExecutor(new TeleportCommand(this));
        this.getCommand("back").setExecutor(new BackCommand(this));
        this.getCommand("message").setExecutor(new MessageCommand(this));
        this.getCommand("reply").setExecutor(new ReplyCommand(this));
        this.getCommand("message-sound").setExecutor(new MessageSoundCommand(this));
        this.getCommand("rename").setExecutor(new RenameCommand());
        this.getCommand("shrug").setExecutor(new SmugCommand(playerNameStorage, miniMessage));
        this.getCommand("tableflip").setExecutor(new SmugCommand(playerNameStorage, miniMessage));
        this.getCommand("unflip").setExecutor(new SmugCommand(playerNameStorage, miniMessage));
        this.getCommand("announce").setExecutor(new AnnounceCommand(this));
        this.getCommand("mail").setExecutor(new MailCommand(this));
        this.getCommand("mailcheck").setExecutor(new MailCommand(this));
        this.getCommand("mailread").setExecutor(new MailCommand(this));
        this.getCommand("mute").setExecutor(new MuteCommand(this, playerNameStorage));
        this.getCommand("unmute").setExecutor(new UnmuteCommand(this));

        this.getCommand("reply").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("back").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("shrug").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("tableflip").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("unflip").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("announce").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("mute").setTabCompleter(new MuteTabCompleter());
        this.getCommand("unmute").setTabCompleter(new UnmuteTabCompleter());

        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerSound(player);
        }

        getLogger().info("MakeABuilders плагин активирован!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MakeABuilders плагин деактивирован.");
        disconnectFromDatabase();  // Отключение от базы данных
    }

    // Подключение к базе данных SQLite
    private void connectToDatabase() {
        try {
            String url = "jdbc:sqlite:" + getDataFolder().getAbsolutePath() + "/mail.db";
            connection = DriverManager.getConnection(url);
            getLogger().info("Подключение к базе данных SQLite установлено.");

            // Создание таблицы для писем, если её нет
            String createTableSQL = "CREATE TABLE IF NOT EXISTS mail_messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "recipient TEXT, " +
                    "senderPrefix TEXT, " +
                    "sender TEXT, " +
                    "senderSuffix TEXT, " +
                    "message TEXT)";
            connection.createStatement().execute(createTableSQL);
            getLogger().info("Таблица для хранения сообщений создана или уже существует.");
        } catch (SQLException e) {
            getLogger().severe("Ошибка при подключении к базе данных: " + e.getMessage());
        }
    }

    // Отключение от базы данных
    private void disconnectFromDatabase() {
        if (connection != null) {
            try {
                connection.close();
                getLogger().info("Подключение к базе данных закрыто.");
            } catch (SQLException e) {
                getLogger().severe("Ошибка при закрытии подключения к базе данных: " + e.getMessage());
            }
        }
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

    public MailStorage getMailStorage() {
        return mailStorage;
    }

    public PlayerNameStorage getPlayerNameStorage() {
        return playerNameStorage;
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
            return history.get(history.size() - 1);
        }
        return null;
    }

    public Location getFirstLocationInHistory(Player player) {
        List<Location> history = getLocationHistory(player);
        if (!history.isEmpty()) {
            return history.get(0);
        }
        return null;
    }

    public String getPlayerPrefix(Player player) {
        return playerNameStorage.getPlayerPrefix(player);
    }

    public String getPlayerSuffix(Player player) {
        return playerNameStorage.getPlayerSuffix(player);
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