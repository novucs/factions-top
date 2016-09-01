package net.novucs.ftop.gui.element.button;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GuiButtonContent {

    private final String text;
    private final List<String> lore;
    private final Material material;
    private final byte data;
    private ItemStack item;

    private GuiButtonContent(String text, List<String> lore, Material material, byte data) {
        this.text = text;
        this.lore = lore;
        this.material = material;
        this.data = data;
    }

    public String getText() {
        return text;
    }

    public List<String> getLore() {
        return lore;
    }

    public Material getMaterial() {
        return material;
    }

    public byte getData() {
        return data;
    }

    public ItemStack getItem() {
        if (item == null) {
            item = new ItemStack(material, 1, data);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(text);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuiButtonContent that = (GuiButtonContent) o;
        return data == that.data &&
                Objects.equals(text, that.text) &&
                Objects.equals(lore, that.lore) &&
                material == that.material;
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, lore, material, data);
    }

    @Override
    public String toString() {
        return "GuiButtonContent{" +
                "text='" + text + '\'' +
                ", lore=" + lore +
                ", material=" + material +
                ", data=" + data +
                '}';
    }

    public static class Builder {

        private String text;
        private List<String> lore = new ArrayList<>();
        private Material material = Material.AIR;
        private byte data = 0;

        public void text(String text) {
            this.text = text;
        }

        public void lore(List<String> lore) {
            this.lore = lore;
        }

        public void material(Material material) {
            this.material = material;
        }

        public void data(byte data) {
            this.data = data;
        }

        public GuiButtonContent build() {
            return new GuiButtonContent(text, lore, material, data);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Builder builder = (Builder) o;
            return data == builder.data &&
                    Objects.equals(text, builder.text) &&
                    Objects.equals(lore, builder.lore) &&
                    material == builder.material;
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, lore, material, data);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "text='" + text + '\'' +
                    ", lore=" + lore +
                    ", material=" + material +
                    ", data=" + data +
                    '}';
        }
    }
}
