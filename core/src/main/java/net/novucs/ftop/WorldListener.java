package net.novucs.ftop;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WorldListener implements Listener, PluginService {

    private final FactionsTopPlugin plugin;
    private final Map<BlockPos, Double> chestPrices = new HashMap<>();

    public WorldListener(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void terminate() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(BlockPlaceEvent event) {
        updateWorth(event.getBlock(), RecalculateReason.PLACE, false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(BlockBreakEvent event) {
        updateWorth(event.getBlock(), RecalculateReason.BREAK, true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(EntityExplodeEvent event) {
        event.blockList().forEach(block -> updateWorth(block, RecalculateReason.EXPLODE, true));
    }

    private void updateWorth(Block block, RecalculateReason reason, boolean negate) {
        // Do nothing if this area should not be calculated.
        String factionId = plugin.getFactionsHook().getFactionAt(block);
        if (plugin.getSettings().getIgnoredFactionIds().contains(factionId)) {
            return;
        }

        // Get the worth type and price of this event.
        double price;
        WorthType worthType;
        Map<Material, Integer> materials = new HashMap<>();
        Map<EntityType, Integer> spawners = new HashMap<>();

        switch (block.getType()) {
            case MOB_SPAWNER:
                worthType = WorthType.SPAWNER;
                EntityType spawnType = ((CreatureSpawner) block.getState()).getSpawnedType();
                price = plugin.getSettings().getSpawnerPrice(spawnType);
                spawners.put(spawnType, negate ? -1 : 1);
                break;
            default:
                worthType = WorthType.BLOCK;
                price = plugin.getSettings().getBlockPrice(block.getType());
                materials.put(block.getType(), negate ? -1 : 1);
                break;
        }

        // Add block price to the count.
        plugin.getWorthManager().add(block.getChunk(), reason, worthType, negate ? -price : price, materials, spawners);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void checkWorth(InventoryOpenEvent event) {
        // Do nothing if a player did not open the inventory.
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        // Set all default worth values for this chest.
        if (event.getInventory().getHolder() instanceof DoubleChest) {
            DoubleChest chest = (DoubleChest) event.getInventory().getHolder();
            checkWorth(chest.getLeftSide().getInventory());
            checkWorth(chest.getRightSide().getInventory());
        }

        if (event.getInventory().getHolder() instanceof Chest) {
            checkWorth(event.getInventory());
        }
    }

    private void checkWorth(Inventory inventory) {
        chestPrices.put(BlockPos.of(inventory.getLocation().getBlock()), getInventoryWorth(inventory));
    }

    private double getInventoryWorth(Inventory inventory) {
        double worth = 0;
        for (ItemStack item : inventory.getStorageContents()) {
            worth += getWorth(item);
        }
        return worth;
    }

    private double getWorth(ItemStack item) {
        if (item == null) return 0;

        if (item.getType() == Material.MOB_SPAWNER) {
            EntityType spawnerType = plugin.getCraftbukkitHook().getSpawnerType(item);
            return plugin.getSettings().getSpawnerPrice(spawnerType) * item.getAmount();
        }

        return plugin.getSettings().getBlockPrice(item.getType()) * item.getAmount();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(InventoryCloseEvent event) {
        // Do nothing if a player did not close the inventory.
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        // Get cached values from when chest was opened and add the difference
        // to the worth manager.
        if (event.getInventory().getHolder() instanceof DoubleChest) {
            DoubleChest chest = (DoubleChest) event.getInventory().getHolder();
            updateWorth(chest.getLeftSide().getInventory());
            updateWorth(chest.getRightSide().getInventory());
        }

        if (event.getInventory().getHolder() instanceof Chest) {
            updateWorth(event.getInventory());
        }
    }

    private void updateWorth(Inventory inventory) {
        BlockPos pos = BlockPos.of(inventory.getLocation().getBlock());
        Double worth = chestPrices.remove(pos);
        if (worth == null) return;

        // Update chest value, do not update block totals. Block total
        // operations can be costly, better keep this to the automatic
        // check.
        worth = getInventoryWorth(inventory) - worth;
        plugin.getWorthManager().add(inventory.getLocation().getChunk(), RecalculateReason.CHEST, WorthType.CHEST,
                worth, Collections.emptyMap(), Collections.emptyMap());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(FactionClaimEvent event) {
        String newFactionId = event.getFactionId();
        event.getClaims().asMap().forEach((oldFactionId, claims) -> {
            plugin.getWorthManager().update(newFactionId, claims, false);
            plugin.getWorthManager().update(oldFactionId, claims, true);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void removeFaction(FactionDisbandEvent event) {
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
        double balance = plugin.getEconomyHook().getBalance(event.getPlayer());
        plugin.getWorthManager().add(event.getFactionId(), WorthType.PLAYER_BALANCE, -balance);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void recalculate(ChunkUnloadEvent event) {
        plugin.getWorthManager().recalculate(event.getChunk(), RecalculateReason.UNLOAD);
    }
}
