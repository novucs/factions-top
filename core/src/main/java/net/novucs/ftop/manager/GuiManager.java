package net.novucs.ftop.manager;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.entity.FactionWorth;
import net.novucs.ftop.gui.GuiContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.WeakHashMap;

public class GuiManager {

    private final FactionsTopPlugin plugin;
    private final Map<Inventory, GuiContext> inventories = new WeakHashMap<>();

    public GuiManager(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    public GuiContext getContext(Inventory inventory) {
        return inventories.get(inventory);
    }

    public void unloadGui(Inventory inventory) {
        inventories.remove(inventory);
    }

    public void sendGui(Player player, int page) {
        int entries = plugin.getSettings().getGuiLayout().getFactionsPerPage();
        List<FactionWorth> factions = plugin.getWorthManager().getOrderedFactions();
        int maxPage = Math.max((int) Math.ceil((double) factions.size() / entries), 1);
        page = Math.max(1, Math.min(maxPage, page));

        int spacer = entries * --page;
        ListIterator<FactionWorth> it = factions.listIterator(spacer);

        int lines = plugin.getSettings().getGuiLineCount() * 9;
        String name = plugin.getSettings().getGuiInventoryName();
        Inventory inventory = plugin.getServer().createInventory(null, lines, name);

        GuiContext context = new GuiContext(plugin, player, inventory, maxPage, page, it);
        context.setCurrentRank(spacer);

        plugin.getSettings().getGuiLayout().render(context);
        inventories.put(inventory, context);
        player.openInventory(inventory);
    }
}
