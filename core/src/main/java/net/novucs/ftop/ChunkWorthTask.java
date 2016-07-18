package net.novucs.ftop;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChunkWorthTask extends Thread implements PluginService {

    private final FactionsTopPlugin plugin;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final BlockingQueue<ChunkSnapshot> queue = new LinkedBlockingQueue<>();

    public ChunkWorthTask(FactionsTopPlugin plugin) {
        super("ChunkWorthTask");
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
            ChunkSnapshot snapshot;
            try {
                snapshot = queue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException("An exception occurred while attempting to take from the chunk snapshot queue.", e);
            }

            ChunkPos pos = ChunkPos.of(snapshot);
            double worth = getWorth(snapshot);
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getWorthManager().set(pos, WorthType.BLOCK, worth));
        }
    }

    private double getWorth(ChunkSnapshot snapshot) {
        double worth = 0;

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
                    Material material = Material.getMaterial(snapshot.getBlockTypeId(x, y, z));
                    if (material != null) {
                        worth += plugin.getSettings().getBlockPrice(material);
                    }
                }
            }
        }

        return worth;
    }
}
