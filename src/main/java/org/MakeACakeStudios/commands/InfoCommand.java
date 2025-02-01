package org.MakeACakeStudios.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.MakeACakeStudios.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;

public class InfoCommand implements Command {

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("info")
                        .senderType(Player.class)
                        .handler(ctx -> handle(ctx.sender()))
        );
    }

    private void handle(@NotNull Player sender){
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>----------------</gray>\n\n" +
                    "<click:open_url:'https://github.com/nalart11/MakeABuilders/'><gradient:#FF3D4D:#FCBDBD>MakeABuilders</gradient></click> <yellow>plugin</yellow>\n\n" +
                    "<yellow>Version:</yellow> <green>beta 0.4</green>\n" +
                    "<yellow>Code name:</yellow> <gradient:#E657EE:#FFD8FC>Sakur</gradient><gradient:#FFD8FC:#FFD8FC>a</gradient>\n\n\n" +
                    "<gray>© 2025 MakeACake Studios Ltd.</gray>\n" +
                    "<gray>----------------</gray>"
                    )
        );
    }
}
