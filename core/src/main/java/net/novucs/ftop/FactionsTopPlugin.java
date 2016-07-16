package net.novucs.ftop;

import com.google.common.collect.ImmutableList;
import net.novucs.ftop.hook.Factions16x;
import net.novucs.ftop.hook.Factions27x;
import net.novucs.ftop.hook.FactionsHook;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

public final class FactionsTopPlugin extends JavaPlugin {

    private final Settings settings = new Settings(this);
    private final WorthManager worthManager = new WorthManager(this);
    private final ImmutableList<PluginService> services = ImmutableList.of(
            new ChunkWorthTask(this),
            new FactionsTopCommand(this),
            new WorldListener(this)
    );

    private boolean active;
    private FactionsHook factionsHook;

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
        if (!loadFactionsHook()) {
            getLogger().severe("No valid version of factions was found!");
            getLogger().severe("Disabling FactionsTop . . .");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        loadSettings();
    }

    @Override
    public void onDisable() {
        services.forEach(PluginService::terminate);
        active = false;
    }

    public boolean loadFactionsHook() {
        Plugin factions = getServer().getPluginManager().getPlugin("Factions");
        if (factions == null) {
            return false;
        }

        // Attempt to find a valid hook for the factions version.
        switch (factions.getDescription().getVersion().substring(0, 3)) {
            case "1.6":
                factionsHook = new Factions16x(this);
                return true;
            case "2.7":
            case "2.8":
                factionsHook = new Factions27x(this);
                return true;
        }

        return false;
    }

    /**
     * Attempts to load plugin settings from disk. In the event of an error,
     * the plugin will disable all services until the settings have been
     * corrected.
     */
    public void loadSettings() {
        try {
            // Attempt to load the plugin settings.
            settings.load();

            // Re-enable all plugin modules if currently inactive.
            if (!active) {
                services.forEach(PluginService::initialize);
            }

            // Update the plugin state to active.
            active = true;
            return;
        } catch (InvalidConfigurationException e) {
            getLogger().severe("Unable to load settings from config.yml");
            getLogger().severe("The configuration you have provided has invalid syntax.");
            getLogger().severe("Please correct your errors, then loadSettings the plugin.");
            getLogger().log(Level.SEVERE, "The errors are as follows:", e);
        } catch (IOException e) {
            getLogger().severe("Unable to load settings from config.yml");
            getLogger().severe("An I/O exception has occurred.");
            getLogger().log(Level.SEVERE, "The errors are as follows:", e);
        }

        // Disable all services and update the plugin state.
        services.forEach(PluginService::terminate);
        active = false;
    }
}
