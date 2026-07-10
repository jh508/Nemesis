package me.jh508.nemesis.storage;

import me.jh508.nemesis.model.Punishment;
import me.jh508.nemesis.punishment.PunishmentType;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class FileStorage implements PunishmentStorage {

    private final File file;
    private final YamlConfiguration data;
    private final Logger logger;

    public FileStorage(File file, Logger logger)
    {
        this.file = file;
        this.logger = logger;

        if (!file.exists())
        {
            file.getParentFile().mkdirs();
            try
            {
                file.createNewFile();
            }
            catch (IOException e)
            {
                logger.severe("Could not create punishments.yml: " + e.getMessage());
            }
        }

        this.data = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public synchronized void save(Punishment punishment)
    {
        if (punishment.getId() == 0)
        {
            int nextId = data.getInt("next-id", 1);
            punishment.setId(nextId);
            data.set("next-id", nextId + 1);
        }

        String path = "punishments." + punishment.getTarget();
        List<Map<?, ?>> rawEntries = data.getMapList(path);
        List<Map<String, Object>> entries = new ArrayList<>();
        for (Map<?, ?> entry : rawEntries)
        {
            if ((int) entry.get("id") == punishment.getId()) continue;
            entries.add((Map<String, Object>) entry);
        }
        entries.add(toMap(punishment));

        data.set(path, entries);
        saveToDisk();
    }

    @Override
    public synchronized Optional<Punishment> getActiveBan(UUID target)
    {
        return getActive(target, PunishmentType.BAN);
    }

    @Override
    public synchronized Optional<Punishment> getActiveMute(UUID target)
    {
        return getActive(target, PunishmentType.MUTE);
    }

    private Optional<Punishment> getActive(UUID target, PunishmentType type)
    {
        Instant now = Instant.now();

        return getHistory(target).stream()
                .filter(p -> p.getType() == type)
                .filter(Punishment::isActive)
                .filter(p -> p.isPermanent() || p.getExpiresAt().isAfter(now))
                .findFirst();
    }

    @Override
    public synchronized void revoke(int punishmentId, UUID revokedBy)
    {
        Optional<Punishment> found = getById(punishmentId);
        if (found.isEmpty()) return;

        Punishment punishment = found.get();
        punishment.setActive(false);
        punishment.setRevokedBy(revokedBy);
        save(punishment);
    }

    @Override
    public synchronized List<Punishment> getHistory(UUID target)
    {
        List<Map<?, ?>> rawEntries = data.getMapList("punishments." + target);
        List<Punishment> history = new ArrayList<>();

        for (Map<?, ?> entry : rawEntries)
        {
            history.add(fromMap(target, entry));
        }

        return history;
    }

    @Override
    public synchronized Optional<Punishment> getById(int punishmentId)
    {
        if (!data.isConfigurationSection("punishments")) return Optional.empty();

        for (String targetKey : data.getConfigurationSection("punishments").getKeys(false))
        {
            UUID target = UUID.fromString(targetKey);
            for (Punishment punishment : getHistory(target))
            {
                if (punishment.getId() == punishmentId) return Optional.of(punishment);
            }
        }

        return Optional.empty();
    }

    private void saveToDisk()
    {
        try
        {
            data.save(file);
        }
        catch (IOException e)
        {
            logger.severe("Could not save punishments.yml: " + e.getMessage());
        }
    }

    private Map<String, Object> toMap(Punishment p)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("target-display-name", p.getTargetDisplayName());
        map.put("issuer", p.getIssuer().toString());
        map.put("type", p.getType().name());
        map.put("reason", p.getReason());
        map.put("issued-at", p.getIssuedAt().toEpochMilli());
        map.put("expires-at", p.getExpiresAt() == null ? null : p.getExpiresAt().toEpochMilli());
        map.put("active", p.isActive());
        map.put("revoked-by", p.getRevokedBy() == null ? null : p.getRevokedBy().toString());
        return map;
    }

    private Punishment fromMap(UUID target, Map<?, ?> map)
    {
        Long expiresAtMillis = map.get("expires-at") == null ? null : ((Number) map.get("expires-at")).longValue();

        Punishment punishment = new Punishment(
                target,
                (String) map.get("target-display-name"),
                UUID.fromString((String) map.get("issuer")),
                PunishmentType.valueOf((String) map.get("type")),
                (String) map.get("reason"),
                Instant.ofEpochMilli(((Number) map.get("issued-at")).longValue()),
                expiresAtMillis == null ? null : Instant.ofEpochMilli(expiresAtMillis)
        );

        punishment.setId((int) map.get("id"));
        punishment.setActive((boolean) map.get("active"));

        if (map.get("revoked-by") != null)
        {
            punishment.setRevokedBy(UUID.fromString((String) map.get("revoked-by")));
        }

        return punishment;
    }
}
