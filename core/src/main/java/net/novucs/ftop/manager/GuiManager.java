package net.novucs.ftop.manager;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.entity.FactionWorth;
import net.novucs.ftop.gui.GuiContext;
import net.novucs.ftop.util.SplaySet;
import net.novucs.ftop.util.StringUtils;
import net.novucs.ftop.util.TreeIterator;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class GuiManager {

    private static final int MAX_TITLE_SIZE = 32;
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

    public void closeInventories() {
        for (Inventory inventory : this.inventories.keySet()) {
            new ArrayList<>(inventory.getViewers()).forEach(HumanEntity::closeInventory);
        }
    }

    public void sendGui(Player player, int page) {
        int entries = plugin.getSettings().getGuiLayout().getFactionsPerPage();
        SplaySet<FactionWorth> factions = plugin.getWorthManager().getOrderedFactions();
        int maxPage = Math.max((int) Math.ceil((double) factions.size() / entries), 1);
        page = Math.max(1, Math.min(maxPage, page));

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{page:back}", String.valueOf(page - 1));
        placeholders.put("{page:this}", String.valueOf(page));
        placeholders.put("{page:next}", String.valueOf(page + 1));
        placeholders.put("{page:last}", String.valueOf(maxPage));

        int spacer = entries * (page - 1);
        TreeIterator<FactionWorth> it = factions.iterator(spacer);

        int lines = plugin.getSettings().getGuiLineCount() * 9;
        String name = StringUtils.replace(plugin.getSettings().getGuiInventoryName(), placeholders);
        if (name.length() > MAX_TITLE_SIZE) {
            name = name.substring(0, MAX_TITLE_SIZE);
        }
        Inventory inventory = plugin.getServer().createInventory(null, lines, name);

        GuiContext context = new GuiContext(plugin, player, inventory, maxPage, page, it, placeholders);
        context.setCurrentRank(spacer + 1);

        plugin.getSettings().getGuiLayout().render(context);
        inventories.put(inventory, context);
        player.openInventory(inventory);
    }
}
