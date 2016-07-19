package net.novucs.ftop.hook;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class Craftbukkit18R3 implements CraftbukkitHook {

    @Override
    public EntityType getSpawnerType(ItemStack item) {
        BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
        CreatureSpawner bs = (CreatureSpawner) bsm.getBlockState();
        return bs.getSpawnedType();
    }
}
