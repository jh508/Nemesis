package me.jh508.nemesis.menu;

import me.jh508.nemesis.Nemesis;
import me.jh508.nemesis.model.Punishment;
import me.jh508.nemesis.punishment.PunishmentOption;
import me.jh508.nemesis.punishment.PunishmentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PunishMainMenu implements Menu {

    private record Header(int slot, Material material, String label, String lineOne, String lineTwo) {}

    private static final List<Header> HEADERS = List.of(
            new Header(10, Material.OAK_SIGN, "Chat Offence", "These punishments are for", "chat offences."),
            new Header(12, Material.HOPPER, "Client Mod", "These punishments are for", "client mods."),
            new Header(14, Material.IRON_SWORD, "Hacking", "These punishments are for", "hacked clients and cheating.")
    );

    private static final List<PunishmentOption> PUNISHMENT_OPTIONS = List.of(
            new PunishmentOption(19, Material.GREEN_DYE, "Mute - 1 day", NamedTextColor.GREEN, "Low level offence", PunishmentType.MUTE, Duration.ofDays(1)),
            new PunishmentOption(28, Material.ORANGE_DYE, "Mute - 7 day", NamedTextColor.GOLD, "Medium level offence", PunishmentType.MUTE, Duration.ofDays(7)),
            new PunishmentOption(37, Material.RED_DYE, "Mute - 30 day", NamedTextColor.RED, "High level offence", PunishmentType.MUTE, Duration.ofDays(30)),

            new PunishmentOption(21, Material.GREEN_DYE, "Ban - 1 day", NamedTextColor.GREEN, "Low level offence", PunishmentType.BAN, Duration.ofDays(1)),
            new PunishmentOption(30, Material.ORANGE_DYE, "Ban - 7 day", NamedTextColor.GOLD, "Medium level offence", PunishmentType.BAN, Duration.ofDays(7)),
            new PunishmentOption(39, Material.ORANGE_DYE, "Ban - 30 day", NamedTextColor.RED, "High level offence", PunishmentType.BAN, Duration.ofDays(30)),

            new PunishmentOption(23, Material.GREEN_DYE, "Ban - 1 day", NamedTextColor.GREEN, "Low level offence", PunishmentType.BAN, Duration.ofDays(1)),
            new PunishmentOption(32, Material.ORANGE_DYE, "Ban - 7 day", NamedTextColor.GOLD, "Medium level offence", PunishmentType.BAN, Duration.ofDays(7)),
            new PunishmentOption(41, Material.RED_DYE, "Ban - 30 day", NamedTextColor.RED, "High level offence", PunishmentType.BAN, Duration.ofDays(30)),

            new PunishmentOption(25, Material.PAPER, "Warning", NamedTextColor.GOLD, "Give the player a warning", PunishmentType.WARN, Duration.ZERO),
            new PunishmentOption(34, Material.REDSTONE_BLOCK, "Permanent Ban", NamedTextColor.RED, "Permanently ban the player.", PunishmentType.BAN, null),
            new PunishmentOption(43, Material.ENCHANTED_BOOK, "Permanent Mute", NamedTextColor.RED, "Permanently mute the player.", PunishmentType.MUTE, null)
    );

    private static final DateTimeFormatter HISTORY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm").withZone(ZoneId.systemDefault());

    private static final int HISTORY_START_SLOT = 45;
    private static final int HISTORY_END_SLOT = 53;

    private final Inventory inventory;
    private final Map<Integer, PunishmentOption> punishmentsBySlot;
    private final Map<Integer, Punishment> historyBySlot = new HashMap<>();
    private final UUID issuer;
    private final UUID targetId;
    private final String target;
    private final String reason;

    public PunishMainMenu(Nemesis nemesis, UUID issuer, String target, String reason)
    {
        this.issuer = issuer;
        this.target = target;
        this.targetId = Bukkit.getOfflinePlayer(target).getUniqueId();
        this.reason = reason;
        this.punishmentsBySlot = PUNISHMENT_OPTIONS.stream().collect(Collectors.toMap(PunishmentOption::slot, p -> p));

        this.inventory = nemesis.getServer().createInventory(this, 54, "Punish " + target);

        this.inventory.setItem(4, getPlayerHead(target));

        for (Header header : HEADERS)
        {
            this.inventory.setItem(header.slot(), createItem(header.material(),
                    Component.text(header.label()).decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN),
                    List.of(loreLine(header.lineOne()), loreLine(header.lineTwo()))));
        }

        for (PunishmentOption punishmentOption : PUNISHMENT_OPTIONS)
        {
            this.inventory.setItem(punishmentOption.slot(), createItem(punishmentOption.material(),
                    Component.text(punishmentOption.label()).decorate(TextDecoration.BOLD).color(punishmentOption.color()),
                    List.of(loreLine(punishmentOption.loreLine()))));
        }

        createHistory();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public void onClick(Player player, int slot, InventoryClickEvent event)
    {
        Punishment historyEntry = historyBySlot.get(slot);
        if (historyEntry != null)
        {
            if (historyEntry.isActive())
            {
                Bukkit.getScheduler().runTaskAsynchronously(Nemesis.getInstance(), () -> {
                    Nemesis.getInstance().getStorage().revoke(historyEntry.getId(), issuer);

                    Bukkit.getScheduler().runTask(Nemesis.getInstance(), () -> {
                        player.sendMessage("Revoked " + historyEntry.getType() + " #" + historyEntry.getId() + " for " + target + ".");
                        createHistory();
                    });
                });
            }
            return;
        }

        PunishmentOption punishmentOption = punishmentsBySlot.get(slot);
        if (punishmentOption == null) return;

        switch (punishmentOption.type())
        {
            case BAN -> ban(punishmentOption);
            case MUTE -> mute(punishmentOption);
            case WARN -> warn(punishmentOption);
        }

        player.sendMessage(punishmentOption.label() + " applied to " + target + ".");
        player.closeInventory();
    }

    private void ban(PunishmentOption punishmentOption)
    {
        Instant now = Instant.now();
        Instant expiresAt = punishmentOption.isPermanent() ? null : now.plus(punishmentOption.duration());

        Punishment punishment = new Punishment(targetId, target, issuer, PunishmentType.BAN, reason, now, expiresAt);
        saveAsync(punishment);

        Player online = Bukkit.getPlayerExact(target);
        if (online != null)
        {
            online.kick(Component.text("You have been banned")
                    .color(NamedTextColor.RED)
                    .appendNewline().append(Component.text("Reason: " + reason).color(NamedTextColor.YELLOW))
                    .appendNewline().append(Component.text("Unfairly banned? Appeal at www.coreplex.com/appeals").color(NamedTextColor.GRAY))
            );
        }
    }

    private void mute(PunishmentOption punishmentOption)
    {
        Instant now = Instant.now();
        Instant expiresAt = punishmentOption.isPermanent() ? null : now.plus(punishmentOption.duration());

        Punishment punishment = new Punishment(targetId, target, issuer, PunishmentType.MUTE, reason, now, expiresAt);
        saveAsync(punishment);
    }

    private void warn(PunishmentOption punishmentOption)
    {
        Instant now = Instant.now();

        Punishment punishment = new Punishment(targetId, target, issuer, PunishmentType.WARN, reason, now, null);
        saveAsync(punishment);

        Player online = Bukkit.getPlayerExact(target);
        if (online != null)
        {
            online.sendMessage(Component.text("You have been warned. Reason: " + reason)
                    .color(NamedTextColor.GOLD));
        }
    }

    private void saveAsync(Punishment punishment)
    {
        Bukkit.getScheduler().runTaskAsynchronously(Nemesis.getInstance(),
                () -> Nemesis.getInstance().getStorage().save(punishment));
    }

    private void createHistory()
    {
        Bukkit.getScheduler().runTaskAsynchronously(Nemesis.getInstance(), () -> {
            List<Punishment> history = Nemesis.getInstance().getStorage().getHistory(targetId).stream()
                    .sorted(Comparator.comparing(Punishment::getIssuedAt).reversed())
                    .collect(Collectors.toList());

            Bukkit.getScheduler().runTask(Nemesis.getInstance(), () -> applyHistory(history));
        });
    }

    private void applyHistory(List<Punishment> history)
    {
        historyBySlot.clear();

        for (int slot = HISTORY_START_SLOT; slot <= HISTORY_END_SLOT; slot++)
        {
            int index = slot - HISTORY_START_SLOT;
            if (index >= history.size())
            {
                this.inventory.setItem(slot, null);
                continue;
            }

            Punishment punishment = history.get(index);
            historyBySlot.put(slot, punishment);
            this.inventory.setItem(slot, createHistoryItem(punishment));
        }
    }

    private ItemStack createHistoryItem(Punishment punishment)
    {
        NamedTextColor color = punishment.isActive() ? NamedTextColor.GREEN : NamedTextColor.GRAY;
        String status = punishment.isActive() ? "Active" : "Revoked";

        ItemStack item = createItem(historyIcon(punishment),
                Component.text(punishment.getType() + " (" + status + ")").decorate(TextDecoration.BOLD).color(color),
                historyLore(punishment));

        if (punishment.isActive())
        {
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(Enchantment.EFFICIENCY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }

        return item;
    }

    private Material historyIcon(Punishment punishment)
    {
        if (punishment.isPermanent())
        {
            return switch (punishment.getType())
            {
                case BAN -> Material.REDSTONE_BLOCK;
                case MUTE -> Material.ENCHANTED_BOOK;
                case WARN -> Material.PAPER;
            };
        }

        return switch (punishment.getType())
        {
            case BAN -> Material.IRON_SWORD;
            case MUTE -> Material.BOOK;
            case WARN -> Material.PAPER;
        };
    }

    private List<Component> historyLore(Punishment punishment)
    {
        List<Component> lore = new ArrayList<>();

        lore.add(keyValueLine("Reason: ", punishment.getReason()));
        lore.add(keyValueLine("Issued: ", HISTORY_DATE_FORMAT.format(punishment.getIssuedAt())));
        lore.add(keyValueLine("Expires: ", punishment.isPermanent()
                ? "Never"
                : HISTORY_DATE_FORMAT.format(punishment.getExpiresAt())));
        lore.add(keyValueLine("Issuer: ", Bukkit.getOfflinePlayer(punishment.getIssuer()).getName()));

        if (!punishment.isActive() && punishment.getRevokedBy() != null)
        {
            lore.add(keyValueLine("Revoked by: ", Bukkit.getOfflinePlayer(punishment.getRevokedBy()).getName()));
        }

        if (punishment.isActive())
        {
            lore.add(loreLine("Click to revoke"));
        }

        return lore;
    }
}
