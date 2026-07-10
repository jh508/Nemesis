package me.jh508.nemesis.listener;

import me.jh508.nemesis.config.ConfigurationManager;
import me.jh508.nemesis.model.Punishment;
import me.jh508.nemesis.storage.PunishmentStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class BanListener implements Listener {

    private static final DateTimeFormatter EXPIRY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final PunishmentStorage storage;

    public BanListener(PunishmentStorage storage)
    {
        this.storage = storage;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPreLogin(AsyncPlayerPreLoginEvent event)
    {
        Optional<Punishment> ban = storage.getActiveBan(event.getUniqueId());
        if (ban.isEmpty()) return;

        Punishment punishment = ban.get();

        String expiry = punishment.isPermanent()
                ? "This ban does not expire."
                : "Expires: " + EXPIRY_FORMAT.format(punishment.getExpiresAt());

        Component message = Component.text("You have been banned", NamedTextColor.RED)
                .appendNewline()
                .appendNewline().append(Component.text("Reason: " + punishment.getReason(), NamedTextColor.YELLOW))
                .appendNewline().append(Component.text(expiry, NamedTextColor.GRAY))
                .appendNewline()
                .appendNewline().append(Component.text("Appeal at " + ConfigurationManager.getInstance().get("appeals-url"), NamedTextColor.GRAY));

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message);
    }
}
