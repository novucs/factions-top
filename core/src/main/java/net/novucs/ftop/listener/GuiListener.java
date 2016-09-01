package net.novucs.ftop.listener;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import net.novucs.ftop.gui.GuiContext;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiListener implements Listener, PluginService {

    private final FactionsTopPlugin plugin;

    public GuiListener(FactionsTopPlugin plugin) {
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void registerClick(InventoryClickEvent event) {
        GuiContext context = plugin.getGuiManager().getContext(event.getClickedInventory());
        if (context == null) {
            return;
        }

        event.setCancelled(true);

        if (context.getSlots().size() >= event.getSlot()) {
            context.getSlots().get(event.getSlot()).handleClick(context);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void denyMovement(InventoryClickEvent event) {
        GuiContext context = plugin.getGuiManager().getContext(event.getInventory());
        if (context != null) {
            event.setCancelled(true);
        }
    }
}
