package org.MakeACakeStudios.parsers;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCaptionKeys;
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
 * Asynchronous parser type that parses into {@link OfflinePlayer}.
 * <p>
 * Uses a {@link CompletableFuture} to prevent blocking the main thread.
 *
 * @param <C> Command sender type
 */
public final class AsyncOfflinePlayerParser<C> implements ArgumentParser<C, OfflinePlayer>, BlockingSuggestionProvider.Strings<C> {

    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, OfflinePlayer> asyncOfflinePlayerParser() {
        return ParserDescriptor.of(new AsyncOfflinePlayerParser<>(), OfflinePlayer.class);
    }

    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, OfflinePlayer> asyncOfflinePlayerComponent() {
        return CommandComponent.<C, OfflinePlayer>builder().parser(asyncOfflinePlayerParser());
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NonNull ArgumentParseResult<OfflinePlayer> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.readString();
        if (!input.matches("^[a-zA-Z0-9_]{3,16}$")) {
            return ArgumentParseResult.failure(new OfflinePlayerParseException(input, commandContext));
        }

        CompletableFuture<ArgumentParseResult<OfflinePlayer>> future = CompletableFuture.supplyAsync(() -> {
            try {
                OfflinePlayer player = Bukkit.getOfflinePlayer(input);
                return ArgumentParseResult.success(player);
            } catch (final Exception e) {
                return ArgumentParseResult.failure(new OfflinePlayerParseException(input, commandContext));
            }
        });

        return future.join();
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        return Stream.concat(
                        Bukkit.getOnlinePlayers().stream(),
                        Arrays.stream(Bukkit.getOfflinePlayers())
                )
                .filter(Objects::nonNull)
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .filter(name -> !name.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }


    public static final class OfflinePlayerParseException extends ParserException {

        private final String input;

        public OfflinePlayerParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    AsyncOfflinePlayerParser.class,
                    context,
                    BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        public @NonNull String input() {
            return this.input;
        }
    }
}
