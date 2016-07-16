package net.novucs.ftop;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.UUID;

public class WorldListener implements Listener {

    private final FactionsTopPlugin plugin;

    public WorldListener(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(BlockPlaceEvent event) {
        updateWorth(event, false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(BlockBreakEvent event) {
        updateWorth(event, true);
    }

    private void updateWorth(BlockEvent event, boolean negate) {
        // Do nothing if this area should not be calculated.
        ChunkPos chunk = ChunkPos.of(event.getBlock());
        UUID factionId = plugin.getFactionsHook().getFactionAt(chunk);
        if (factionId == null || !plugin.getSettings().isEnabled(WorthType.PLACED)) {
            return;
        }

        // Do nothing if price of the placed block is nothing.
        double price = plugin.getSettings().getBlockPrice(event.getBlock().getType());
        if (price == 0) return;

        // Add block price to the count.
        plugin.getWorthManager().addPlaced(chunk, negate ? -price : price);
    }
}
