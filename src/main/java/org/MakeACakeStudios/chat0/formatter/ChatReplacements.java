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
    CRY((player, component) -> builder(":cry:", miniMessage("<yellow>☹</yellow><aqua>,</aqua>"))),
    SKULL((player, component) -> builder(":skull:", miniMessage("☠"))),
    SKULLEY((player, component) -> builder(":skulley:", miniMessage("<red>☠'ey</red>"))),
    LOVE((player, component) -> builder("<3", miniMessage("<red>❤</red>"))),
    HEART((player, component) -> builder(":heart:", miniMessage("<red>❤</red>"))),
    FIRE((player, component) -> builder(":cry:", miniMessage("<color:#FF7800>\uD83D\uDD25</color>"))),
    STAR((player, component) -> builder(":cry:", miniMessage("<yellow>★</yellow>"))),
    STOP((player, component) -> builder(":cry:", miniMessage("<red>⚠</red>"))),
    SUN(((player, component) -> builder(":sun:", miniMessage("<yellow>☀</yellow>")))),
    MAIL(((player, component) -> builder(":mail:", miniMessage("<yellow>✉</yellow>")))),
    HAPPY(((player, component) -> builder(":happy:", miniMessage("<yellow>☺</yellow>")))),
    SAD(((player, component) -> builder(":sad:", miniMessage("<yellow>☹</yellow>")))),
    UMBRELLA(((player, component) -> builder(":umbrella:", miniMessage("<blue>☂</blue>")))),
    TADA(((player, component) -> builder(":tada:", miniMessage("<color:#FF00FF>\uD83C\uDF89</color>")))),
    NYABOOM(((player, component) -> builder(":nyaboom:", miniMessage("<rainbow>NYABOOM</rainbow> <aqua>:333</aqua>")))),
    NO(((player, component) -> builder(":no:", miniMessage("<red>✖</red>")))),
    YES(((player, component) -> builder(":yes:", miniMessage("<green>✔</green>")))),
    SHRUG(((player, component) -> builder(":shrug:", miniMessage("¯\\_(ツ)_/¯")))),
    TABLEFLIP(((player, component) -> builder(":tableflip:", miniMessage("<red>(╯°□°)╯︵ ┻━┻</red>")))),
    UNFLIP(((player, component) -> builder(":unflip:", miniMessage("┬─┬ノ( º _ ºノ)")))),
    QUESTIONMARK(((player, component) -> builder(":unflip:", miniMessage("<b>???<b>")))),
    SADGE(((player, component) -> builder(":sadge:", miniMessage("<yellow>ⓈⒶⒹⒼⒺ</yellow><aqua>...</aqua>")))),


    LOC(((player, component) -> {
        var environment = player.getWorld().getEnvironment();
        var worldName = player.getWorld().getName();
        final String gradientOpenTag = switch (environment) {
            case NORMAL -> "<gradient:#00FF1A:#7EFF91>";
            case NETHER -> "<gradient:#FF0000:#FF7E7E>";
            case THE_END -> "<gradient:#ED00FF:#DE7EFF>";
            case CUSTOM -> "<gradient:#FFFFFF:#FFFFFF>";
        };
        final String checkWorldName = switch (environment) {
            case NORMAL -> "overworld";
            case NETHER -> "the_nether";
            case THE_END -> "the_end";
            case CUSTOM -> worldName;
        };
        final String gradientCloseTag = "</gradient>";
        final int blockX = player.getLocation().getBlockX();
        final int blockY = player.getLocation().getBlockY();
        final int blockZ = player.getLocation().getBlockZ();
        var miniMessage = MiniMessage.miniMessage().deserialize(
                gradientOpenTag + "[" + blockX + "x/" + blockY + "y/" + blockZ + "z/, "
                + worldName + "]" + gradientCloseTag
        );
        var builtComponent = runCommand(miniMessage, "/execute in minecraft:" + checkWorldName + " run tp " + blockX + " " + blockY + " " + blockZ);
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
        String processedLiteral = literal.replaceAll("\\\\", "\\\\\\\\");
        return TextReplacementConfig.builder().match(processedLiteral).replacement(reset(replacement)).build();
    }
}
