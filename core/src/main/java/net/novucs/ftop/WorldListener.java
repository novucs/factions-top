package net.novucs.ftop;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
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
        updateWorth(event, false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateWorth(BlockBreakEvent event) {
        updateWorth(event, true);
    }

    private void updateWorth(BlockEvent event, boolean negate) {
        // Do nothing if this area should not be calculated.
        String factionId = plugin.getFactionsHook().getFactionAt(event.getBlock());
        if (factionId == null || !plugin.getSettings().isEnabled(WorthType.PLACED)) {
            return;
        }

        // Do nothing if price of the placed block is nothing.
        double price = plugin.getSettings().getBlockPrice(event.getBlock().getType());
        if (price == 0) return;

        // Add block price to the count.
        plugin.getWorthManager().addPlaced(ChunkPos.of(event.getBlock()), negate ? -price : price);
    }
}
