package me.jh508.nemesis.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface Menu extends InventoryHolder {

    void onClick(Player player, int slot, InventoryClickEvent event);

    default ItemStack getPlayerHead(String player)
    {
        player = Character.toUpperCase(player.charAt(0)) + player.substring(1);
        boolean isNewVersion = Arrays.stream(Material.values()).map(Material::name).collect(Collectors.toList()).contains("PLAYER_HEAD");

        Material type = Material.matchMaterial(isNewVersion ? "PLAYER_HEAD" : "SKULL_ITEM");
        ItemStack item = new ItemStack(type, 1);

        if (!isNewVersion)
        {
            item.setDurability((short) 3);
        }

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(player);

        item.setItemMeta(meta);

        return item;
    }

    default ItemStack createItem(Material material, Component name, List<Component> lore)
    {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        if (lore.size() > 0) meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    default Component loreLine(String text)
    {
        return Component.text(text, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false);
    }

    default Component keyValueLine(String label, String value)
    {
        return Component.text(label, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(value, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
    }
}
