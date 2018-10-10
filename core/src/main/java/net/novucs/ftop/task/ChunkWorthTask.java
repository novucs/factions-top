package net.novucs.ftop.task;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.WorthType;
import net.novucs.ftop.entity.ChunkPos;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChunkWorthTask extends Thread {

    private final FactionsTopPlugin plugin;
    private final BlockingQueue<ChunkSnapshot> queue = new LinkedBlockingQueue<>();

    public ChunkWorthTask(FactionsTopPlugin plugin) {
        super("factions-top-chunk-task");
        this.plugin = plugin;
    }

    public void queue(ChunkSnapshot snapshot) {
        queue.add(snapshot);
    }

    public int getQueueSize() {
        return queue.size();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            ChunkSnapshot snapshot;
            try {
                snapshot = queue.take();
            } catch (InterruptedException e) {
                interrupt();
                break;
            }

            ChunkPos pos = ChunkPos.of(snapshot);
            double worth = 0;
            double blockPrice;
            Map<Material, Integer> materials = new EnumMap<>(Material.class);

            for (int y = 0; y < 256; y++) {
                // ChunkSnapshot#getHighestBlockYAt(x, y) for whatever reason
                // provides us with a half complete chunk in Spigot v1.10.x. So
                // we're testing if the chunk section is empty instead.
                if (snapshot.isSectionEmpty(y >> 4)) {
                    y += 15;
                    continue;
                }

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        // TODO: Add backwards compatibility for:
                        // TODO: Material.getMaterial(snapshot.getBlockTypeId(x, y, z));
                        Material material = snapshot.getBlockType(x, y, z);
                        if (material == null) {
                            continue;
                        }

                        blockPrice = plugin.getSettings().getBlockPrice(material);
                        worth += blockPrice;

                        if (blockPrice != 0) {
                            int count = materials.getOrDefault(material, 0);
                            materials.put(material, count + 1);
                        }
                    }
                }
            }

            final double worthFinal = worth;
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getWorthManager().set(pos, WorthType.BLOCK, worthFinal);
                plugin.getWorthManager().setMaterials(pos, materials);
            });
        }
    }
}
