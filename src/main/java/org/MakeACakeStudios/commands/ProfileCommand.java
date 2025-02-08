package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.chat.ChatUtils;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.MakeACakeStudios.storage.PunishmentStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ProfileCommand implements Command, Listener {

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("profile")
                        .senderType(Player.class)
                        .optional("player", OfflinePlayerParser.offlinePlayerParser())
                        .handler(ctx -> {
                            OfflinePlayer targetPlayer = ctx.getOrDefault("player", (OfflinePlayer) ctx.sender());
                            handle((Player) ctx.sender(), targetPlayer);
                        })
        );
    }

    private void handle(@NotNull Player player, @NotNull OfflinePlayer offlinePlayer) {
        if (!offlinePlayer.isOnline() && offlinePlayer.hasPlayedBefore() == false) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Игрок не существует или не заходил на сервер!</red>"));
        } else {
            Bukkit.getScheduler().runTask(MakeABuilders.instance, () -> {
                Inventory inventory = Bukkit.createInventory(null, 27, "Профиль игрока " + offlinePlayer.getName());

                inventory.setItem(13, getPlayerHead(offlinePlayer));
                inventory.setItem(0, getPlaytimeClock(offlinePlayer));
                inventory.setItem(18, getMuteColor(offlinePlayer));
                if (PunishmentStorage.instance.isBanned(offlinePlayer.getName())) {
                    inventory.setItem(19, getBanBarrier(offlinePlayer));
                }

                player.openInventory(inventory);
            });
        }
    }


    private static ItemStack getPlayerHead(@NotNull OfflinePlayer player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);

            String playerName = ChatUtils.getFormattedPlayerString(player.getName(), true);

            Component formattedName = MiniMessage.miniMessage().deserialize("<!i>" + playerName);
            meta.displayName(formattedName);

            String roleName = PlayerDataStorage.instance.getPlayerRoleByName(player.getName());

            String playerRoleText;
            switch (roleName) {
                case "owner":
                    playerRoleText = "<gradient:#C16E1C:#801CDD>Владелец</gradient>";
                    break;
                case "custom":
                    playerRoleText = "<gradient:#57EEB2:#D8FFED>Особый</gradient>";
                    break;
                case "admin":
                    playerRoleText = "<gradient:#FF2323:#FF7878>Администратор</gradient>";
                    break;
                case "developer":
                    playerRoleText = "<gradient:#E43A96:#FF0000>Разработчик</gradient>";
                    break;
                case "moderator":
                    playerRoleText = "<gradient:#23DBFF:#C8E9FF>Модератор</gradient>";
                    break;
                case "sponsor":
                    playerRoleText = "<gradient:#00A53E:#C8FFD4>Спонсор</gradient>";
                    break;
                default:
                    playerRoleText = "<gray>Игрок</gray>";
            }

            Component loreText1 = MiniMessage.miniMessage().deserialize("<!i><yellow>Роль: </yellow>" + playerRoleText);
            Component loreText2 = MiniMessage.miniMessage().deserialize("<!i><yellow>Значки: </yellow>" + PlayerDataStorage.instance.getPlayerBadges(player.getName()));
            meta.lore(List.of(loreText1, loreText2));

            skull.setItemMeta(meta);
        }
        return skull;
    }

    private static ItemStack getPlaytimeClock(@NotNull OfflinePlayer player) {
        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta meta = clock.getItemMeta();
        if (meta != null) {
            int ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
            int minutesPlayed = ticksPlayed / (20 * 60);
            int hoursPlayed = minutesPlayed / 60;
            minutesPlayed %= 60;

            long firstJoinMillis = player.getFirstPlayed();
            String firstJoinDate = formatDate(firstJoinMillis);

            boolean isOnline = player.isOnline();
            String lastOnlineText;

            if (isOnline) {
                lastOnlineText = "<!i><aqua>Последний онлайн: <green>сейчас";
            } else {
                long lastJoinMillis = player.getLastPlayed();
                String lastJoinDate = formatDate(lastJoinMillis);
                lastOnlineText = "<!i><aqua>Последний онлайн: <green>" + lastJoinDate;
            }

            String timePlayedText = "<!i><gold>Время в игре: <yellow>" + hoursPlayed + " ч " + minutesPlayed + " мин";
            String firstJoinText = "<!i><aqua>Первый вход: <green>" + firstJoinDate;

            Component timeComponent = MiniMessage.miniMessage().deserialize(timePlayedText);
            Component firstJoinComponent = MiniMessage.miniMessage().deserialize(firstJoinText);
            Component lastOnlineComponent = MiniMessage.miniMessage().deserialize(lastOnlineText);

            meta.displayName(MiniMessage.miniMessage().deserialize("<gold>🕰 Часы времени"));
            meta.lore(List.of(timeComponent, firstJoinComponent, lastOnlineComponent));

            clock.setItemMeta(meta);
        }
        return clock;
    }

    private static String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return sdf.format(new Date(millis));
    }

    private static ItemStack getMuteColor(@NotNull OfflinePlayer player) {
        if (!PunishmentStorage.instance.isMuted(player.getName())) {
            ItemStack limeDye = new ItemStack(Material.LIME_DYE);
            ItemMeta meta = limeDye.getItemMeta();
            if (meta != null) {
                Component statusMessage = MiniMessage.miniMessage().deserialize("<!i><green>Не замьючен.</green>");
                meta.displayName(statusMessage);

                limeDye.setItemMeta(meta);
            }
            return limeDye;
        } else {
            ItemStack redDye = new ItemStack(Material.RED_DYE);
            ItemMeta meta = redDye.getItemMeta();
            if (meta != null) {
                String formattedMuteEndTime = PunishmentStorage.instance.getFormattedMuteEndTime(player.getName());
                String message = formattedMuteEndTime.equals("навсегда")
                        ? "<!i><red>Игрок замьючен навсегда.</red>"
                        : "<!i><red>Игрок замьючен до " + formattedMuteEndTime + ".</red>";

                Component statusMessage = MiniMessage.miniMessage().deserialize(message);
                meta.displayName(statusMessage);

                redDye.setItemMeta(meta);
            }
            return redDye;
        }
    }

    private static ItemStack getBanBarrier(@NotNull OfflinePlayer player) {
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        if (meta != null) {
            String formattedBanEndTime = PunishmentStorage.instance.getFormattedBanEndTime(player.getName());
            String message = formattedBanEndTime.equals("навсегда")
                    ? "<!i><red>Игрок забанен навсегда.</red>"
                    : "<!i><red>Игрок забанен до " + formattedBanEndTime + ".</red>";

            Component statusMessage = MiniMessage.miniMessage().deserialize(message);
            meta.displayName(statusMessage);

            barrier.setItemMeta(meta);
        }
        return barrier;
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("Профиль игрока")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().startsWith("Профиль игрока")) {
            event.setCancelled(true);
        }
    }
}
