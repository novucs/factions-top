package net.novucs.ftop;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

public final class FactionsTopPlugin extends JavaPlugin {

    private final ChunkWorthTask chunkWorthTask = new ChunkWorthTask(this);
    private final Settings settings = new Settings(this);
    private final WorthManager worthManager = new WorthManager(this);
    private FactionsHook factionsHook;

    public ChunkWorthTask getChunkWorthTask() {
        return chunkWorthTask;
    }

    public Settings getSettings() {
        return settings;
    }

    public WorthManager getWorthManager() {
        return worthManager;
    }

    public FactionsHook getFactionsHook() {
        return factionsHook;
    }

    @Override
    public void onEnable() {
        try {
            settings.load();
            chunkWorthTask.start();
        } catch (InvalidConfigurationException e) {
            getLogger().severe("Unable to load settings from config.yml");
            getLogger().severe("The configuration you have provided has invalid syntax.");
            getLogger().severe("Please correct your errors, then reload the plugin.");
            getLogger().log(Level.SEVERE, "The errors are as follows:", e);
        } catch (IOException e) {
            getLogger().severe("Unable to load settings from config.yml");
            getLogger().severe("An I/O exception has occurred.");
            getLogger().log(Level.SEVERE, "The errors are as follows:", e);
        }
    }

    @Override
    public void onDisable() {
        chunkWorthTask.finish();
    }
}
