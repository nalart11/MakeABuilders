package org.MakeACakeStudios.chat0.formatter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

import static org.MakeACakeStudios.utils.Formatter.*;

public enum ChatReplacements {
    // Emojis Scope
    CRY((player, component) -> builder(":cry:", miniMessage("<yellow>☹</yellow><aqua>,</aqua>"))),
    SKULL((player, component) -> builder(":skull:", miniMessage("☠"))),
    SKULLEY((player, component) -> builder(":skulley:", miniMessage("<red>☠'ey</red>"))),
    LOVE((player, component) -> builder("<3", miniMessage("<red>❤</red>"))),
    HEART((player, component) -> builder(":heart:", miniMessage("<red>❤</red>"))),
    FIRE((player, component) -> builder(":cry:", miniMessage("<color:#FF7800>\uD83D\uDD25</color>"))),
    STAR((player, component) -> builder(":cry:", miniMessage("<yellow>★</yellow>"))),
    STOP((player, component) -> builder(":cry:", miniMessage("<red>⚠</red>"))),

    // Additional
    LOC(((player, component) -> {
        var environment = player.getWorld().getEnvironment();
        var worldName = player.getWorld().getName();
        final String gradientOpenTag = switch (environment) {
            case NORMAL -> "<gradient:#00FF1A:#7EFF91>";
            case NETHER -> "<gradient:#FF0000:#FF7E7E>";
            case THE_END -> "<gradient:#ED00FF:#DE7EFF>";
            case CUSTOM -> "<gradient:#FFFFFF:#FFFFFF>";
        };
        final String gradientCloseTag = "</gradient>";
        final int blockX = player.getLocation().getBlockX();
        final int blockY = player.getLocation().getBlockY();
        final int blockZ = player.getLocation().getBlockZ();
        var miniMessage = MiniMessage.miniMessage().deserialize(
                gradientOpenTag + "[" + blockX + "x/" + blockY + "y/" + blockZ + "z/, "
                + worldName + "]" + gradientCloseTag
        );
        var builtComponent = runCommand(miniMessage, "/goto " + worldName + " " + blockX + " " + blockY + " " + blockZ);
        return builder(":loc:", builtComponent);
    }));


    private final @NotNull BiFunction<Player, Component, TextReplacementConfig> invokeResult;

    public static final @NotNull ChatReplacements[] entries = ChatReplacements.values();

    ChatReplacements(@NotNull BiFunction<Player, Component, TextReplacementConfig> builder) {
        this.invokeResult = builder;
    }

    public @NotNull Component replace(@NotNull Player player, @NotNull Component text) {
        return text.replaceText(this.apply(player, text));
    }

    public @NotNull TextReplacementConfig apply(@NotNull Player player, @NotNull Component originalMessage) {
        return invokeResult.apply(player, originalMessage);
    }

    public static @NotNull TextReplacementConfig builder(@RegExp @NotNull String literal, @NotNull Component replacement) {
        return TextReplacementConfig.builder().match(literal).replacement(reset(replacement)).build();
    }
}
