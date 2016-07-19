package net.novucs.ftop.hook;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class Craftbukkit17R4 implements CraftbukkitHook {

    @Override
    public EntityType getSpawnerType(ItemStack item) {
        return EntityType.fromId((int) item.getData().getData());
    }
}
