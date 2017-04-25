package net.novucs.ftop.hook;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class VanillaSpawnerStackerHook implements SpawnerStackerHook {

    private final CraftbukkitHook craftbukkitHook;

    public VanillaSpawnerStackerHook(CraftbukkitHook craftbukkitHook) {
        this.craftbukkitHook = craftbukkitHook;
    }

    @Override
    public void initialize() {
    }

    @Override
    public EntityType getSpawnedType(ItemStack spawner) {
        return craftbukkitHook.getSpawnerType(spawner);
    }

    @Override
    public int getStackSize(ItemStack spawner) {
        return 1;
    }

    @Override
    public int getStackSize(CreatureSpawner spawner) {
        return 1;
    }
}
