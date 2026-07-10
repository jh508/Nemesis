package me.jh508.nemesis.listener;

import me.jh508.nemesis.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class PunishMenuEvent implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Menu menu)) return;

        event.setCancelled(true);

        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        menu.onClick(player, event.getSlot(), event);
    }

}
