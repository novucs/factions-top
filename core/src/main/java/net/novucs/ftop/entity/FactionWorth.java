package net.novucs.ftop.entity;

import net.novucs.ftop.WorthType;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class FactionWorth implements Comparable<FactionWorth> {

    private final String factionId;
    private final Map<WorthType, Double> worth = new EnumMap<>(WorthType.class);
    private final Map<Material, Integer> materials = new EnumMap<>(Material.class);
    private final Map<EntityType, Integer> spawners = new EnumMap<>(EntityType.class);
    private String name;
    private double totalWorth = 0;
    private double penaltyWorth = 0;
    private int totalSpawners = 0;

    public FactionWorth(String factionId, String name) {
        this.factionId = factionId;
        this.name = name;
    }

    public String getFactionId() {
        return factionId;
    }

    public double getWorth(WorthType worthType) {
        return worth.getOrDefault(worthType, 0d);
    }

    public Map<WorthType, Double> getWorth() {
        return worth;
    }

    public Map<Material, Integer> getMaterials() {
        return Collections.unmodifiableMap(materials);
    }

    public Map<EntityType, Integer> getSpawners() {
        return Collections.unmodifiableMap(spawners);
    }

    public int getTotalSpawnerCount() {
        return totalSpawners;
    }

    public String getName() {
        return name;
    }

    public double getTotalWorth() {
        return Math.max(0, totalWorth - penaltyWorth);
    }

    public double getPenaltyWorth() {
        return penaltyWorth;
    }

    public void setPenaltyWorth(double penaltyWorth) {
        this.penaltyWorth = penaltyWorth;
    }

    public void addPenaltyWorth(double penaltyWorth) {
        this.penaltyWorth += penaltyWorth;
    }

    private void setWorth(WorthType worthType, double worth) {
        worth = Math.max(0, worth);
        Double prev = this.worth.put(worthType, worth);
        totalWorth += worth - (prev == null ? 0 : prev);
    }

    public void addWorth(WorthType worthType, double worth) {
        setWorth(worthType, getWorth(worthType) + worth);
    }

    public void addMaterials(Map<Material, Integer> materials) {
        add(materials, this.materials);
    }

    public void removeMaterials(Map<Material, Integer> materials) {
        remove(materials, this.materials);
    }

    public void addSpawners(Map<EntityType, Integer> spawners) {
        spawners.values().forEach(count -> totalSpawners += count);
        add(spawners, this.spawners);
    }

    public void removeSpawners(Map<EntityType, Integer> spawners) {
        spawners.values().forEach(count -> totalSpawners -= count);
        remove(spawners, this.spawners);
    }

    private void addWorth(Map<WorthType, Double> worth) {
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

    public void addAll(ChunkWorth chunkWorth) {
        addMaterials(chunkWorth.getMaterials());
        addSpawners(chunkWorth.getSpawners());
        addWorth(chunkWorth.getWorth());
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(FactionWorth o) {
        return Double.compare(o.getTotalWorth(), getTotalWorth());
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
