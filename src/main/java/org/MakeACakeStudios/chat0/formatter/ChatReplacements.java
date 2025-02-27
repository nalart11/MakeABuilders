package org.MakeACakeStudios.chat0.formatter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

import static org.MakeACakeStudios.utils.Formatter.miniMessage;
import static org.MakeACakeStudios.utils.Formatter.runCommand;

public enum ChatReplacements {
    // Emojis Scope
    CRY(":cry:", (player, component) -> builder(":cry:", miniMessage("<yellow>☹</yellow><aqua>,</aqua>"))),
    SKULL(":skull:", (player, component) -> builder(":skull:", miniMessage("☠"))),
    SKULLEY(":skulley:", (player, component) -> builder(":skulley:", miniMessage("<red>☠'ey</red>"))),
    LOVE("<3", (player, component) -> builder("<3", miniMessage("<red>❤</red>"))),
    HEART(":heart:", (player, component) -> builder(":heart:", miniMessage("<red>❤</red>"))),
    FIRE(":fire:", (player, component) -> builder(":cry:", miniMessage("<color:#FF7800>\uD83D\uDD25</color>"))),
    STAR(":star:", (player, component) -> builder(":cry:", miniMessage("<yellow>★</yellow>"))),
    STOP(":stop:", (player, component) -> builder(":cry:", miniMessage("<red>⚠</red>"))),

    // Additional
    LOC(":loc:", ((player, component) -> {
        var environment = player.getWorld().getEnvironment();
        var worldName = player.getWorld().getName();
        final String gradientOpenTag = switch (environment) {
            case NORMAL -> "<gradient:#00FF1A:#7EFF91>";
            case NETHER -> "<gradient:#FF0000:#FF7E7E>";
            case THE_END -> "<gradient:#ED00FF:#DE7EFF>";
            case CUSTOM -> "<gradient:#FFFFFF:#FFFFFF>";
        };
        final String gradientCloseTag = "</gradient";
        final int blockX = player.getLocation().getBlockX();
        final int blockY = player.getLocation().getBlockY();
        final int blockZ = player.getLocation().getBlockZ();
        var miniMessage = MiniMessage.miniMessage().deserialize(
                gradientOpenTag + "[" + blockX + "x/" + blockY + "y/" + blockZ + "z/, "
                + worldName + "]" + gradientCloseTag
        );
        var builtComponent = runCommand(miniMessage, "/goto " + worldName + " " + blockX + " " + blockY + " " + blockZ);
        return TextReplacementConfig.builder().match(":loc:").replacement(builtComponent).build();
    }));


    private final @NotNull String tag;
    private final @NotNull BiFunction<Player, Component, TextReplacementConfig> invokeResult;

    public static final @NotNull ChatReplacements[] entries = ChatReplacements.values();

    ChatReplacements(@NotNull String tag, @NotNull BiFunction<Player, Component, TextReplacementConfig> builder) {
        this.tag = tag;
        this.invokeResult = builder;
    }

    public @NotNull Component replace(@NotNull Player player, @NotNull Component text) {
        return text.replaceText(this.invoke(player, text));
    }

    public @NotNull TextReplacementConfig invoke(@NotNull Player player, @NotNull Component originalMessage) {
        return invokeResult.apply(player, originalMessage);
    }

    public static @NotNull TextReplacementConfig builder(@RegExp @NotNull String literal, @NotNull Component replacement) {
        return TextReplacementConfig.builder().match(literal).replacement(replacement).build();
    }

    public @NotNull String getTag() {
        return this.tag;
    }
}
