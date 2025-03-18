package org.MakeACakeStudios.parsers;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCaptionKeys;
import org.incendo.cloud.bukkit.BukkitCommandContextKeys;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

/**
 * Asynchronous parser type that parses into {@link Player}.
 * Uses a {@link CompletableFuture} to prevent blocking the main thread.
 *
 * @param <C> Command sender type
 */
public final class AsyncPlayerParser<C> implements ArgumentParser<C, Player>, BlockingSuggestionProvider.Strings<C> {

    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Player> asyncPlayerParser() {
        return ParserDescriptor.of(new AsyncPlayerParser<>(), Player.class);
    }

    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Player> asyncPlayerComponent() {
        return CommandComponent.<C, Player>builder().parser(asyncPlayerParser());
    }

    @Override
    public @NonNull ArgumentParseResult<Player> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.readString();

        CompletableFuture<ArgumentParseResult<Player>> future = CompletableFuture.supplyAsync(() -> {
            Player player = Bukkit.getPlayer(input);
            if (player == null) {
                return ArgumentParseResult.failure(new PlayerParseException(input, commandContext));
            }
            return ArgumentParseResult.success(player);
        });

        return future.join();
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        final CommandSender bukkit = commandContext.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> !(bukkit instanceof Player && !((Player) bukkit).canSee(player)))
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    public static final class PlayerParseException extends ParserException {

        private final String input;

        public PlayerParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    AsyncPlayerParser.class,
                    context,
                    BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        public @NonNull String input() {
            return this.input;
        }
    }
}