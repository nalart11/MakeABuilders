package org.MakeACakeStudios.commands;

import org.MakeACakeStudios.MakeABuilders;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageSoundCommand implements CommandExecutor, TabCompleter {

    private final MakeABuilders plugin;
    private final Map<String, Sound> soundMap = new HashMap<>();

    public MessageSoundCommand(MakeABuilders plugin) {
        this.plugin = plugin;

        soundMap.put("cat", Sound.ENTITY_CAT_AMBIENT);
        soundMap.put("anvil", Sound.BLOCK_ANVIL_LAND);
        soundMap.put("bell", Sound.BLOCK_NOTE_BLOCK_BELL);
        soundMap.put("chime", Sound.BLOCK_NOTE_BLOCK_CHIME);
        soundMap.put("xylophone", Sound.BLOCK_NOTE_BLOCK_XYLOPHONE);
        soundMap.put("cow_bell", Sound.BLOCK_NOTE_BLOCK_COW_BELL);
        soundMap.put("wolf", Sound.ENTITY_WOLF_AMBIENT);
        soundMap.put("villager", Sound.ENTITY_VILLAGER_TRADE);
        soundMap.put("wither", Sound.ENTITY_WITHER_SPAWN);
        soundMap.put("warden", Sound.ENTITY_WARDEN_LISTENING);
        soundMap.put("arrow", Sound.ENTITY_ARROW_HIT_PLAYER);
        soundMap.put("donkey", Sound.ENTITY_DONKEY_AMBIENT);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игроки могут использовать эту команду.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage("Использование: /msgs <звук>");
            return true;
        }

        String soundKey = args[0].toLowerCase();

        if (!soundMap.containsKey(soundKey)) {
            player.sendMessage("Недопустимый звук. Введите один из доступных звуков.");
            return true;
        }

        Sound selectedSound = soundMap.get(soundKey);
        plugin.setPlayerSound(player, selectedSound);
        player.sendMessage("Звук уведомлений изменён на: " + soundKey);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String currentInput = args[0].toLowerCase();
            for (String key : soundMap.keySet()) {
                if (key.startsWith(currentInput)) {
                    completions.add(key);
                }
            }
            return completions;
        }
        return new ArrayList<>();
    }
}
