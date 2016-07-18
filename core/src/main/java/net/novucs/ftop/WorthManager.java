package net.novucs.ftop;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Chunk;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;

import java.util.*;

public final class WorthManager {

    private final FactionsTopPlugin plugin;
    private final Map<ChunkPos, ChunkWorth> chunks = new HashMap<>();
    private final Map<String, FactionWorth> factions = new HashMap<>();
    private final List<FactionWorth> orderedFactions = new LinkedList<>();
    private final Table<ChunkPos, WorthType, Double> recalculateQueue = HashBasedTable.create();

    public WorthManager(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns an unmodifiable view of the ordered factions.
     *
     * @return the ordered factions.
     */
    public List<FactionWorth> getOrderedFactions() {
        return Collections.unmodifiableList(orderedFactions);
    }

    /**
     * Adds a faction worth profile to the ordered factions list.
     *
     * @param factionWorth the profile to add.
     */
    private void add(FactionWorth factionWorth) {
        // Start from end of the list.
        ListIterator<FactionWorth> it = orderedFactions.listIterator(orderedFactions.size());

        // Locate where to insert the new element.
        while (it.hasPrevious()) {
            if (it.previous().compareTo(factionWorth) >= 0) {
                it.next();
                break;
            }
        }

        // Insert ordered value.
        it.add(factionWorth);
    }

    /**
     * Updates the position in which the faction worth profile is ordered in
     * the top factions list, depending on the profiles total worth.
     *
     * @param factionWorth the profile to sort.
     */
    private void sort(FactionWorth factionWorth) {
        // Remove the current value.
        ListIterator<FactionWorth> it = orderedFactions.listIterator();
        while (it.hasNext()) {
            if (it.next() == factionWorth) {
                it.remove();
                break;
            }
        }

        // Locate where the value should be ordered.
        while (it.hasPrevious()) {
            if (it.previous().compareTo(factionWorth) >= 0) {
                break;
            }
        }

        while (it.hasNext()) {
            if (it.next().compareTo(factionWorth) <= 0) {
                it.previous();
                break;
            }
        }

        // Add back to list with the correct position.
        it.add(factionWorth);
    }

    /**
     * Gets the chunk worth profile for a specific chunk.
     *
     * @param pos the position of the chunk.
     * @return the chunk profile.
     */
    private ChunkWorth getChunkWorth(ChunkPos pos) {
        return chunks.compute(pos, (k, v) -> {
            if (v == null) {
                v = new ChunkWorth();
            }
            return v;
        });
    }

    /**
     * Gets a faction worth profile by a chunk.
     *
     * @param pos the chunk.
     * @return the worth of a faction who has claimed this chunk, or null if no
     * valid faction owns this land.
     */
    private FactionWorth getFactionWorth(ChunkPos pos) {
        return getFactionWorth(plugin.getFactionsHook().getFactionAt(pos));
    }

    /**
     * Gets the faction worth profile by a faction ID.
     *
     * @param factionId the faction ID.
     * @return the faction worth profile or null of not a valid faction.
     */
    private FactionWorth getFactionWorth(String factionId) {
        // No faction worth is associated with ignored faction IDs.
        if (plugin.getSettings().getIgnoredFactionIds().contains(factionId)) {
            return null;
        }

        return factions.compute(factionId, (k, v) -> {
            if (v == null) {
                v = new FactionWorth(k, plugin.getFactionsHook().getFactionName(k));
                add(v);
            }
            return v;
        });
    }

    /**
     * Sets a chunks worth.
     *
     * @param pos       the chunk position.
     * @param worthType the worth type.
     * @param worth     the worth value.
     */
    protected void set(ChunkPos pos, WorthType worthType, double worth) {
        // Do nothing if faction worth is null.
        FactionWorth factionWorth = getFactionWorth(pos);
        if (factionWorth == null) return;

        // Update all stats with the new chunk data.
        ChunkWorth chunkWorth = getChunkWorth(pos);
        double oldWorth = chunkWorth.getWorth(worthType);
        chunkWorth.setWorth(worthType, worth);
        factionWorth.addWorth(worthType, worth - oldWorth);

        // Adjust faction worth position.
        sort(factionWorth);

        // If this position was added to the recalculate queue, add all queued
        // updates while the chunk was recalculated and set the next time to
        // recalculate the chunk.
        if (recalculateQueue.contains(pos, worthType)) {
            double queuedWorth = recalculateQueue.remove(pos, worthType);
            chunkWorth.addWorth(worthType, queuedWorth);
            factionWorth.addWorth(worthType, queuedWorth);
            chunkWorth.setNextRecalculation(plugin.getSettings().getChunkRecalculateMillis() + System.currentTimeMillis());
        }
    }

    /**
     * Adds worth to a chunk.
     *
     * @param chunk     the chunk.
     * @param reason    the reason.
     * @param worthType the worth type.
     * @param worth     the worth value.
     */
    protected void add(Chunk chunk, RecalculateReason reason, WorthType worthType, double worth) {
        // Do nothing if faction worth is null.
        ChunkPos pos = ChunkPos.of(chunk);
        FactionWorth factionWorth = getFactionWorth(pos);
        if (factionWorth == null) return;

        // Update all stats with the new chunk data.
        ChunkWorth chunkWorth = getChunkWorth(pos);
        chunkWorth.addWorth(worthType, worth);
        factionWorth.addWorth(worthType, worth);

        // Adjust faction worth position.
        sort(factionWorth);

        // Add this worth to the recalculation queue if the chunk is being
        // recalculated.
        if (recalculateQueue.contains(pos, worthType)) {
            double prev = recalculateQueue.get(pos, worthType);
            recalculateQueue.put(pos, worthType, worth + prev);
            return;
        }

        // Schedule chunk for recalculation.
        recalculate(chunkWorth, pos, chunk, reason);
    }

    /**
     * Attempts to schedule a chunk for recalculation if the chunk is allowed
     * to be recalculated at this time.
     *
     * @param chunkWorth the worth associated with this chunk.
     * @param pos        the chunk position.
     * @param chunk      the chunk.
     * @param reason     the reason for recalculating.
     */
    private void recalculate(ChunkWorth chunkWorth, ChunkPos pos, Chunk chunk, RecalculateReason reason) {
        // Do not recalculate the chunk value if not within the recalculation period.
        if (chunkWorth.getNextRecalculation() >= System.currentTimeMillis() &&
                !plugin.getSettings().isBypassRecalculateDelay(reason)) {
            return;
        }

        // Update the chunk spawner worth on the main thread, unfortunately
        // there is no better method of doing this.
        set(pos, WorthType.SPAWNER, getSpawnerWorth(chunk));

        // Add chunk position to the recalculation queue with a dummy value.
        recalculateQueue.put(pos, WorthType.BLOCK, 0d);

        // Next recalculation is scheduled once the chunk worth is re-set.
        chunkWorth.setNextRecalculation(Long.MAX_VALUE);

        // Schedule this chunk to be recalculated on a separate thread.
        // Occasionally block updates are not updated in the chunk on the
        // same tick, getting the chunk snapshot in the next tick fixes
        // this issue.
        plugin.getServer().getScheduler().runTask(plugin, () ->
                plugin.getChunkWorthTask().queue(chunk.getChunkSnapshot()));
    }

    /**
     * Gets the total worth of all spawners within a chunk.
     *
     * @param chunk the chunk to calculate.
     * @return the total value of all spawners in this chunk.
     */
    private double getSpawnerWorth(Chunk chunk) {
        double worth = 0;
        for (BlockState blockState : chunk.getTileEntities()) {
            if (blockState instanceof CreatureSpawner) {
                worth += plugin.getSettings().getSpawnerPrice(((CreatureSpawner) blockState).getSpawnedType());
            }
        }
        return worth;
    }

    /**
     * Updates the factions worth once the faction has claimed or unclaimed territory.
     *
     * @param factionId the faction ID.
     * @param claims    the claims affected in this transaction.
     * @param unclaimed true if the claims were unclaimed.
     */
    protected void update(String factionId, Collection<ChunkPos> claims, boolean unclaimed) {
        // Do nothing if faction worth is null.
        FactionWorth factionWorth = getFactionWorth(factionId);
        if (factionWorth == null) return;

        // Add all placed and chest worth of each claim to the faction.
        for (ChunkPos pos : claims) {
            ChunkWorth chunkWorth = getChunkWorth(pos);
            for (WorthType worthType : WorthType.getPlaced()) {
                double worth = chunkWorth.getWorth(worthType);
                factionWorth.addWorth(worthType, unclaimed ? -worth : worth);
            }

            // Schedule chunk for recalculation.
            recalculate(chunkWorth, pos, pos.getChunk(plugin.getServer()), RecalculateReason.CLAIM);
        }

        // Adjust faction worth position.
        sort(factionWorth);
    }

    /**
     * Renames a listed faction.
     *
     * @param factionId the faction ID.
     * @param newName   the new faction name.
     */
    protected void rename(String factionId, String newName) {
        FactionWorth factionWorth = factions.getOrDefault(factionId, null);
        if (factionWorth != null) {
            factionWorth.setName(newName);
        }
    }

    /**
     * Removes a faction from the list.
     *
     * @param factionId the ID of the faction to remove.
     */
    protected void remove(String factionId) {
        FactionWorth factionWorth = factions.remove(factionId);

        // Optimised removal, factions lower down the list are more likely to be disbanded.
        ListIterator<FactionWorth> it = orderedFactions.listIterator(orderedFactions.size());
        while (it.hasPrevious()) {
            if (it.previous() == factionWorth) {
                it.remove();
                break;
            }
        }
    }
}
