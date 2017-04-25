package net.novucs.ftop.hook;

import com.songoda.epicspawners.API.EpicSpawnersAPI;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Spawners.SpawnerChangeEvent;
import net.novucs.ftop.hook.event.SpawnerMultiplierChangeEvent;
import org.bukkit.ChatColor;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class EpicSpawnersHook implements SpawnerStackerHook, Listener {

    private final Plugin plugin;
    private final CraftbukkitHook craftbukkitHook;
    private EpicSpawnersAPI api;

    public EpicSpawnersHook(Plugin plugin, CraftbukkitHook craftbukkitHook) {
        this.plugin = plugin;
        this.craftbukkitHook = craftbukkitHook;
    }

    public void initialize() {
        Plugin epicSpawnersPlugin = plugin.getServer().getPluginManager().getPlugin("EpicSpawners");

        if (epicSpawnersPlugin instanceof EpicSpawners) {
            api = ((EpicSpawners) epicSpawnersPlugin).getApi();
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @Override
    public EntityType getSpawnedType(ItemStack spawner) {
        if (api == null) {
            return craftbukkitHook.getSpawnerType(spawner);
        }

        try {
            return api.getType(spawner);
        } catch (IllegalArgumentException ex) {
            return craftbukkitHook.getSpawnerType(spawner);
        }
    }

    @Override
    public int getStackSize(ItemStack spawner) {
        if (api == null || !spawner.hasItemMeta()) {
            return 1;
        }

        String[] args = spawner.getItemMeta().getDisplayName().split(" ");
        String lastArg = ChatColor.stripColor(args[args.length - 1]);

        if (lastArg.length() > 0) {
            lastArg = lastArg.substring(0, lastArg.length() - 1);
        }

        try {
            return Integer.parseInt(lastArg);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    @Override
    public int getStackSize(CreatureSpawner spawner) {
        if (api == null) {
            return 1;
        }

        return api.getSpawnerMultiplier(spawner.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(SpawnerChangeEvent event) {
        CreatureSpawner spawner = (CreatureSpawner) event.getSpawner().getState();
        int oldMultiplier = event.getOldMulti();
        int newMultiplier = event.getCurrentMulti();
        SpawnerMultiplierChangeEvent event1 = new SpawnerMultiplierChangeEvent(spawner, oldMultiplier, newMultiplier);
        plugin.getServer().getPluginManager().callEvent(event1);
    }
}
