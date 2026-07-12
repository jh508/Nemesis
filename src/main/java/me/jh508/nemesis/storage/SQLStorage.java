package me.jh508.nemesis.storage;

import com.zaxxer.hikari.HikariDataSource;
import me.jh508.nemesis.model.Punishment;
import me.jh508.nemesis.punishment.PunishmentType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class SQLStorage implements PunishmentStorage {

    private final HikariDataSource dataSource;
    private final String tableName;
    private final Logger logger;

    public SQLStorage(HikariDataSource dataSource, String tablePrefix, Logger logger)
    {
        if (!tablePrefix.matches("[A-Za-z0-9_]*"))
        {
            throw new IllegalArgumentException("sql.table-prefix must only contain letters, digits, and underscores: " + tablePrefix);
        }

        this.dataSource = dataSource;
        this.tableName = tablePrefix + "punishments";
        this.logger = logger;
        initSchema();
    }

    private void initSchema()
    {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "target CHAR(36) NOT NULL," +
                "target_display_name VARCHAR(16)," +
                "issuer CHAR(36)," +
                "type VARCHAR(16) NOT NULL," +
                "reason TEXT," +
                "issued_at TIMESTAMP NOT NULL," +
                "expires_at TIMESTAMP NULL," +
                "active BOOLEAN NOT NULL DEFAULT TRUE," +
                "revoked_by CHAR(36) NULL," +
                "INDEX idx_target_type_active (target, type, active)" +
                ")";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement())
        {
            stmt.executeUpdate(sql);
        } catch (SQLException ex)
        {
            logger.severe("Failed to initialize SQL schema: " + ex.getMessage());
            throw new RuntimeException("Could not initialize punishment table", ex);
        }
    }

    @Override
    public void save(Punishment punishment) {
        String sql = "INSERT INTO " + tableName +
                " (target, target_display_name, issuer, type, reason, issued_at, expires_at, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            stmt.setString(1, punishment.getTarget().toString());
            stmt.setString(2, punishment.getTargetDisplayName());
            stmt.setString(3, punishment.getIssuer() != null ? punishment.getIssuer().toString() : null);
            stmt.setString(4, punishment.getType().name());
            stmt.setString(5, punishment.getReason());
            stmt.setTimestamp(6, Timestamp.from(punishment.getIssuedAt()));
            stmt.setTimestamp(7, punishment.getExpiresAt() != null ? Timestamp.from(punishment.getExpiresAt()) : null);
            stmt.setBoolean(8, punishment.isActive());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys())
            {
                if (keys.next())
                {
                    punishment.setId(keys.getInt(1));
                }
            }
        } catch (SQLException ex)
        {
            logger.severe("Failed to save punishment: " + ex.getMessage());
            throw new StorageException("Failed to save punishment for " + punishment.getTarget(), ex);
        }
    }

    @Override
    public Optional<Punishment> getActiveBan(UUID target) {
        return getActive(target, PunishmentType.BAN);
    }

    @Override
    public Optional<Punishment> getActiveMute(UUID target) {
        return getActive(target, PunishmentType.MUTE);
    }

    private Optional<Punishment> getActive(UUID target, PunishmentType type)
    {
        String sql = "SELECT * FROM " + tableName + " WHERE target = ? AND type = ? AND active = TRUE LIMIT 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString(1, target.toString());
            stmt.setString(2, type.name());

            try (ResultSet rs = stmt.executeQuery())
            {
                if (rs.next())
                {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex)
        {
            logger.severe("Failed to fetch active " + type + " for " + target + ": " + ex.getMessage());
            throw new StorageException("Failed to fetch active " + type + " for " + target, ex);
        }

        return Optional.empty();
    }

    @Override
    public void revoke(int punishmentId, UUID revokedBy) {
        String sql = "UPDATE " + tableName + " SET active = FALSE, revoked_by = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString(1, revokedBy != null ? revokedBy.toString() : null);
            stmt.setInt(2, punishmentId);
            stmt.executeUpdate();
        } catch (SQLException ex)
        {
            logger.severe("Failed to revoke punishment " + punishmentId + ": " + ex.getMessage());
            throw new StorageException("Failed to revoke punishment " + punishmentId, ex);
        }
    }

    @Override
    public List<Punishment> getHistory(UUID target) {
        String sql = "SELECT * FROM " + tableName + " WHERE target = ? ORDER BY issued_at DESC";
        List<Punishment> history = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString(1, target.toString());

            try (ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    history.add(mapRow(rs));
                }
            }
        } catch (SQLException ex)
        {
            logger.severe("Failed to fetch history for " + target + ": " + ex.getMessage());
            throw new StorageException("Failed to fetch history for " + target, ex);
        }

        return history;
    }

    @Override
    public Optional<Punishment> getById(int punishmentId) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, punishmentId);

            try (ResultSet rs = stmt.executeQuery())
            {
                if (rs.next())
                {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex)
        {
            logger.severe("Failed to fetch punishment " + punishmentId + ": " + ex.getMessage());
            throw new StorageException("Failed to fetch punishment " + punishmentId, ex);
        }

        return Optional.empty();
    }

    private Punishment mapRow(ResultSet rs) throws SQLException
    {
        UUID target = UUID.fromString(rs.getString("target"));
        String targetDisplayName = rs.getString("target_display_name");
        String issuerStr = rs.getString("issuer");
        UUID issuer = issuerStr != null ? UUID.fromString(issuerStr) : null;
        PunishmentType type = PunishmentType.valueOf(rs.getString("type"));
        String reason = rs.getString("reason");
        Instant issuedAt = rs.getTimestamp("issued_at").toInstant();
        Timestamp expiresAtTs = rs.getTimestamp("expires_at");
        Instant expiresAt = expiresAtTs != null ? expiresAtTs.toInstant() : null;

        Punishment punishment = new Punishment(target, targetDisplayName, issuer, type, reason, issuedAt, expiresAt);
        punishment.setId(rs.getInt("id"));
        punishment.setActive(rs.getBoolean("active"));

        String revokedByStr = rs.getString("revoked_by");
        if (revokedByStr != null)
        {
            punishment.setRevokedBy(UUID.fromString(revokedByStr));
        }

        return punishment;
    }
}