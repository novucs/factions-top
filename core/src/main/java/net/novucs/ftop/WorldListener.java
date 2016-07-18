package net.novucs.ftop;

import net.novucs.ftop.hook.FactionClaimEvent;
import net.novucs.ftop.hook.FactionDisbandEvent;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class WorldListener implements Listener, PluginService {

    private final FactionsTopPlugin plugin;

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

        // Do nothing if price of the placed block is nothing or if this worth type is not enabled.
        if (price == 0 || !plugin.getSettings().isEnabled(worthType)) {
            return;
        }

        // Add block price to the count.
        plugin.getWorthManager().add(block.getChunk(), reason, worthType, negate ? -price : price);
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
}
