package net.novucs.ftop;

import net.novucs.ftop.hook.event.*;
import org.bukkit.block.*;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.block.BlockFace.*;

public class WorldListener implements Listener, PluginService {

    private static final BlockFace[] CHEST_JOIN_FACES = {NORTH, EAST, SOUTH, WEST};
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

        switch (block.getType()) {
            case MOB_SPAWNER:
                worthType = WorthType.SPAWNER;
                price = plugin.getSettings().getSpawnerPrice(((CreatureSpawner) block.getState()).getSpawnedType());
                break;
            default:
                worthType = WorthType.BLOCK;
                price = plugin.getSettings().getBlockPrice(block.getType());
                break;
        }

        // Add block price to the count.
        plugin.getWorthManager().add(block.getChunk(), reason, worthType, negate ? -price : price);
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
            worth += plugin.getWorthManager().getWorth(item);
        }
        return worth;
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

        // Update chest value.
        worth = getInventoryWorth(inventory) - worth;
        plugin.getWorthManager().add(inventory.getLocation().getChunk(), RecalculateReason.CHEST, WorthType.CHEST, worth);
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
}
