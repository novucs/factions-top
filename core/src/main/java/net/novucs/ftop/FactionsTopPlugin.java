package net.novucs.ftop;

import net.novucs.ftop.hook.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public final class FactionsTopPlugin extends JavaPlugin {

    private final Settings settings = new Settings(this);
    private final ChunkWorthTask chunkWorthTask = new ChunkWorthTask(this);
    private final WorthManager worthManager = new WorthManager(this);
    private final Set<PluginService> services = new HashSet<>(Arrays.asList(
            chunkWorthTask,
            new FactionsTopCommand(this),
            new WorldListener(this)
    ));

    private boolean active;
    private CraftbukkitHook craftbukkitHook;
    private EconomyHook economyHook;
    private FactionsHook factionsHook;

    public Settings getSettings() {
        return settings;
    }

    public ChunkWorthTask getChunkWorthTask() {
        return chunkWorthTask;
    }

    public WorthManager getWorthManager() {
        return worthManager;
    }

    public CraftbukkitHook getCraftbukkitHook() {
        return craftbukkitHook;
    }

    public EconomyHook getEconomyHook() {
        return economyHook;
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

        loadCraftbukkitHook();

        if (loadEconomyHook()) {
            services.add(economyHook);
        }

        services.add(factionsHook);
        loadSettings();
    }

    @Override
    public void onDisable() {
        services.forEach(PluginService::terminate);
        active = false;
    }

    private void loadCraftbukkitHook() {
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];

        if (version.compareTo("v1_7_R4") <= 0 && version.split("_")[1].length() == 1) {
            craftbukkitHook = new Craftbukkit17R4();
        } else if (version.equals("v1_8_R1")) {
            craftbukkitHook = new Craftbukkit18R1();
        } else if (version.equals("v1_8_R2")) {
            craftbukkitHook = new Craftbukkit18R2();
        } else {
            craftbukkitHook = new Craftbukkit18R3();
        }
    }

    private boolean loadEconomyHook() {
        Plugin essentials = getServer().getPluginManager().getPlugin("Essentials");
        if (essentials != null) {
            economyHook = new EssentialsEconomyHook(this, factionsHook);
            getLogger().info("Essentials found, using as economy backend.");
            return true;
        }

        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if (vault != null) {
            economyHook = new VaultEconomyHook(this, worthManager.getFactionIds());
            getLogger().info("Vault found, using as economy backend.");
            return true;
        }
        return false;
    }

    private boolean loadFactionsHook() {
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
    private void loadSettings() {
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
