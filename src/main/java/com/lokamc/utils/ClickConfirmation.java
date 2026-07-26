package com.lokamc.utils;

import com.google.common.base.Strings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ClickConfirmation {
    private static final ClickConfirmation instance = new ClickConfirmation();
    private static final Duration CONFIRMATION_LIFETIME = Duration.ofMinutes(1);

    private ClickConfirmation() {
    }

    public void getConfirmation(Player p, Component question, String yesResponse, Consumer<Player> yesCommand) {
        if (question != null) p.sendMessage(question);
        sendQuestion(p, yesResponse, yesCommand, "Cancel", null);
    }

    public void getConfirmation(Player p, String question, String yesResponse, Consumer<Player> yesCommand) {
        getConfirmation(p, question, yesResponse, yesCommand, "Cancel", null);
    }

    public void getConfirmation(Player p, String question, String yesResponse, Consumer<Player> yesCommand, String noResponse, Consumer<Player> noCommand) {
        if (!Strings.isNullOrEmpty(question)) p.sendMessage(StringUtils.legacyToComponent(question));
        sendQuestion(p, yesResponse, yesCommand, noResponse, noCommand);
    }

    private void sendQuestion(Player p, String yesResponse, Consumer<Player> yesCommand, String noResponse, Consumer<Player> noCommand) {
        // A single shared guard so that clicking either button neutralizes the whole group,
        // matching the old group-wide single-use behaviour.
        AtomicBoolean answered = new AtomicBoolean(false);

        Component msg = Component.text("[" + yesResponse + "]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text("Click to confirm!")))
                .clickEvent(guardedCallback(answered, yesCommand));

        if (noResponse != null) {
            msg = msg.append(Component.space())
                    .append(Component.text("[" + noResponse + "]", NamedTextColor.RED, TextDecoration.BOLD)
                            .hoverEvent(HoverEvent.showText(Component.text("Click to deny.")))
                            .clickEvent(guardedCallback(answered, noCommand)));
        }

        p.sendMessage(msg);
    }

    private ClickEvent guardedCallback(AtomicBoolean answered, Consumer<Player> action) {
        ClickCallback.Options options = ClickCallback.Options.builder()
                .uses(ClickCallback.UNLIMITED_USES)
                .lifetime(CONFIRMATION_LIFETIME)
                .build();
        return ClickEvent.callback(audience -> {
            if (audience instanceof Player p && answered.compareAndSet(false, true) && action != null) {
                action.accept(p);
            }
        }, options);
    }

    public void sendClickableComponent(Player p, Component component, Component hover, Consumer<Player> consumer) {
        sendClickableComponent(p, component, hover, consumer, false);
    }

    public void sendClickableComponent(Player p, Component component, Component hover, Consumer<Player> consumer, boolean reusable) {
        p.sendMessage(getClickableComponent(p, component, hover, consumer, reusable));
    }

    public Component getClickableComponent(Player p, Component component, Component hover, Consumer<Player> consumer) {
        return getClickableComponent(p, component, hover, consumer, false);
    }

    public Component getClickableComponent(Player p, Component component, Component hover, Consumer<Player> consumer, boolean reusable) {
        return component
                .hoverEvent(HoverEvent.showText(hover))
                .clickEvent(ChatClicks.callback(consumer, reusable));
    }

    public static ClickConfirmation getInstance() {
        return instance;
    }
}
