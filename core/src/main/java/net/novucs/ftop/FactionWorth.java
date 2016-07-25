package net.novucs.ftop;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class FactionWorth implements Comparable<FactionWorth> {

    private final String factionId;
    private final Map<WorthType, Double> worth;
    private final Map<Material, Integer> materials;
    private final Map<EntityType, Integer> spawners;
    private String name;
    private double totalWorth = 0;

    public FactionWorth(String factionId, String name) {
        this(factionId, name, new EnumMap<>(WorthType.class), new EnumMap<>(Material.class), new EnumMap<>(EntityType.class));
    }

    public FactionWorth(String factionId, String name, Map<WorthType, Double> worth, Map<Material, Integer> materials, Map<EntityType, Integer> spawners) {
        this.factionId = factionId;
        this.name = name;
        this.worth = worth;
        this.materials = materials;
        this.spawners = spawners;
        worth.values().forEach(v -> totalWorth += v);
    }

    public String getFactionId() {
        return factionId;
    }

    public double getWorth(WorthType worthType) {
        return worth.getOrDefault(worthType, 0d);
    }

    public Map<Material, Integer> getMaterials() {
        return Collections.unmodifiableMap(materials);
    }

    public Map<EntityType, Integer> getSpawners() {
        return Collections.unmodifiableMap(spawners);
    }

    protected void setWorth(WorthType worthType, double worth) {
        worth = Math.max(0, worth);
        Double prev = this.worth.put(worthType, worth);
        totalWorth += worth - (prev == null ? 0 : prev);
    }

    protected void addWorth(WorthType worthType, double worth) {
        setWorth(worthType, getWorth(worthType) + worth);
    }

    protected void addMaterials(Map<Material, Integer> materials) {
        add(materials, this.materials);
    }

    protected void removeMaterials(Map<Material, Integer> materials) {
        remove(materials, this.materials);
    }

    protected void addSpawners(Map<EntityType, Integer> spawners) {
        add(spawners, this.spawners);
    }

    protected void removeSpawners(Map<EntityType, Integer> spawners) {
        remove(spawners, this.spawners);
    }

    protected void addWorth(Map<WorthType, Double> worth) {
        for (Map.Entry<WorthType, Double> entry : worth.entrySet()) {
            double amount = this.worth.getOrDefault(entry.getKey(), 0d);
            totalWorth += entry.getValue();
            this.worth.put(entry.getKey(), amount + entry.getValue());
        }
    }

    private <T> void add(Map<T, Integer> modifier, Map<T, Integer> modified) {
        for (Map.Entry<T, Integer> entry : modifier.entrySet()) {
            int amount = Math.max(0, modified.getOrDefault(entry.getKey(), 0) + entry.getValue());
            modified.put(entry.getKey(), amount);
        }
    }

    private <T> void remove(Map<T, Integer> modifier, Map<T, Integer> modified) {
        for (Map.Entry<T, Integer> entry : modifier.entrySet()) {
            int amount = Math.max(0, modified.getOrDefault(entry.getKey(), 0) - entry.getValue());
            modified.put(entry.getKey(), amount);
        }
    }

    protected void addAll(ChunkWorth chunkWorth) {
        addMaterials(chunkWorth.getMaterials());
        addSpawners(chunkWorth.getSpawners());
        addWorth(chunkWorth.getWorth());
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public double getTotalWorth() {
        return totalWorth;
    }

    @Override
    public int compareTo(FactionWorth o) {
        return Double.compare(o.totalWorth, totalWorth);
    }

    @Override
    public String toString() {
        return "FactionWorth{" +
                "factionId='" + factionId + '\'' +
                ", worth=" + worth +
                ", materials=" + materials +
                ", spawners=" + spawners +
                ", name='" + name + '\'' +
                ", totalWorth=" + totalWorth +
                '}';
    }
}
