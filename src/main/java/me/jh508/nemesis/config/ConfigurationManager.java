package me.jh508.nemesis.config;

import me.jh508.nemesis.Nemesis;

public class ConfigurationManager {

    private static ConfigurationManager instance;

    private final Nemesis nemesis;

    private ConfigurationManager(Nemesis nemesis)
    {
        this.nemesis = nemesis;
    }

    public static void init(Nemesis nemesis)
    {
        instance = new ConfigurationManager(nemesis);
    }

    public static ConfigurationManager getInstance()
    {
        return instance;
    }

    public String get(String key)
    {
        return nemesis.getConfig().getString(key);
    }

    public String getStorageMethod()
    {
        return nemesis.getConfig().getString("storage-method", "file");
    }

    public String getSqlHost()
    {
        return nemesis.getConfig().getString("sql.host", "localhost");
    }

    public int getSqlPort()
    {
        return nemesis.getConfig().getInt("sql.port", 3306);
    }

    public String getSqlDatabase()
    {
        return nemesis.getConfig().getString("sql.database", "");
    }

    public String getSqlUser()
    {
        return nemesis.getConfig().getString("sql.user", "root");
    }

    public String getSqlPassword()
    {
        return nemesis.getConfig().getString("sql.password", "");
    }

    public String getSqlTablePrefix()
    {
        return nemesis.getConfig().getString("sql.table-prefix", "");
    }

    public int getSqlPoolSize()
    {
        return nemesis.getConfig().getInt("sql.pool-size", 10);
    }
}