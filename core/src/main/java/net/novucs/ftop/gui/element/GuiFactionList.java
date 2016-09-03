package net.novucs.ftop.gui.element;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.Settings;
import net.novucs.ftop.entity.FactionWorth;
import net.novucs.ftop.gui.GuiContext;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.DecimalFormat;
import java.util.*;

import static net.novucs.ftop.util.StringUtils.*;

public class GuiFactionList implements GuiElement {

    private final int factionCount;
    private final boolean fillEmpty;
    private final String text;
    private final List<String> lore;

    private GuiFactionList(int factionCount, boolean fillEmpty, String text, List<String> lore) {
        this.factionCount = factionCount;
        this.fillEmpty = fillEmpty;
        this.text = text;
        this.lore = lore;
    }

    @Override
    public void render(GuiContext context) {
        FactionsTopPlugin plugin = context.getPlugin();
        DecimalFormat currencyFormat = plugin.getSettings().getCurrencyFormat();
        DecimalFormat countFormat = plugin.getSettings().getCountFormat();

        int counter = 0;
        while (counter++ < factionCount) {
            if (context.getInventory().getSize() <= context.getSlot()) {
                break;
            }

            context.getSlots().add(this);

            if (!context.getWorthIterator().hasNext()) {
                if (!fillEmpty) {
                    break;
                }
                context.getAndIncrementSlot();
                continue;
            }

            FactionWorth worth = context.getWorthIterator().next();
            Map<String, String> placeholders = new HashMap<>(context.getPlaceholders());
            placeholders.put("{rank}", Integer.toString(context.getAndIncrementRank()));
            placeholders.put("{relcolor}", "" + ChatColor.COLOR_CHAR +
                    getRelationColor(plugin, context.getPlayer(), worth.getFactionId()).getChar());
            placeholders.put("{faction}", worth.getName());
            placeholders.put("{worth:total}", currencyFormat.format(worth.getTotalWorth()));
            placeholders.put("{count:total:spawner}", countFormat.format(worth.getTotalSpawnerCount()));

            String owner = plugin.getFactionsHook().getOwnerName(worth.getFactionId());
            ItemStack item = getItem(worth, placeholders, plugin.getSettings(), owner);
            context.getInventory().setItem(context.getAndIncrementSlot(), item);
        }
    }

    @Override
    public void handleClick(GuiContext context) {
    }

    public int getFactionCount() {
        return factionCount;
    }

    private ItemStack getItem(FactionWorth worth, Map<String, String> placeholders, Settings settings, String owner) {
        String text = insertPlaceholders(settings, worth, replace(this.text, placeholders));
        List<String> lore = insertPlaceholders(settings, worth, replace(this.lore, placeholders));

        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayName(text);
        meta.setLore(lore);
        meta.setOwner(owner);

        item.setItemMeta(meta);

        return item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuiFactionList that = (GuiFactionList) o;
        return factionCount == that.factionCount &&
                fillEmpty == that.fillEmpty &&
                Objects.equals(text, that.text) &&
                Objects.equals(lore, that.lore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(factionCount, fillEmpty, text, lore);
    }

    @Override
    public String toString() {
        return "GuiFactionList{" +
                "factionCount=" + factionCount +
                ", fillEmpty=" + fillEmpty +
                ", text='" + text + '\'' +
                ", lore=" + lore +
                '}';
    }

    public static class Builder {
        private int factionCount = 0;
        private boolean fillEmpty = true;
        private String text = "";
        private List<String> lore = new ArrayList<>();

        public void factionCount(int factionCount) {
            this.factionCount = factionCount;
        }

        public void fillEmpty(boolean fillEmpty) {
            this.fillEmpty = fillEmpty;
        }

        public void text(String text) {
            this.text = text;
        }

        public void lore(List<String> lore) {
            this.lore = lore;
        }

        public GuiFactionList build() {
            return new GuiFactionList(factionCount, fillEmpty, text, lore);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Builder builder = (Builder) o;
            return factionCount == builder.factionCount &&
                    fillEmpty == builder.fillEmpty &&
                    Objects.equals(text, builder.text) &&
                    Objects.equals(lore, builder.lore);
        }

        @Override
        public int hashCode() {
            return Objects.hash(factionCount, fillEmpty, text, lore);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "factionCount=" + factionCount +
                    ", fillEmpty=" + fillEmpty +
                    ", text='" + text + '\'' +
                    ", lore=" + lore +
                    '}';
        }
    }
}
