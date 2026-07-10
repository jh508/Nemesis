package me.jh508.nemesis.storage;

import me.jh508.nemesis.model.Punishment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PunishmentStorage {

    void save(Punishment punishment);


    Optional<Punishment> getActiveBan(UUID target);
    Optional<Punishment> getActiveMute(UUID target);

    void revoke(int punishmentId, UUID revokedBy);

    List<Punishment> getHistory(UUID target);

    Optional<Punishment> getById(int punishmentId);
}