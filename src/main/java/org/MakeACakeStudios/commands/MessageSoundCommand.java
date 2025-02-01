//package org.MakeACakeStudios.commands;
//
//import net.kyori.adventure.text.minimessage.MiniMessage;
//import org.MakeACakeStudios.Command;
//import org.MakeACakeStudios.MakeABuilders;
//import org.MakeACakeStudios.storage.PlayerDataStorage;
//import org.bukkit.Sound;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import org.incendo.cloud.paper.LegacyPaperCommandManager;
//import org.incendo.cloud.bukkit.BukkitCommandManager;
//import org.incendo.cloud.parser.standard.StringParser;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//
//public class MessageSoundCommand implements Command {
//
//    public static final Map<String, Sound> soundMap = new HashMap<>();
//
//    static {
//        soundMap.put("cat", Sound.ENTITY_CAT_AMBIENT);
//        soundMap.put("anvil", Sound.BLOCK_ANVIL_LAND);
//        soundMap.put("bell", Sound.BLOCK_NOTE_BLOCK_BELL);
//        soundMap.put("chime", Sound.BLOCK_NOTE_BLOCK_CHIME);
//        soundMap.put("xylophone", Sound.BLOCK_NOTE_BLOCK_XYLOPHONE);
//        soundMap.put("cow_bell", Sound.BLOCK_NOTE_BLOCK_COW_BELL);
//        soundMap.put("wolf", Sound.ENTITY_WOLF_AMBIENT);
//        soundMap.put("villager", Sound.ENTITY_VILLAGER_TRADE);
//        soundMap.put("wither", Sound.ENTITY_WITHER_SPAWN);
//        soundMap.put("warden", Sound.ENTITY_WARDEN_LISTENING);
//        soundMap.put("arrow", Sound.ENTITY_ARROW_HIT_PLAYER);
//        soundMap.put("donkey", Sound.ENTITY_DONKEY_AMBIENT);
//    }
//
//    @Override
//    public void register(LegacyPaperCommandManager<CommandSender> manager) {
//        manager.command(
//                manager.commandBuilder("messagesound")
//                        .senderType(Player.class)
//                        .optional("sound", StringParser.stringParser())
//                        .handler(ctx -> {
//                            Player player = (Player) ctx.sender();
//                            String sound = ctx.getOrDefault("sound", null);
//
//                            if(sound == null) {
//                                showCurrentSound(player);
//                            } else {
//                                changeSound(player, sound);
//                            }
//                        })
//        );
//    }
//
//    private void showCurrentSound(Player player) {
//        String currentSound = PlayerDataStorage.instance.getMessageSound(player.getName());
//        player.sendMessage(MiniMessage.miniMessage().deserialize(
//                "<green>Текущий звук уведомлений: <yellow>" + currentSound + "</yellow></green>"));
//    }
//
//    private void changeSound(Player player, String sound) {
//        if(!soundMap.containsKey(sound.toLowerCase())) {
//            player.sendMessage(MiniMessage.miniMessage().deserialize(
//                    "<red>Доступные звуки: " + String.join(", ", soundMap.keySet()) + "</red>"));
//            return;
//        }
//
//        PlayerDataStorage.instance.setMessageSound(player.getName(), sound.toLowerCase());
//        player.sendMessage(MiniMessage.miniMessage().deserialize(
//                "<green>Звук уведомлений изменен на: <yellow>" + sound.toLowerCase() + "</yellow></green>"));
//    }
//}