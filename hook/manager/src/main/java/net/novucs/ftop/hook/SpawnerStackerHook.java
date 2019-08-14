package net.novucs.ftop.hook;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface SpawnerStackerHook {

    void initialize();

    EntityType getSpawnedType(ItemStack spawner);

    int getStackSize(ItemStack spawner);

    int getStackSize(CreatureSpawner spawner);

}
