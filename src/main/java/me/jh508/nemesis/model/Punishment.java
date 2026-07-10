package me.jh508.nemesis.model;

import me.jh508.nemesis.punishment.PunishmentType;

import java.time.Instant;
import java.util.UUID;

public class Punishment {

    private int id;
    private UUID target;
    private String targetDisplayName;
    private UUID issuer;
    private PunishmentType type;
    private String reason;
    private Instant issuedAt;
    private Instant expiresAt;
    private boolean active;
    private UUID revokedBy;

    public Punishment(UUID target, String targetDisplayName, UUID issuer, PunishmentType type, String reason,
                      Instant issuedAt, Instant expiresAt)
    {
        this.target = target;
        this.targetDisplayName = targetDisplayName;
        this.issuer = issuer;
        this.type = type;
        this.reason = reason;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.active = true;
    }

    public boolean isPermanent()
    {
        return expiresAt == null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getTarget() {
        return target;
    }

    public String getTargetDisplayName()
    {
        return this.targetDisplayName;
    }

    public UUID getIssuer() {
        return issuer;
    }

    public PunishmentType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UUID getRevokedBy() {
        return revokedBy;
    }

    public void setRevokedBy(UUID revokedBy) {
        this.revokedBy = revokedBy;
    }
}
