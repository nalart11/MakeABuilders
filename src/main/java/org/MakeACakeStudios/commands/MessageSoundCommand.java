package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.MakeACakeStudios.storage.PlayerDataStorage;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MessageSoundCommand implements Command {

    public enum Sounds {
        CAT("cat", Sound.ENTITY_CAT_AMBIENT),
        ANVIL("anvil", Sound.BLOCK_ANVIL_LAND),
        BELL("bell", Sound.BLOCK_NOTE_BLOCK_BELL),
        CHIME("chime", Sound.BLOCK_NOTE_BLOCK_CHIME),
        XYLOPHONE("xylophone", Sound.BLOCK_NOTE_BLOCK_XYLOPHONE),
        COW_BELL("cow_bell", Sound.BLOCK_NOTE_BLOCK_COW_BELL),
        WOLF("wolf", Sound.ENTITY_WOLF_AMBIENT),
        VILLAGER("villager", Sound.ENTITY_VILLAGER_TRADE),
        WITHER("wither", Sound.ENTITY_WITHER_SPAWN),
        WARDEN("warden", Sound.ENTITY_WARDEN_LISTENING),
        ARROW("arrow", Sound.ENTITY_ARROW_HIT_PLAYER),
        DONKEY("donkey", Sound.ENTITY_DONKEY_AMBIENT)
        ;
        public String soundName;
        public Sound sound;
        Sounds(String soundName, Sound sound){
            this.soundName = soundName;
            this.sound = sound;
        }
    }

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("message-sound", "msgs")
                        .senderType(Player.class)
                        .literal("set")
                        .optional("sound", EnumParser.enumParser(Sounds.class))
                        .handler(ctx -> handle(ctx.sender(), ctx.get("sound")))
        );
    }

    private void handle(@NotNull Player player, Sounds sound) {
        PlayerDataStorage storage = PlayerDataStorage.instance;

//        if (!soundMap.containsKey(soundName)) {
//            player.sendMessage(MiniMessage.miniMessage().deserialize(
//                    "<red>Звук \"" + soundName + "\" не найден! Используйте один из доступных звуков: " +
//                            String.join(", ", soundMap.keySet()) + ".</red>"));
//            return;
//        }


        storage.setNotificationSound(player.getName(), sound.soundName);
        System.out.println(player.getName() + " " + sound.soundName);
        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<green>Звук уведомлений успешно изменен на:</green> <yellow>" + sound.soundName + "</yellow>"));
    }

    private void check(@NotNull Player player) {
        PlayerDataStorage storage = PlayerDataStorage.instance;
        String currentSound = storage.getNotificationSound(player.getName());
        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<green>Текущий звук уведомлений:</green> <yellow>" + currentSound + "</yellow>"));
    }
}
