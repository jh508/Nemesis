package me.jh508.nemesis;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import me.jh508.nemesis.command.PunishCommand;
import me.jh508.nemesis.config.ConfigurationManager;
import me.jh508.nemesis.listener.BanListener;
import me.jh508.nemesis.listener.MuteListener;
import me.jh508.nemesis.listener.PunishMenuEvent;
import me.jh508.nemesis.storage.FileStorage;
import me.jh508.nemesis.storage.PunishmentStorage;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Nemesis extends JavaPlugin {

    private static Nemesis instance;

    private PunishmentStorage storage;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        ConfigurationManager.init(this);

        String storageMethod = ConfigurationManager.getInstance().getStorageMethod();
        this.storage = switch (storageMethod.toLowerCase())
        {
            case "file" -> new FileStorage(new File(getDataFolder(), "punishments.yml"), getLogger());
            default ->
            {
                getLogger().warning("Unknown or unsupported storage-method '" + storageMethod
                        + "' (SQL storage is not implemented yet). Falling back to file storage.");
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
