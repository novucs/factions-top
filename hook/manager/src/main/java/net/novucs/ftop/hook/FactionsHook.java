package net.novucs.ftop.hook;

import org.bukkit.block.Block;

import java.util.UUID;

public interface FactionsHook {

    default UUID getFactionAt(Block block) {
        return getFactionAt(block.getWorld().getName(), block.getChunk().getX(), block.getChunk().getZ());
    }

    UUID getFactionAt(String worldName, int chunkX, int chunkZ);

    String getFactionName(UUID factionId);

}
