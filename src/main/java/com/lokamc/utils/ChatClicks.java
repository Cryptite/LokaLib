package com.lokamc.utils;

import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Stateless factory for player-scoped click callbacks, replacing the old
 * FancyMessage/ClickConfirmation "/confirm" command hack with Paper's native
 * {@link ClickEvent#callback}.
 * <p>
 * The old {@code expires} flag mapped to two behaviours:
 * <ul>
 *     <li>single-use: consumed after the first click, short lifetime</li>
 *     <li>reusable: usable repeatedly until it expires</li>
 * </ul>
 * These are preserved here via {@link ClickCallback.Options#uses(int)} and
 * {@link ClickCallback.Options.Builder#lifetime(Duration)}.
 */
public final class ChatClicks {
    private static final Duration SINGLE_USE_LIFETIME = Duration.ofMinutes(1);
    private static final Duration REUSABLE_LIFETIME = Duration.ofMinutes(15);

    private ChatClicks() {
    }

    /**
     * A single-use click callback (equivalent to the old {@code expires = true}).
     */
    public static ClickEvent callback(Consumer<Player> action) {
        return callback(action, false);
    }

    /**
     * @param reusable {@code true} for an infinitely reusable callback (old {@code expires = false}),
     *                 {@code false} for a single-use callback (old {@code expires = true}).
     */
    public static ClickEvent callback(Consumer<Player> action, boolean reusable) {
        ClickCallback.Options options = ClickCallback.Options.builder()
                .uses(reusable ? ClickCallback.UNLIMITED_USES : 1)
                .lifetime(reusable ? REUSABLE_LIFETIME : SINGLE_USE_LIFETIME)
                .build();
        return ClickEvent.callback(audience -> {
            if (audience instanceof Player p) {
                action.accept(p);
            }
        }, options);
    }
}
