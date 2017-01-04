package net.novucs.ftop.task;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.WorthType;
import net.novucs.ftop.entity.BlockPos;
import net.novucs.ftop.entity.ChunkPos;
import net.novucs.ftop.entity.ChunkWorth;
import net.novucs.ftop.entity.FactionWorth;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class PersistenceTask extends Thread {

    private final FactionsTopPlugin plugin;
    private final Map<ChunkPos, ChunkWorth> worthCache = new HashMap<>();
    private final BlockingQueue<Map.Entry<ChunkPos, ChunkWorth>> chunkQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<FactionWorth> factionQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> factionDeletionQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Map.Entry<BlockPos, Integer>> signCreationQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<BlockPos> signDeletionQueue = new LinkedBlockingQueue<>();

    public void queue(ChunkPos pos, ChunkWorth chunkWorth) {
        ChunkWorth cachedWorth = worthCache.get(pos);

        if (cachedWorth == null || !chunkWorthEquals(chunkWorth, cachedWorth)) {
            chunkQueue.add(new AbstractMap.SimpleImmutableEntry<>(pos, chunkWorth));
            worthCache.put(pos, clone(chunkWorth));
        }
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

    public void queueDeletedFaction(String factionId) {
        if (plugin.getSettings().isDatabasePersistFactions()) {
            factionDeletionQueue.add(factionId);
        }
    }

    public void queueCreatedSign(BlockPos block, int rank) {
        signCreationQueue.add(new AbstractMap.SimpleImmutableEntry<>(block, rank));
    }

    public void queueDeletedSign(BlockPos block) {
        signDeletionQueue.add(block);
    }

    public PersistenceTask(FactionsTopPlugin plugin) {
        super("factions-top-persistence-task");
        this.plugin = plugin;
    }

    @Override
    public void start() {
        loadChunkCache();
        super.start();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            if (plugin.getSettings().isDatabasePersistFactions()) {
                deleteInvalidFactions();
            }

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

    private void deleteInvalidFactions() {
        Set<String> factionIds = plugin.getFactionsHook().getFactionIds();

        for (String factionId : plugin.getDatabaseManager().getIdentityCache().getFactionIds()) {
            if (!factionIds.contains(factionId)) {
                factionDeletionQueue.add(factionId);
            }
        }
    }

    private void persist() {
        List<Map.Entry<ChunkPos, ChunkWorth>> chunks = new LinkedList<>();
        chunkQueue.drainTo(chunks);

        List<FactionWorth> factions = new LinkedList<>();
        factionQueue.drainTo(factions);

        Set<String> deletedFactions = new HashSet<>();
        factionDeletionQueue.drainTo(deletedFactions);

        List<Map.Entry<BlockPos, Integer>> createdSigns = new LinkedList<>();
        signCreationQueue.drainTo(createdSigns);

        List<BlockPos> deletedSigns = new LinkedList<>();
        signDeletionQueue.drainTo(deletedSigns);

        try {
            plugin.getDatabaseManager().save(chunks, factions, deletedFactions, createdSigns, deletedSigns);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to persist queued updates to the database");
            plugin.getLogger().log(Level.SEVERE, "Are the database credentials in the config correct?");
            plugin.getLogger().log(Level.SEVERE, "Stack trace: ", e);
        }
    }

    private void loadChunkCache() {
        plugin.getWorthManager().getChunks().forEach((chunkPos, chunkWorth) ->
                worthCache.put(chunkPos, clone(chunkWorth)));
    }

    private ChunkWorth clone(ChunkWorth chunkWorth) {
        Map<WorthType, Double> worth = new EnumMap<>(WorthType.class);
        worth.putAll(chunkWorth.getWorth());

        Map<Material, Integer> materials = new EnumMap<>(Material.class);
        materials.putAll(chunkWorth.getMaterials());

        Map<EntityType, Integer> spawners = new EnumMap<>(EntityType.class);
        spawners.putAll(chunkWorth.getSpawners());

        return new ChunkWorth(worth, materials, spawners);
    }

    private boolean chunkWorthEquals(ChunkWorth chunkWorth1, ChunkWorth chunkWorth2) {
        return Objects.equals(chunkWorth1.getWorth(), chunkWorth2.getWorth()) &&
                Objects.equals(chunkWorth1.getMaterials(), chunkWorth2.getMaterials()) &&
                Objects.equals(chunkWorth1.getSpawners(), chunkWorth2.getSpawners());
    }
}
