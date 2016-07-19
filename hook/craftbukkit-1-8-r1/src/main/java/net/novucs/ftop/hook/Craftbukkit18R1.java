package net.novucs.ftop.hook;

import net.minecraft.server.v1_8_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class Craftbukkit18R1 implements CraftbukkitHook {

    @Override
    public EntityType getSpawnerType(ItemStack item) {
        CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        NBTTagCompound tag = CraftItemStack.asNMSCopy(craftStack).getTag();

        if (tag == null || !tag.hasKey("BlockEntityTag")) {
            throw new IllegalArgumentException();
        }

        return EntityType.fromName(tag.getCompound("BlockEntityTag").getString("EntityId"));
    }
}
