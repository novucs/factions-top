package net.novucs.ftop.task;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.entity.ChunkPos;
import net.novucs.ftop.entity.ChunkWorth;
import net.novucs.ftop.entity.FactionWorth;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class PersistenceTask extends Thread {

    private final FactionsTopPlugin plugin;
    private final BlockingQueue<Map.Entry<ChunkPos, ChunkWorth>> chunkQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<FactionWorth> factionQueue = new LinkedBlockingQueue<>();

    public void queue(ChunkPos pos, ChunkWorth chunkWorth) {
        chunkQueue.add(new AbstractMap.SimpleImmutableEntry<>(pos, chunkWorth));
    }

    public void queue(FactionWorth factionWorth) {
        if (plugin.getSettings().isDatabasePersistFactions()) {
            factionQueue.add(factionWorth);
        }
    }

    public void queue(Collection<FactionWorth> factions) {
        if (plugin.getSettings().isDatabasePersistFactions()) {
            factionQueue.addAll(factions);
        }
    }

    public PersistenceTask(FactionsTopPlugin plugin) {
        super("factions-top-persistence-task");
        this.plugin = plugin;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            persist();

            try {
                sleep(plugin.getSettings().getDatabasePersistInterval());
            } catch (InterruptedException e) {
                persist();
                interrupt();
                break;
            }
        }
    }

    private void persist() {
        persistChunks();
        persistFactions();
    }

    private void persistChunks() {
        List<Map.Entry<ChunkPos, ChunkWorth>> chunks = new LinkedList<>();
        chunkQueue.drainTo(chunks);

        try {
            plugin.getDatabaseManager().save(chunks);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to persist chunk data", e);
        }
    }

    private void persistFactions() {
        List<FactionWorth> factions = new LinkedList<>();
        factionQueue.drainTo(factions);
        // TODO: Save factions to database.
    }
}
