package net.novucs.ftop;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChunkWorthTask extends Thread implements PluginService {

    private final FactionsTopPlugin plugin;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final BlockingQueue<ChunkSnapshot> queue = new LinkedBlockingQueue<>();

    public ChunkWorthTask(FactionsTopPlugin plugin) {
        super("Chunk Worth Task");
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        running.set(true);
        start();
    }

    @Override
    public void terminate() {
        running.set(false);
    }

    public void queue(ChunkSnapshot snapshot) {
        queue.add(snapshot);
    }

    @Override
    public void run() {
        while (running.get()) {
            Set<ChunkSnapshot> toProcess = new HashSet<>();
            queue.drainTo(toProcess);

            Map<ChunkPos, Double> batch = new HashMap<>();
            for (ChunkSnapshot snapshot : toProcess) {
                batch.put(ChunkPos.of(snapshot), getWorth(snapshot));
            }

            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getWorthManager().updatePlaced(batch));
        }
    }

    private double getWorth(ChunkSnapshot snapshot) {
        double worth = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < snapshot.getHighestBlockYAt(z, x); y++) {
                    Material material = Material.getMaterial(snapshot.getBlockTypeId(x, y, z));
                    if (material == null) continue;

                    if (material == Material.MOB_SPAWNER) {
                        EntityType entityType = EntityType.fromId(snapshot.getBlockData(x, y, z));
                        worth += plugin.getSettings().getSpawnerPrice(entityType);
                        continue;
                    }

                    worth += plugin.getSettings().getBlockPrice(material);
                }
            }
        }
        return worth;
    }
}
