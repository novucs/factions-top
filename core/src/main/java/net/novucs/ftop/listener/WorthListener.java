package net.novucs.ftop.listener;

import com.google.common.collect.ImmutableMap;
import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import net.novucs.ftop.RecalculateReason;
import net.novucs.ftop.WorthType;
import net.novucs.ftop.delayedspawners.DelayedSpawners;
import net.novucs.ftop.entity.BlockPos;
import net.novucs.ftop.entity.ChestWorth;
import net.novucs.ftop.hook.event.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorthListener extends BukkitRunnable implements Listener, PluginService {

    private final FactionsTopPlugin plugin;
    private final Map<BlockPos, ChestWorth> chests = new HashMap<>();
    private final Set<String> recentDisbands = new HashSet<>();

    public WorthListener(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        runTaskTimer(plugin, 1, 1);
    }

    @Override
    public void terminate() {
        HandlerList.unregisterAll(this);
        cancel();
    }

    @Override
    public void run() {
        recentDisbands.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.MOB_SPAWNER) {
            plugin.getDelayedSpawners().queue((CreatureSpawner) event.getBlock().getState());
            return;
        }

        updateWorth(event.getBlock(), RecalculateReason.PLACE, false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.MOB_SPAWNER) {
            CreatureSpawner spawner = (CreatureSpawner) event.getBlock().getState();

            if (plugin.getDelayedSpawners().isDelayed(spawner)) {
                plugin.getDelayedSpawners().removeFromQueue(spawner);
                return;
            }
        }

        updateWorth(event.getBlock(), RecalculateReason.BREAK, true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(EntityExplodeEvent event) {
        event.blockList().forEach(block -> {
            if (block.getType() == Material.MOB_SPAWNER) {
                CreatureSpawner spawner = (CreatureSpawner) block.getState();

                if (plugin.getDelayedSpawners().isDelayed(spawner)) {
                    plugin.getDelayedSpawners().removeFromQueue(spawner);
                    return;
                }
            }

            updateWorth(block, RecalculateReason.EXPLODE, true);
        });
    }

    private void updateWorth(Block block, RecalculateReason reason, boolean negate) {
        // Do nothing if this area should not be calculated.
        String factionId = plugin.getFactionsHook().getFactionAt(block);
        if (plugin.getSettings().getIgnoredFactionIds().contains(factionId)) {
            return;
        }

        // Get the worth type and price of this event.
        int multiplier = negate ? -1 : 1;
        double price = multiplier * plugin.getSettings().getBlockPrice(block.getType());
        WorthType worthType = WorthType.BLOCK;
        Map<Material, Integer> materials = new HashMap<>();
        Map<EntityType, Integer> spawners = new HashMap<>();

        plugin.getWorthManager().add(block.getChunk(), reason, worthType, price,
                ImmutableMap.of(block.getType(), multiplier), spawners);

        switch (block.getType()) {
            case MOB_SPAWNER:
                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                worthType = WorthType.SPAWNER;
                EntityType spawnedType = spawner.getSpawnedType();
                multiplier *= plugin.getSpawnerStackerHook().getStackSize(spawner);
                price = multiplier * plugin.getSettings().getSpawnerPrice(spawnedType);
                spawners.put(spawnedType, multiplier);
                break;
            case CHEST:
            case TRAPPED_CHEST:
                if (plugin.getSettings().isDisableChestEvents()) {
                    return;
                }

                worthType = WorthType.CHEST;
                Chest chest = (Chest) block.getState();
                ChestWorth chestWorth = negate ? getWorthNegative(chest.getBlockInventory()) : getWorth(chest.getBlockInventory());
                price = chestWorth.getTotalWorth();
                materials.putAll(chestWorth.getMaterials());
                spawners.putAll(chestWorth.getSpawners());
                break;
            default:
                return;
        }

        // Add block price to the count.
        plugin.getWorthManager().add(block.getChunk(), reason, worthType, price, materials, spawners);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void checkWorth(InventoryOpenEvent event) {
        // Do nothing if a player did not open the inventory or if chest events
        // are disabled.
        if (!(event.getPlayer() instanceof Player) || plugin.getSettings().isDisableChestEvents()) {
            return;
        }

        Inventory inventory = event.getInventory();

        // Set all default worth values for this chest.
        if (inventory.getHolder() instanceof DoubleChest) {
            DoubleChest chest = (DoubleChest) inventory.getHolder();
            checkWorth((Chest) chest.getLeftSide());
            checkWorth((Chest) chest.getRightSide());
        }

        if (inventory.getHolder() instanceof Chest) {
            checkWorth((Chest) inventory.getHolder());
        }
    }

    private void checkWorth(Chest chest) {
        chests.put(BlockPos.of(chest.getBlock()), getWorth(chest.getBlockInventory()));
    }

    private ChestWorth getWorth(Inventory inventory) {
        double worth = 0;
        Map<Material, Integer> materials = new HashMap<>();
        Map<EntityType, Integer> spawners = new HashMap<>();

        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;

            if (item.getType() == Material.MOB_SPAWNER) {
                int stackSize = plugin.getSpawnerStackerHook().getStackSize(item);
                EntityType spawnerType = plugin.getSpawnerStackerHook().getSpawnedType(item);
                worth += plugin.getSettings().getSpawnerPrice(spawnerType) * item.getAmount() * stackSize;

                int count = spawners.getOrDefault(spawnerType, 0);
                spawners.put(spawnerType, count + (item.getAmount() * stackSize));
                continue;
            }

            worth += plugin.getSettings().getBlockPrice(item.getType()) * item.getAmount();
            int count = materials.getOrDefault(item.getType(), 0);
            materials.put(item.getType(), count + item.getAmount());
        }

        return new ChestWorth(worth, materials, spawners);
    }

    private ChestWorth getWorthNegative(Inventory inventory) {
        double worth = 0;
        Map<Material, Integer> materials = new HashMap<>();
        Map<EntityType, Integer> spawners = new HashMap<>();

        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;

            if (item.getType() == Material.MOB_SPAWNER) {
                int stackSize = plugin.getSpawnerStackerHook().getStackSize(item);
                EntityType spawnerType = plugin.getSpawnerStackerHook().getSpawnedType(item);
                worth -= plugin.getSettings().getSpawnerPrice(spawnerType) * item.getAmount() * stackSize;

                int count = spawners.getOrDefault(spawnerType, 0);
                spawners.put(spawnerType, count - (item.getAmount() + stackSize));
                continue;
            }

            worth -= plugin.getSettings().getBlockPrice(item.getType()) * item.getAmount();
            int count = materials.getOrDefault(item.getType(), 0);
            materials.put(item.getType(), count - item.getAmount());
        }

        return new ChestWorth(worth, materials, spawners);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(InventoryCloseEvent event) {
        // Do nothing if a player did not close the inventory or if chest
        // events are disabled.
        if (!(event.getPlayer() instanceof Player) || plugin.getSettings().isDisableChestEvents()) {
            return;
        }

        // Get cached values from when chest was opened and add the difference
        // to the worth manager.
        if (event.getInventory().getHolder() instanceof DoubleChest) {
            DoubleChest chest = (DoubleChest) event.getInventory().getHolder();
            updateWorth((Chest) chest.getLeftSide());
            updateWorth((Chest) chest.getRightSide());
        }

        if (event.getInventory().getHolder() instanceof Chest) {
            updateWorth((Chest) event.getInventory().getHolder());
        }
    }

    private void updateWorth(Chest chest) {
        if (chest == null) return;
        BlockPos pos = BlockPos.of(chest.getBlock());
        ChestWorth worth = chests.remove(pos);
        if (worth == null) return;

        worth = getDifference(worth, getWorth(chest.getBlockInventory()));

        plugin.getWorthManager().add(chest.getChunk(), RecalculateReason.CHEST, WorthType.CHEST,
                worth.getTotalWorth(), worth.getMaterials(), worth.getSpawners());
    }

    private ChestWorth getDifference(ChestWorth first, ChestWorth second) {
        double worth = second.getTotalWorth() - first.getTotalWorth();
        Map<Material, Integer> materials = getDifference(first.getMaterials(), second.getMaterials());
        Map<EntityType, Integer> spawners = getDifference(first.getSpawners(), second.getSpawners());
        return new ChestWorth(worth, materials, spawners);
    }

    private <T> Map<T, Integer> getDifference(Map<T, Integer> first, Map<T, Integer> second) {
        Map<T, Integer> target = new HashMap<>();

        for (Map.Entry<T, Integer> entry : first.entrySet()) {
            int difference = second.getOrDefault(entry.getKey(), 0) - entry.getValue();
            target.put(entry.getKey(), difference);
        }

        for (Map.Entry<T, Integer> entry : second.entrySet()) {
            if (target.containsKey(entry.getKey())) continue;
            target.put(entry.getKey(), entry.getValue());
        }

        return target;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(FactionClaimEvent event) {
        String newFactionId = event.getFactionId();
        event.getClaims().asMap().forEach((oldFactionId, claims) -> {
            if (!oldFactionId.equals(newFactionId)) {
                plugin.getWorthManager().update(newFactionId, claims, false);
                plugin.getWorthManager().update(oldFactionId, claims, true);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void removeFaction(FactionDisbandEvent event) {
        recentDisbands.add(event.getFactionId());
        plugin.getWorthManager().remove(event.getFactionId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void renameFaction(FactionRenameEvent event) {
        plugin.getWorthManager().rename(event.getFactionId(), event.getNewName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(FactionEconomyEvent event) {
        plugin.getWorthManager().add(event.getFactionId(), WorthType.FACTION_BALANCE, event.getNewBalance() - event.getOldBalance());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(PlayerEconomyEvent event) {
        String factionId = plugin.getFactionsHook().getFaction(event.getPlayer());
        plugin.getWorthManager().add(factionId, WorthType.PLAYER_BALANCE, event.getNewBalance() - event.getOldBalance());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(FactionJoinEvent event) {
        double balance = plugin.getEconomyHook().getBalance(event.getPlayer());
        plugin.getWorthManager().add(event.getFactionId(), WorthType.PLAYER_BALANCE, balance);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(FactionLeaveEvent event) {
        // Do nothing if the faction was disbanded within this tick.
        if (recentDisbands.contains(event.getFactionId())) {
            return;
        }

        double balance = plugin.getEconomyHook().getBalance(event.getPlayer());
        plugin.getWorthManager().add(event.getFactionId(), WorthType.PLAYER_BALANCE, -balance);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void recalculate(ChunkUnloadEvent event) {
        plugin.getWorthManager().recalculate(event.getChunk(), RecalculateReason.UNLOAD);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(SpawnerMultiplierChangeEvent event) {
        // Do nothing if this area should not be calculated.
        Block block = event.getBlock();
        String factionId = plugin.getFactionsHook().getFactionAt(block);
        if (plugin.getSettings().getIgnoredFactionIds().contains(factionId)) {
            return;
        }

        // Get the worth type and price of this event.
        int difference = event.getNewMultiplier() - event.getOldMultiplier();
        WorthType worthType = WorthType.SPAWNER;
        Map<Material, Integer> materials = new HashMap<>();
        Map<EntityType, Integer> spawners = new HashMap<>();

        EntityType spawnType = ((CreatureSpawner) block.getState()).getSpawnedType();
        double price = difference * plugin.getSettings().getSpawnerPrice(spawnType);
        spawners.put(spawnType, difference);

        RecalculateReason reason = difference > 0 ? RecalculateReason.PLACE : RecalculateReason.BREAK;

        // Add block price to the count.
        plugin.getWorthManager().add(block.getChunk(), reason, worthType, price, materials, spawners);
    }
}
