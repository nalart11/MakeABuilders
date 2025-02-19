package org.MakeACakeStudios;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.chat.ChatListener;
import org.MakeACakeStudios.chat.TagFormatter;
import org.MakeACakeStudios.commands.*;
import org.MakeACakeStudios.donates.EffectManager;
import org.MakeACakeStudios.donates.effects.StarEffect;
import org.MakeACakeStudios.motd.DynamicMotd;
import org.MakeACakeStudios.tab.TabList;
import org.MakeACakeStudios.other.MuteExpirationTask;
import org.bukkit.*;
import org.MakeACakeStudios.storage.*;
import org.MakeACakeStudios.other.*;
import org.MakeACakeStudios.other.PlayerBanListener;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.A;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ALL")
public final class MakeABuilders extends JavaPlugin implements @NotNull Listener {

    private final HashMap<Player, Player> lastMessaged = new HashMap<>();
    private final HashMap<Player, Sound> playerSounds = new HashMap<>();
    private HashMap<Player, List<Location>> locationHistory = new HashMap<>();

    public LegacyPaperCommandManager<CommandSender> commandManager;
    public static MakeABuilders instance;
    private ChatListener chatListener;
    public FileConfiguration config;
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
        instance = this;

        Bukkit.getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ProfileCommand(), this);
        Bukkit.getPluginManager().registerEvents(new DonateCommand(), this);

        saveDefaultConfig();
        config = getConfig();

        commandManager = new LegacyPaperCommandManager<CommandSender>(
                this,
                ExecutionCoordinator.asyncCoordinator(),
                SenderMapper.identity()
        );

        List.<Command>of(
                new SmugCommand(),
                new MessageCommand(),
                new ReplyCommand(),
                new StatusCommand(),
                new BanCommand(),
                new PardonCommand(),
                new MuteCommand(),
                new UnmuteCommand(),
                new InfoCommand(),
                new MailCommand(),
                new MailCheckCommand(),
                new MailReadCommand(),
                new ProfileCommand(),
                new DonateCommand(),
                new AutoDonateCommand(),
                new AdminCommand(),
                new VanishCommand(),
                new ChatCommand()
        ).forEach(cmd -> cmd.register(commandManager));

        playerDataStorage = new PlayerDataStorage();
        MiniMessage miniMessage = MiniMessage.miniMessage();
        String dbPath = getDataFolder().getAbsolutePath();
        mailStorage = new MailStorage(dbPath);
        punishmentStorage = new PunishmentStorage(dbPath);
        tabList = new TabList(this);
        dynamicMotd = new DynamicMotd(this);
        tagFormatter = new TagFormatter();
        chatListener = new ChatListener();

        this.muteExpirationTask = new MuteExpirationTask(punishmentStorage, miniMessage);
        muteExpirationTask.runTaskTimer(this, 0L, 20L);

        this.banExpirationTask = new BanExpirationTask(punishmentStorage, miniMessage);
        banExpirationTask.runTaskTimer(this, 0L, 20L);

        getServer().getPluginManager().registerEvents(new DynamicMotd(this), this);
        getServer().getPluginManager().registerEvents(chatListener, this);
        getServer().getPluginManager().registerEvents(this, this);

        getServer().getPluginManager().registerEvents(new PlayerBanListener(punishmentStorage), this);

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
        if (DonateStorage.instance.hasDonation(player.getName(), 1) && EffectManager.getEnabledEffectsForPlayer(player.getName()).contains(1)) {
            Location location = player.getLocation();
            World world = location.getWorld();

            LightningStrike lightning = world.strikeLightningEffect(location);
        }
        Location loc = player.getLocation();
        World world = player.getWorld();

        new BukkitRunnable() {
            int count = 0;
            final int maxCount = 3 * 20 / 5;

            @Override
            public void run() {
                if (count >= maxCount) {
                    cancel();
                    return;
                }
                spawnFirework(loc, world);
                count++;
            }
        }.runTaskTimer(this, 0L, 5L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
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

    public void setLastMessaged(Player sender, Player recipient) {
        lastMessaged.put(recipient, sender);
    }

    public Player getLastMessaged(Player player) {
        return lastMessaged.get(player);
    }

    private void spawnFirework(Location loc, World world) {
        Firework firework = (Firework) world.spawn(loc.add(0, 0, 0), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        List<Color> colors = Arrays.asList(
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                Color.PURPLE, Color.ORANGE, Color.AQUA, Color.WHITE
        );

        FireworkEffect effect = FireworkEffect.builder()
                .flicker(true)
                .trail(true)
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(colors)
                .withFade(colors)
                .build();

        meta.addEffect(effect);
        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }
}