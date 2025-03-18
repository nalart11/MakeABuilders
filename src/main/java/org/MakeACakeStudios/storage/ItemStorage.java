package org.MakeACakeStudios.storage;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import org.MakeACakeStudios.chat.ChatUtils;
import org.MakeACakeStudios.commands.VanishCommand;
import org.MakeACakeStudios.donates.EffectManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static org.MakeACakeStudios.chat.ChatUtils.formatDate;
import static org.MakeACakeStudios.utils.Formatter.miniMessage;

public class ItemStorage {
    public static ItemStorage instance;
    public ItemStorage() {
        instance = this;
    }

    public static final Map<String, Integer> DONATE_EFFECTS = new HashMap<>() {{
        put("Зевс", 1);
        put("Звезда", 2);
        put("Сакура", 3);
        put("Vanila", 4);
    }};

    public static ItemStack getPlaceHolderItem() {
        ItemStack glass_pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass_pane.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());

            glass_pane.setItemMeta(meta);
        }
        return glass_pane;
    }

    public static ItemStack getDonateItem(@NotNull OfflinePlayer player) {
        int donate = DonateStorage.instance.getTotalDonations(player.getName());
        Set<String> purchasedEffects = DonateStorage.instance.getPurchasedDonations(player.getName());

        List<Component> effectsList = purchasedEffects.stream()
                .map(id -> DONATE_EFFECTS.entrySet().stream()
                        .filter(entry -> entry.getValue().toString().equals(id))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse("Неизвестный эффект"))
                .map(effectName -> miniMessage("<!i><white>- " + effectName + "</white>"))
                .collect(Collectors.toList());

        if (effectsList.isEmpty()) {
            effectsList.add(miniMessage("<gray>- Нет эффектов</gray>"));
        }

        ItemStack item;
        Component roleMessage;

        if (donate >= 2000) {
            item = new ItemStack(Material.NETHERITE_INGOT);
            roleMessage = miniMessage("<!i><gradient:#00A53E:#C8FFD4>Спонсор</gradient>");
        } else if (donate >= 150) {
            item = new ItemStack(Material.DIAMOND);
            roleMessage = miniMessage("<!i><gradient:#6EFFC9:#F0FFF7>Донатер</gradient>");
        } else {
            item = new ItemStack(Material.GOLD_NUGGET);
            roleMessage = miniMessage("<!i><gray>Игрок</gray>");
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Component donateMessage = miniMessage("<!i><color:#57EEB2>Донаты</color>");
            Component loreMessage = miniMessage(
                    "<!i><yellow>Сумма доната: <white>" + donate + "р</white></yellow>");
            Component effectsTitle = miniMessage("<!i><yellow>Эффекты:</yellow>");

            List<Component> lore = List.of(roleMessage, Component.empty(), loreMessage, effectsTitle);
            lore = new java.util.ArrayList<>(lore);
            lore.addAll(effectsList);

            meta.displayName(donateMessage);
            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack getBanBarrier(@NotNull OfflinePlayer player) {
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        if (meta != null) {
            String formattedBanEndTime = PunishmentStorage.instance.getFormattedBanEndTime(player.getName());
            String message = formattedBanEndTime.equals("навсегда")
                    ? "<!i><red>Игрок забанен навсегда.</red>"
                    : "<!i><red>Игрок забанен до " + formattedBanEndTime + ".</red>";

            Component statusMessage = miniMessage(message);
            meta.displayName(statusMessage);

            barrier.setItemMeta(meta);
        }
        return barrier;
    }

    public static ItemStack getMuteColor(@NotNull OfflinePlayer player) {
        if (!PunishmentStorage.instance.isMuted(player.getName())) {
            ItemStack limeDye = new ItemStack(Material.LIME_DYE);
            ItemMeta meta = limeDye.getItemMeta();
            if (meta != null) {
                Component statusMessage = miniMessage("<!i><color:#57EEB2>Не замьючен.</color>");
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

                Component statusMessage = miniMessage(message);
                meta.displayName(statusMessage);

                redDye.setItemMeta(meta);
            }
            return redDye;
        }
    }

    public static ItemStack getPlaytimeArmor(@NotNull OfflinePlayer player) {
        int ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        int minutesPlayed = ticksPlayed / (20 * 60);
        int hoursPlayed = minutesPlayed / 60;

        Material armorMaterial;
        if (hoursPlayed < 10) {
            armorMaterial = Material.LEATHER_CHESTPLATE;
        } else if (hoursPlayed < 100) {
            armorMaterial = Material.CHAINMAIL_CHESTPLATE;
        } else if (hoursPlayed < 250) {
            armorMaterial = Material.IRON_CHESTPLATE;
        } else if (hoursPlayed < 500) {
            armorMaterial = Material.GOLDEN_CHESTPLATE;
        } else if (hoursPlayed < 1000) {
            armorMaterial = Material.DIAMOND_CHESTPLATE;
        } else {
            armorMaterial = Material.NETHERITE_CHESTPLATE;
        }

        ItemStack armor = new ItemStack(armorMaterial);
        ItemMeta meta = armor.getItemMeta();
        if (meta != null) {
            long firstJoinMillis = player.getFirstPlayed();
            String firstJoinDate = formatDate(firstJoinMillis);

            boolean isOnline = player.isOnline();
            boolean isVanished = isOnline && VanishCommand.isVanished((Player) player);
            String lastOnlineText;

            if (isOnline && !isVanished) {
                lastOnlineText = "<!i><aqua>Последний онлайн: <green>сейчас";
            } else {
                long lastJoinMillis = player.getLastPlayed();
                String lastJoinDate = formatDate(lastJoinMillis);
                lastOnlineText = "<!i><aqua>Последний онлайн: <green>" + lastJoinDate;
            }

            Component title = miniMessage("<!i><color:#57EEB2>Игровое время</color>");
            Component timePlayedMessage = miniMessage("<!i><gold>Время в игре: <yellow>" + hoursPlayed + " ч " + (minutesPlayed % 60) + " мин");
            Component firstJoinMessage = miniMessage("<!i><aqua>Первый онлайн: <green>" + firstJoinDate);
            Component lastOnlineMessage = miniMessage(lastOnlineText);

            meta.displayName(title);
            meta.lore(List.of(timePlayedMessage, Component.empty(), firstJoinMessage, lastOnlineMessage));

            armor.setItemMeta(meta);
        }
        return armor;
    }

    public static ItemStack getPlayerHead(@NotNull OfflinePlayer player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);

            String playerName = ChatUtils.getFormattedPlayerString(player.getName(), true);

            Component formattedName = miniMessage("<!i><white>" + playerName + "</white>");
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
                    playerRoleText = "<gradient:#E43A96:#FF0000>Раз`работчик</gradient>";
                    break;
                case "moderator":
                    playerRoleText = "<gradient:#23DBFF:#C8E9FF>Модератор</gradient>";
                    break;
                case "sponsor":
                    playerRoleText = "<gradient:#00A53E:#C8FFD4>Спонсор</gradient>";
                    break;
                case "donator":
                    playerRoleText = "<gradient:#6EFFC9:#F0FFF7>Донатер</gradient>";
                    break;
                default:
                    playerRoleText = "<gray>Игрок</gray>";
            }

            Component loreText1 = miniMessage("<!i><yellow>Роль: </yellow>" + playerRoleText);
            List<String> badges = PlayerDataStorage.instance.getPlayerBadges(player.getName());
            if (!badges.equals(List.of())) {
                Component loreText2 = miniMessage("<!i><yellow>Значки: </yellow>" + PlayerDataStorage.instance.getPlayerBadges(player.getName()));
                meta.lore(List.of(loreText1, loreText2));
            } else {
                meta.lore(List.of(loreText1));
            }


            skull.setItemMeta(meta);
        }
        return skull;
    }

    public static ItemStack getDonateCake(@NotNull Player player) {
        ItemStack cake = new ItemStack(Material.CAKE);
        ItemMeta meta = cake.getItemMeta();
        int donate = DonateStorage.instance.getTotalDonations(player.getName());
        if (meta != null) {
            Component donatesCountMessage = miniMessage("<!i><color:#57EEB2>Вы задонатили нам " + donate + " рублей</color>");
            Component donatesThanksMessage = miniMessage("<!i><yellow>Спасибо большое за вашу поддержку!</yellow>");

            meta.displayName(donatesCountMessage);
            meta.lore(List.of(donatesThanksMessage));

            cake.setItemMeta(meta);
        }

        return cake;
    }

    public static ItemStack getSakuraEffect(@NotNull Player player) {
        if (DonateStorage.instance.hasDonation(player.getName(), 3)) {
            ItemStack sakura = new ItemStack(Material.CHERRY_SAPLING);
            ItemMeta meta = sakura.getItemMeta();
            if (meta != null) {
                Component sakuraEffectMessage = miniMessage("<!i><white>Эффект</white> <gradient:#FBE0FF:#F739E0>Сакура</gradient>");
                Component sakuraDescriptionMessage1 = miniMessage("<color:#f2ace9>Нежный, как первая любовь,</color>");
                Component sakuraDescriptionMessage2 = miniMessage("<color:#f2ace9>и прекрасный, как весенний рассвет.</color>");
                Component statusEffectMessage;

                boolean isSakuraEnabled = EffectManager.getEnabledEffectsForPlayer(player.getName()).contains(3);

                if (isSakuraEnabled) {
                    statusEffectMessage = miniMessage("<!i><green>Включен</green>");
                } else {
                    statusEffectMessage = miniMessage("<!i><red>Выключен</red>");
                }

                meta.displayName(sakuraEffectMessage);
                meta.lore(List.of(sakuraDescriptionMessage1, sakuraDescriptionMessage2, Component.empty(), statusEffectMessage));

                sakura.setItemMeta(meta);
            }
            return sakura;
        } else {
            ItemStack gray_dye = new ItemStack(Material.GRAY_DYE);
            ItemMeta meta = gray_dye.getItemMeta();
            if (meta != null) {
                Component sakuraEffectMessage = miniMessage("<!i><white>Эффект</white> <gradient:#FBE0FF:#F739E0>Сакура</gradient>");
                Component sakuraDescriptionMessage1 = miniMessage("<color:#f2ace9>Нежные, как первая любовь,</color>");
                Component sakuraDescriptionMessage2 = miniMessage("<color:#f2ace9>и прекрасные, как весенний рассвет.</color>");
                Component statusEffectMessage = miniMessage("<!i><gray>Не куплен</gray>");

                meta.displayName(sakuraEffectMessage);
                meta.lore(List.of(sakuraDescriptionMessage1, sakuraDescriptionMessage2, Component.empty(), statusEffectMessage));

                gray_dye.setItemMeta(meta);
            }
            return gray_dye;
        }
    }

    public static ItemStack getZeusEffect(@NotNull Player player) {
        if (DonateStorage.instance.hasDonation(player.getName(), 1)) {
            ItemStack tridient = new ItemStack(Material.TRIDENT);
            ItemMeta meta = tridient.getItemMeta();
            if (meta != null) {
                Component zeusEffectMessage = miniMessage("<!i><white>Эффект</white> <gradient:#B46E10:#C31717>Зевс</gradient>");
                Component zeusDescriptionMessage1 = miniMessage("<color:#a62907>Грозовые тучи сгущаются,</color>");
                Component zeusDescriptionMessage2 = miniMessage("<color:#a62907>а гнев самого Зевса сверкает вокруг тебя.</color>");
                Component statusEffectMessage;

                boolean isZeusEnabled = EffectManager.getEnabledEffectsForPlayer(player.getName()).contains(1);

                if (isZeusEnabled) {
                    statusEffectMessage = miniMessage("<!i><green>Включен</green>");
                } else {
                    statusEffectMessage = miniMessage("<!i><red>Выключен</red>");
                }

                meta.displayName(zeusEffectMessage);
                meta.lore(List.of(zeusDescriptionMessage1, zeusDescriptionMessage2, Component.empty(), statusEffectMessage));

                tridient.setItemMeta(meta);
            }
            return tridient;
        } else {
            ItemStack gray_dye = new ItemStack(Material.GRAY_DYE);
            ItemMeta meta = gray_dye.getItemMeta();
            if (meta != null) {
                Component zeusEffectMessage = miniMessage("<!i><white>Эффект</white> <gradient:#B46E10:#C31717>Зевс</gradient>");
                Component zeusDescriptionMessage1 = miniMessage("<color:#a62907>Грозовые тучи сгущаются,</color>");
                Component zeusDescriptionMessage2 = miniMessage("<color:#a62907>а гнев самого Зевса сверкает вокруг тебя.</color>");
                Component statusEffectMessage = miniMessage("<!i><gray>Не куплен</gray>");

                meta.displayName(zeusEffectMessage);
                meta.lore(List.of(zeusDescriptionMessage1, zeusDescriptionMessage2, Component.empty(), statusEffectMessage));

                gray_dye.setItemMeta(meta);
            }
            return gray_dye;
        }
    }

    public static ItemStack getStarEffect(@NotNull Player player) {
        if (DonateStorage.instance.hasDonation(player.getName(), 2)) {
            ItemStack star = new ItemStack(Material.NETHER_STAR);
            ItemMeta meta = star.getItemMeta();
            if (meta != null) {
                Component starEffectMessage = miniMessage("<!i><white>Эффект</white> <gradient:#F0F7B9:#E8921A>Звезда</gradient>");
                Component starDescriptionMessage1 = miniMessage("<color:#ffe47a>Ослепительный звездный свет озаряет тебя,</color>");
                Component starDescriptionMessage2 = miniMessage("<color:#ffe47a>оставляя за тобой след волшебного сияния.</color>");
                Component statusEffectMessage;

                boolean isStarEnabled = EffectManager.getEnabledEffectsForPlayer(player.getName()).contains(2);

                if (isStarEnabled) {
                    statusEffectMessage = miniMessage("<!i><green>Включен</green>");
                } else {
                    statusEffectMessage = miniMessage("<!i><red>Выключен</red>");
                }

                meta.displayName(starEffectMessage);
                meta.lore(List.of(starDescriptionMessage1, starDescriptionMessage2, Component.empty(), statusEffectMessage));

                star.setItemMeta(meta);
            }
            return star;
        } else {
            ItemStack gray_dye = new ItemStack(Material.GRAY_DYE);
            ItemMeta meta = gray_dye.getItemMeta();
            if (meta != null) {
                Component starEffectMessage = miniMessage("<!i><white>Эффект</white> <gradient:#F0F7B9:#E8921A>Звезда</gradient>");
                Component starDescriptionMessage1 = miniMessage("<color:#ffe47a>Ослепительный звездный свет озаряет тебя,</color>");
                Component starDescriptionMessage2 = miniMessage("<color:#ffe47a>оставляя за тобой след волшебного сияния.</color>");
                Component statusEffectMessage = miniMessage("<!i><gray>Не куплен</gray>");

                meta.displayName(starEffectMessage);
                meta.lore(List.of(starDescriptionMessage1, starDescriptionMessage2, Component.empty(), statusEffectMessage));

                gray_dye.setItemMeta(meta);
            }
            return gray_dye;
        }
    }

    public static ItemStack getBirthdayEffect(@NotNull Player player) {
        if (DonateStorage.instance.hasDonation(player.getName(), 12)) {
            ItemStack diamond = new ItemStack(Material.DIAMOND);
            ItemMeta meta = diamond.getItemMeta();
            if (meta != null) {
                Component birthdayEffectMessage = miniMessage("<!i><white>Эффект</white> <gradient:#22DCEE:#11ED7C>День рождения</gradient>");
                Component birthdayDescriptionMessage1 = miniMessage("<color:#7aebdd>Яркие искры фейерверков вспыхивают вокруг тебя,</color>");
                Component birthdayDescriptionMessage2 = miniMessage("<color:#7aebdd>словно праздничный салют, озаряя момент радости и веселья!</color>");
                Component statusEffectMessage;

                boolean isBirthdayEnabled = EffectManager.getEnabledEffectsForPlayer(player.getName()).contains(12);

                if (isBirthdayEnabled) {
                    statusEffectMessage = miniMessage("<!i><green>Включен</green>");
                } else {
                    statusEffectMessage = miniMessage("<!i><red>Выключен</red>");
                }

                meta.displayName(birthdayEffectMessage);
                meta.lore(List.of(birthdayDescriptionMessage1, birthdayDescriptionMessage2, Component.empty(), statusEffectMessage));

                diamond.setItemMeta(meta);
            }
            return diamond;
        } else {
            ItemStack gray_dye = new ItemStack(Material.GRAY_DYE);
            ItemMeta meta = gray_dye.getItemMeta();
            if (meta != null) {
                Component birthdayEffectMessage = miniMessage("<!i><white>Эффект</white> <gradient:#22DCEE:#11ED7C>День рождения!</gradient>");
                Component birthdayDescriptionMessage1 = miniMessage("<color:#7aebdd>Яркие искры фейерверков вспыхивают вокруг тебя,</color>");
                Component birthdayDescriptionMessage2 = miniMessage("<color:#7aebdd>словно праздничный салют, озаряя момент радости и веселья!</color>");
                Component statusEffectMessage = miniMessage("<!i><gray>Можно получить только на свой день рождения</gray>");

                meta.displayName(birthdayEffectMessage);
                meta.lore(List.of(birthdayDescriptionMessage1, birthdayDescriptionMessage2, Component.empty(), statusEffectMessage));

                gray_dye.setItemMeta(meta);
            }
            return gray_dye;
        }
    }

    public static ItemStack getTelegramHead() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            PlayerProfile profile = Bukkit.getServer().createProfile("Telegram");

            profile.setProperty(new ProfileProperty("textures",
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2ViM2E1Mzk5MzdmOGU4ZTExNzAzNGE3MTczYmRkZWQ1YTAzMjNjOTc5NGFhZWRmMTkxODczMWY0Zjg3MjliYiJ9fX0="));

            meta.setPlayerProfile(profile);
            head.setItemMeta(meta);
        }

        return head;
    }

    public static ItemStack getDiscordHead() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            PlayerProfile profile = Bukkit.createProfile("Discord");

            profile.setProperty(new ProfileProperty("textures",
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg3M2MxMmJmZmI1MjUxYTBiODhkNWFlNzVjNzI0N2NiMzlhNzVmZjFhODFjYmU0YzhhMzliMzExZGRlZGEifX19"));

            meta.setPlayerProfile(profile);
            head.setItemMeta(meta);
        }

        return head;
    }

    public static ItemStack getUnavailableBarrier() {
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        if (meta != null) {
            Component unavailableMessage = miniMessage("<!i><red>Недоступно</red>");
            meta.displayName(unavailableMessage);

            barrier.setItemMeta(meta);
        }
        return barrier;
    }
}
