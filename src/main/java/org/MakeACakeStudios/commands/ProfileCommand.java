package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.MakeABuilders;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
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
                        .handler(ctx -> handle(ctx.sender(), ctx.get("player")))
        );
    }

    private void handle(@NotNull Player player, OfflinePlayer offlinePlayer) {
        Bukkit.getScheduler().runTask(MakeABuilders.instance, () -> {
            Inventory inventory = Bukkit.createInventory(null, 27, "Профиль игрока " + offlinePlayer.getName());

            if (offlinePlayer == null) {
                inventory.setItem(13, getPlayerHead(player));
                inventory.setItem(0, getPlaytimeClock(player));
            } else {
                inventory.setItem(13, getPlayerHead(offlinePlayer));
                inventory.setItem(0, getPlaytimeClock(offlinePlayer));
            }

            player.openInventory(inventory);
        });
    }


    private ItemStack getPlayerHead(@NotNull OfflinePlayer player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);

            String senderPrefix = PlayerDataStorage.instance.getPlayerPrefixByName(player.getName());
            String senderSuffix = PlayerDataStorage.instance.getPlayerSuffixByName(player.getName());
            String senderName = senderPrefix + player.getName() + senderSuffix;

            Component formattedName = MiniMessage.miniMessage().deserialize("<!i>" + senderName);
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
            Component loreText2 = MiniMessage.miniMessage().deserialize("<!i><yellow>Значки: </yellow>" + senderPrefix);
            meta.lore(List.of(loreText1, loreText2));

            skull.setItemMeta(meta);
        }
        return skull;
    }

    public static ItemStack getPlaytimeClock(@NotNull OfflinePlayer player) {
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
}
