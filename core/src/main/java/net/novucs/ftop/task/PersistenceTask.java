package net.novucs.ftop.task;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.entity.BlockPos;
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
    private final BlockingQueue<Map.Entry<BlockPos, Integer>> signCreationQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<BlockPos> signDeletionQueue = new LinkedBlockingQueue<>();

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

    public void queueCreatedSign(BlockPos block, int rank) {
        signCreationQueue.add(new AbstractMap.SimpleImmutableEntry<BlockPos, Integer>(block, rank));
    }

    public void queueDeletedSign(BlockPos block) {
        signDeletionQueue.add(block);
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
                interrupt();
                break;
            }
        }

        persist();
    }

    private void persist() {
        List<Map.Entry<ChunkPos, ChunkWorth>> chunks = new LinkedList<>();
        chunkQueue.drainTo(chunks);

        List<FactionWorth> factions = new LinkedList<>();
        factionQueue.drainTo(factions);

        List<Map.Entry<BlockPos, Integer>> createdSigns = new LinkedList<>();
        signCreationQueue.drainTo(createdSigns);

        List<BlockPos> deletedSigns = new LinkedList<>();
        signDeletionQueue.drainTo(deletedSigns);

        try {
            plugin.getDatabaseManager().save(chunks, factions, createdSigns, deletedSigns);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to persist chunk data", e);
        }
    }
}
