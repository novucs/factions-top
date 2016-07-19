package net.novucs.ftop.hook;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface CraftbukkitHook {

    EntityType getSpawnerType(ItemStack item);

}
