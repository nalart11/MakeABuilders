package org.MakeACakeStudios;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.commands.*;
import org.MakeACakeStudios.motd.DynamicMotd;
import org.MakeACakeStudios.other.EmptyTabCompleter;
import org.MakeACakeStudios.tab.TabList;
import org.MakeACakeStudios.other.MuteExpirationTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.MakeACakeStudios.chat.ChatHandler;
import org.MakeACakeStudios.storage.*;
import org.MakeACakeStudios.other.*;
import org.MakeACakeStudios.other.PlayerBanListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ALL")
public final class MakeABuilders extends JavaPlugin implements @NotNull Listener {

    private final HashMap<Player, Player> lastMessaged = new HashMap<>();
    private final HashMap<Player, Sound> playerSounds = new HashMap<>();
    private HashMap<Player, List<Location>> locationHistory = new HashMap<>();

    private FileConfiguration config;
    private ChatHandler chatHandler;
    private TabList tabList;
    private MailStorage mailStorage;
    private PlayerDataStorage playerDataStorage;
    private PunishmentStorage punishmentStorage;
    private DynamicMotd dynamicMotd;
    private MuteCommand muteCommand;
    private MuteExpirationTask muteExpirationTask;
    private BanExpirationTask banExpirationTask;
    private PlayerBanListener playerBanListener;
    private TagFormatter tagFormatter;

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        playerDataStorage = new PlayerDataStorage(this);
        MiniMessage miniMessage = MiniMessage.miniMessage();
        String dbPath = getDataFolder().getAbsolutePath();
        mailStorage = new MailStorage(dbPath);
        punishmentStorage = new PunishmentStorage(dbPath);
        chatHandler = new ChatHandler(this, punishmentStorage);
        tabList = new TabList(this);
        dynamicMotd = new DynamicMotd(this);
        tagFormatter = new TagFormatter(this);

        this.muteExpirationTask = new MuteExpirationTask(punishmentStorage, miniMessage);
        muteExpirationTask.runTaskTimer(this, 0L, 20L);

        this.banExpirationTask = new BanExpirationTask(punishmentStorage, miniMessage);
        banExpirationTask.runTaskTimer(this, 0L, 20L);

        MuteCommand muteCommand = new MuteCommand(this, punishmentStorage, muteExpirationTask, tagFormatter);
        getCommand("mute").setExecutor(muteCommand);
        getCommand("unmute").setExecutor(new UnmuteCommand(this, punishmentStorage, muteExpirationTask));

        getServer().getPluginManager().registerEvents(new DynamicMotd(this), this);
        getServer().getPluginManager().registerEvents(chatHandler, this);
        getServer().getPluginManager().registerEvents(this, this);

        this.getCommand("goto").setExecutor(new TeleportCommand(this));
        this.getCommand("back").setExecutor(new BackCommand(this));
        this.getCommand("message").setExecutor(new MessageCommand(this, playerDataStorage));
        this.getCommand("reply").setExecutor(new ReplyCommand(this, playerDataStorage));
        this.getCommand("message-sound").setExecutor(new MessageSoundCommand(this));
        this.getCommand("rename").setExecutor(new RenameCommand());
        this.getCommand("shrug").setExecutor(new SmugCommand(this, miniMessage, chatHandler));
        this.getCommand("tableflip").setExecutor(new SmugCommand(this, miniMessage, chatHandler));
        this.getCommand("unflip").setExecutor(new SmugCommand(this, miniMessage, chatHandler));
        this.getCommand("mail").setExecutor(new MailCommand(this, playerDataStorage, tagFormatter));
        this.getCommand("mailcheck").setExecutor(new MailCommand(this, playerDataStorage, tagFormatter));
        this.getCommand("mailread").setExecutor(new MailCommand(this, playerDataStorage, tagFormatter));
        this.getCommand("info").setExecutor(new VersionCommand());
        this.getCommand("remove-message").setExecutor(new RemoveMessage(chatHandler));
        this.getCommand("return-message").setExecutor(new ReturnMessage(this, chatHandler));
        this.getCommand("status").setExecutor(new StatusCommand(this, mailStorage, playerDataStorage, punishmentStorage));
        this.getCommand("ban").setExecutor(new BanCommand(this, playerDataStorage, punishmentStorage, miniMessage, banExpirationTask, tagFormatter));
        this.getCommand("pardon").setExecutor(new PardonCommand(this, punishmentStorage));

        this.getCommand("reply").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("back").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("shrug").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("tableflip").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("unflip").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("info").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("mailcheck").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("mute").setTabCompleter(new PunishmentTabCompleter(playerDataStorage));
        this.getCommand("ban").setTabCompleter(new PunishmentTabCompleter(playerDataStorage));
        this.getCommand("unmute").setTabCompleter(new PlayerTabCompleter());
        this.getCommand("message").setTabCompleter(new PlayerTabCompleter());
        this.getCommand("mail").setTabCompleter(new PlayerDBTabCompleter(playerDataStorage));
        this.getCommand("pardon").setTabCompleter(new PardonTabCompleter(punishmentStorage));
        this.getCommand("mailread").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("remmsg").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("retmsg").setTabCompleter(new EmptyTabCompleter());
        this.getCommand("status").setTabCompleter(new StatusTabCompleter());

        this.muteExpirationTask = new MuteExpirationTask(punishmentStorage, miniMessage);
        muteExpirationTask.runTaskTimer(this, 0L, 20L);

        this.banExpirationTask = new BanExpirationTask(punishmentStorage, miniMessage);
        banExpirationTask.runTaskTimer(this, 0L,  20L);

        getServer().getPluginManager().registerEvents(new PlayerBanListener(punishmentStorage), this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerSound(player);
        }

        getLogger().info("MakeABuilders плагин активирован!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MakeABuilders плагин деактивирован.");
        mailStorage.disconnectFromDatabase();
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
        boolean deleted = mailStorage.deleteReadMessages(player.getName());
        if (deleted) {
            getLogger().info("Прочитанные сообщения для игрока " + player.getName() + " удалены.");
        }
    }

    public MailStorage getMailStorage() {
        return mailStorage;
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
        playerDataStorage.updatePlayerData(player);
        return playerDataStorage.getPlayerPrefixByName(player.getName());
    }

    public String getPlayerSuffix(Player player) {
        playerDataStorage.updatePlayerData(player);
        return playerDataStorage.getPlayerSuffixByName(player.getName());
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