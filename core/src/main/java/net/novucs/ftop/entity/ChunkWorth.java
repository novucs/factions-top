package net.novucs.ftop.entity;

import net.novucs.ftop.WorthType;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.Map;

public class ChunkWorth {

    private final Map<WorthType, Double> worth;
    private Map<Material, Integer> materials;
    private Map<EntityType, Integer> spawners;
    private long nextRecalculation;

    public ChunkWorth() {
        this(new EnumMap<>(WorthType.class), new EnumMap<>(Material.class), new EnumMap<>(EntityType.class));
    }

    public ChunkWorth(Map<WorthType, Double> worth, Map<Material, Integer> materials, Map<EntityType, Integer> spawners) {
        this.worth = worth;
        this.materials = materials;
        this.spawners = spawners;
    }

    public double getWorth(WorthType worthType) {
        return worth.getOrDefault(worthType, 0d);
    }

    public void setWorth(WorthType worthType, double worth) {
        if (!WorthType.isPlaced(worthType)) {
            throw new IllegalArgumentException("Liquid worth cannot be associated with chunks!");
        }

        worth = Math.max(0, worth);
        this.worth.put(worthType, worth);
    }

    public Map<WorthType, Double> getWorth() {
        return worth;
    }

    public Map<Material, Integer> getMaterials() {
        return materials;
    }

    public Map<EntityType, Integer> getSpawners() {
        return spawners;
    }

    public void setMaterials(Map<Material, Integer> materials) {
        this.materials = materials;
    }

    public void setSpawners(Map<EntityType, Integer> spawners) {
        this.spawners = spawners;
    }

    public void addMaterials(Map<Material, Integer> materials) {
        for (Map.Entry<Material, Integer> material : materials.entrySet()) {
            int amount = Math.max(0, this.materials.getOrDefault(material.getKey(), 0) + material.getValue());
            this.materials.put(material.getKey(), amount);
        }
    }

    public void addSpawners(Map<EntityType, Integer> spawners) {
        for (Map.Entry<EntityType, Integer> spawner : spawners.entrySet()) {
            int amount = Math.max(0, this.spawners.getOrDefault(spawner.getKey(), 0) + spawner.getValue());
            this.spawners.put(spawner.getKey(), amount);
        }
    }

    public void addWorth(WorthType worthType, double worth) {
        setWorth(worthType, getWorth(worthType) + worth);
    }

    public long getNextRecalculation() {
        return nextRecalculation;
    }

    public void setNextRecalculation(long nextRecalculation) {
        this.nextRecalculation = nextRecalculation;
    }

    @Override
    public String toString() {
        return "ChunkWorth{" +
                "worth=" + worth +
                ", materials=" + materials +
                ", spawners=" + spawners +
                ", nextRecalculation=" + nextRecalculation +
                '}';
    }
}
