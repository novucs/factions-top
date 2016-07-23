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

    protected void modifyMaterials(Map<Material, Integer> materials, boolean remove) {
        for (Map.Entry<Material, Integer> material : materials.entrySet()) {
            int amount = this.materials.getOrDefault(material.getKey(), 0);
            this.materials.put(material.getKey(), amount + (remove ? -material.getValue() : material.getValue()));
        }
    }

    protected void modifySpawners(Map<EntityType, Integer> spawners, boolean remove) {
        for (Map.Entry<EntityType, Integer> spawner : spawners.entrySet()) {
            int amount = this.spawners.getOrDefault(spawner.getKey(), 0);
            this.spawners.put(spawner.getKey(), amount + (remove ? -spawner.getValue() : spawner.getValue()));
        }
    }

    protected void modifyWorth(Map<WorthType, Double> worth, boolean remove) {
        for (Map.Entry<WorthType, Double> entry : worth.entrySet()) {
            double amount = this.worth.getOrDefault(entry.getKey(), 0d);
            totalWorth += remove ? -entry.getValue() : entry.getValue();
            this.worth.put(entry.getKey(), amount + (remove ? -entry.getValue() : entry.getValue()));
        }
    }

    protected void addAll(ChunkWorth chunkWorth) {
        modifyMaterials(chunkWorth.getMaterials(), false);
        modifySpawners(chunkWorth.getSpawners(), false);
        modifyWorth(chunkWorth.getWorth(), false);
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
