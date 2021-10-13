package net.novucs.ftop.hook;

import com.songoda.epicspawners.api.EpicSpawners;
import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.events.SpawnerChangeEvent;
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
    private EpicSpawners api;

    public EpicSpawnersHook(Plugin plugin, CraftbukkitHook craftbukkitHook) {
        this.plugin = plugin;
        this.craftbukkitHook = craftbukkitHook;
    }

    public void initialize() {
        api = EpicSpawnersAPI.getImplementation();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public EntityType getSpawnedType(ItemStack spawner) {
        if (api == null) {
            return craftbukkitHook.getSpawnerType(spawner);
        }

        try {
            return api.getSpawnerDataFromItem(spawner).getEntities().get(0);
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

        return api.getSpawnerManager().getSpawnerFromWorld(spawner.getLocation()).getSpawnerStacks().size();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(SpawnerChangeEvent event) {
        CreatureSpawner spawner = event.getSpawner().getCreatureSpawner();
        int oldMultiplier = event.getOldStackSize();
        int newMultiplier = event.getStackSize();
        SpawnerMultiplierChangeEvent event1 = new SpawnerMultiplierChangeEvent(spawner, oldMultiplier, newMultiplier);

        if (plugin.getServer().isPrimaryThread()) {
            plugin.getServer().getPluginManager().callEvent(event1);
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () ->
                plugin.getServer().getPluginManager().callEvent(event1));
    }
}
