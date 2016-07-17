package net.novucs.ftop;

import java.util.*;

public final class WorthManager {

    private final FactionsTopPlugin plugin;
    private final Map<ChunkPos, ChunkWorth> chunks = new HashMap<>();
    private final Map<String, FactionWorth> factions = new HashMap<>();
    private final List<FactionWorth> orderedFactions = new LinkedList<>();

    public WorthManager(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

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

    private ChunkWorth getChunkWorth(ChunkPos pos) {
        return chunks.compute(pos, (k, v) -> {
            if (v == null) {
                // TODO: Add chunk to calculation queue.
                v = new ChunkWorth(this);
            }
            return v;
        });
    }

    private FactionWorth getFactionWorth(ChunkPos pos) {
        return getFactionWorth(plugin.getFactionsHook().getFactionAt(pos));
    }

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

    public void updatePlaced(ChunkPos pos, double placed) {
        // Do nothing if faction worth is null.
        FactionWorth factionWorth = getFactionWorth(pos);
        if (factionWorth == null) return;

        // Update all stats with the new chunk data.
        ChunkWorth chunkWorth = getChunkWorth(pos);
        double oldWorth = chunkWorth.getWorth(WorthType.PLACED);
        chunkWorth.setWorth(WorthType.PLACED, placed);
        factionWorth.addWorth(WorthType.PLACED, placed - oldWorth);
        sort(factionWorth);
    }

    public void addPlaced(ChunkPos pos, double placed) {
        // Do nothing if faction worth is null.
        FactionWorth factionWorth = getFactionWorth(pos);
        if (factionWorth == null) return;

        // Update all stats with the new chunk data.
        ChunkWorth chunkWorth = getChunkWorth(pos);
        double oldWorth = chunkWorth.getWorth(WorthType.PLACED);
        chunkWorth.setWorth(WorthType.PLACED, placed + oldWorth);
        factionWorth.addWorth(WorthType.PLACED, placed);
        sort(factionWorth);
    }

    public void add(String factionId, Collection<ChunkPos> claims) {
        // Do nothing if faction worth is null.
        FactionWorth factionWorth = getFactionWorth(factionId);
        if (factionWorth == null) return;

        // Add all placed and chest worth of each claim to the faction.
        for (ChunkPos pos : claims) {
            ChunkWorth chunkWorth = getChunkWorth(pos);
            factionWorth.addWorth(WorthType.PLACED, chunkWorth.getWorth(WorthType.PLACED));
            factionWorth.addWorth(WorthType.CHESTS, chunkWorth.getWorth(WorthType.CHESTS));
        }

        // Adjust factions location.
        sort(factionWorth);
    }

    public void remove(String factionId, Collection<ChunkPos> claims) {
        // Do nothing if faction worth is null.
        FactionWorth factionWorth = getFactionWorth(factionId);
        if (factionWorth == null) return;

        // Take all placed and chest worth of each claim to the faction.
        for (ChunkPos pos : claims) {
            ChunkWorth chunkWorth = getChunkWorth(pos);
            factionWorth.addWorth(WorthType.PLACED, -chunkWorth.getWorth(WorthType.PLACED));
            factionWorth.addWorth(WorthType.CHESTS, -chunkWorth.getWorth(WorthType.CHESTS));
        }

        // Adjust factions location.
        sort(factionWorth);
    }

    public void remove(String factionId) {
        FactionWorth factionWorth = factions.remove(factionId);
        orderedFactions.remove(factionWorth);
    }

    public List<FactionWorth> getOrderedFactions() {
        return orderedFactions;
    }
}
