package me.jh508.nemesis;

import com.zaxxer.hikari.HikariDataSource;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import me.jh508.nemesis.command.PunishCommand;
import me.jh508.nemesis.config.ConfigurationManager;
import me.jh508.nemesis.listener.BanListener;
import me.jh508.nemesis.listener.MuteListener;
import me.jh508.nemesis.listener.PunishMenuEvent;
import me.jh508.nemesis.storage.FileStorage;
import me.jh508.nemesis.storage.PunishmentStorage;
import me.jh508.nemesis.storage.SQLStorage;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public final class Nemesis extends JavaPlugin {

    private static Nemesis instance;
    private PunishmentStorage storage;

    private HikariDataSource dataSource;


    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        ConfigurationManager.init(this);

        ConfigurationManager config = ConfigurationManager.getInstance();
        String storageMethod = config.getStorageMethod();
        this.storage = switch (storageMethod.toLowerCase())
        {
            case "file" -> new FileStorage(new File(getDataFolder(), "punishments.yml"), getLogger());
            case "sql" -> {
                try
                {
                    this.dataSource = createSqlDataSource(config);
                    yield new SQLStorage(dataSource, config.getSqlTablePrefix(), getLogger());
                } catch (Exception ex)
                {
                    getLogger().severe("Failed to initialize SQL storage, falling back to file storage: " + ex.getMessage());
                    if (dataSource != null)
                    {
                        dataSource.close();
                        dataSource = null;
                    }
                    yield new FileStorage(new File(getDataFolder(), "punishments.yml"), getLogger());
                }
            }
            default ->
            {
                getLogger().warning("Unknown storage-method '" + storageMethod + "'. Falling back to file storage.");
                yield new FileStorage(new File(getDataFolder(), "punishments.yml"), getLogger());
            }
        };

        this.getCommand("punish").setExecutor(new PunishCommand());
        this.getServer().getPluginManager().registerEvents(new PunishMenuEvent(), this);
        this.getServer().getPluginManager().registerEvents(new MuteListener(storage), this);
        this.getServer().getPluginManager().registerEvents(new BanListener(storage), this);
    }

    @Override
    public void onDisable() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private HikariDataSource createSqlDataSource(ConfigurationManager config)
    {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:mysql://" + config.getSqlHost() + ":" + config.getSqlPort()
                + "/" + config.getSqlDatabase());
        ds.setUsername(config.getSqlUser());
        ds.setPassword(config.getSqlPassword());
        ds.setMaximumPoolSize(config.getSqlPoolSize());
        return ds;
    }

    public static Nemesis getInstance()
    {
        return instance;
    }

    public PunishmentStorage getStorage()
    {
        return storage;
    }
}
