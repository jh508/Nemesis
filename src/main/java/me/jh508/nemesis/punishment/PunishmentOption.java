package me.jh508.nemesis.punishment;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.time.Duration;

public record PunishmentOption(int slot, Material material, String label, NamedTextColor color,
                               String loreLine, PunishmentType type, Duration duration)
{
    public boolean isPermanent()
    {
        return duration == null;
    }
}
