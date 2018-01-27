package net.novucs.ftop.hook;

import com.vk2gpz.mergedspawner.MergedSpawner;
import net.novucs.ftop.hook.event.SpawnerMultiplierChangeEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MergedSpawnerHook implements SpawnerStackerHook, Listener{
    private final Plugin plugin;
    private final CraftbukkitHook craftbukkitHook;
    private boolean enabled;

    public MergedSpawnerHook(Plugin plugin, CraftbukkitHook craftbukkitHook) {
        this.plugin = plugin;
        this.craftbukkitHook = craftbukkitHook;
    }

    @Override
    public void initialize() {
        this.enabled = plugin.getServer().getPluginManager().isPluginEnabled("MergedSpawner");

        if(this.enabled) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @Override
    public EntityType getSpawnedType(ItemStack spawner) {
        // MergedSpawner doesn't alter the way ItemStacks work.
        return craftbukkitHook.getSpawnerType(spawner);
    }

    @Override
    public int getStackSize(ItemStack spawner) {
        // MergedSpawner doesn't alter the way ItemStacks work, so return 1 for a spawner, 0 otherwise.
        return spawner.getType() == Material.MOB_SPAWNER ? 1 : 0;
    }

    @Override
    public int getStackSize(CreatureSpawner spawner) {
        if(!enabled) {
            return 1;
        }

        return MergedSpawner.getCountFor(spawner.getBlock());
    }

    /**
     * Handles an explosion event containing a blocklist by collecting all spawners and running
     * {@link net.novucs.ftop.hook.MergedSpawnerHook#checkSpawners(List)} when appropriate.
     *
     * @param blockList List containing all exploded list, as provided by explosion event.
     */
    private void handleExplosionEvent(List<Block> blockList) {
        ArrayList<Block> spawners = new ArrayList<>();

        for(Block exploded : blockList) {
            if(exploded.getType() == Material.MOB_SPAWNER) {
                spawners.add(exploded);
            }
        }

        if(!spawners.isEmpty()) {
            checkSpawners(spawners);
        }
    }

    /**
     * Calculates the multiplier of each spawner and stores it in a HashMap.
     *
     * @param spawners The spawners to calculate.
     * @return {@code HashMap} containing the multiplier of each spawner.
     */
    private HashMap<Block, Integer> calcMultipliers(List<Block> spawners) {
        HashMap<Block, Integer> multipliers = new HashMap<>();

        for(Block spawner : spawners) {
            multipliers.put(spawner, MergedSpawner.getCountFor(spawner));
        }

        return multipliers;
    }

    /**
     * Checks for each spawner in the list whether its multiplier has changed one tick later.
     * If so it will fire a {@code SpawnerMultiplierChangeEvent}.
     *
     * @param spawners The spawners to check.
     */
    private void checkSpawners(List<Block> spawners) {
        // First calculate all spawners' multipliers, before any event handling by the plugin has been done.
        HashMap<Block, Integer> oldMultipliers = calcMultipliers(spawners);

        // Wait one tick, so the event is handled properly by MergedSpawner.
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Map.Entry<Block, Integer> entry : calcMultipliers(spawners).entrySet()) {
                    // For every spawner calculate the multiplier again, and compare it with its previous value.
                    Block spawner = entry.getKey();
                    Integer newMultiplier = entry.getValue();
                    Integer oldMultipler = oldMultipliers.get(spawner);

                    if(newMultiplier > oldMultipler) {
                        SpawnerMultiplierChangeEvent event = new SpawnerMultiplierChangeEvent(
                                ((CreatureSpawner) spawner.getState()), oldMultipler, newMultiplier);

                        // runTaskLater always runs on primary thread, so we can safely call the event here.
                        plugin.getServer().getPluginManager().callEvent(event);
                    }
                }
            }
        }.runTaskLater(plugin, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.getBlock().getType() != Material.MOB_SPAWNER) {
            return;
        }

        ArrayList<Block> spawners = new ArrayList<>();

        // Check all neighbors, and if it is a spawner add it to the list.
        for(BlockFace face : BlockFace.values()) {
            Block neighbor = event.getBlock().getRelative(face);

            if(neighbor.getType() == Material.MOB_SPAWNER) {
                spawners.add(neighbor);
            }
        }

        checkSpawners(spawners);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getBlock().getType() != Material.MOB_SPAWNER) {
            return;
        }

        checkSpawners(Collections.singletonList(event.getBlock()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockExplosion(BlockExplodeEvent event) {
        handleExplosionEvent(event.blockList());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityExplosion(EntityExplodeEvent event) {
        handleExplosionEvent(event.blockList());
    }
}
