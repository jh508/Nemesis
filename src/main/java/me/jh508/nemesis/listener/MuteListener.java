package me.jh508.nemesis.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.jh508.nemesis.model.Punishment;
import me.jh508.nemesis.storage.PunishmentStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

public class MuteListener implements Listener {

    private final PunishmentStorage storage;

    public MuteListener(PunishmentStorage storage)
    {
        this.storage = storage;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event)
    {
        Optional<Punishment> mute = storage.getActiveMute(event.getPlayer().getUniqueId());
        if (mute.isEmpty()) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("You are muted. Reason: " + mute.get().getReason())
                .color(NamedTextColor.RED));
    }
}
