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
}