package net.novucs.ftop;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Map;
import java.util.Objects;

public class ChestWorth {

    private final double totalWorth;
    private final Map<Material, Integer> materials;
    private final Map<EntityType, Integer> spawners;

    public ChestWorth(double totalWorth, Map<Material, Integer> materials, Map<EntityType, Integer> spawners) {
        this.totalWorth = totalWorth;
        this.materials = materials;
        this.spawners = spawners;
    }

    public double getTotalWorth() {
        return totalWorth;
    }

    public Map<Material, Integer> getMaterials() {
        return materials;
    }

    public Map<EntityType, Integer> getSpawners() {
        return spawners;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChestWorth that = (ChestWorth) o;
        return Double.compare(that.totalWorth, totalWorth) == 0 &&
                Objects.equals(materials, that.materials) &&
                Objects.equals(spawners, that.spawners);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalWorth, materials, spawners);
    }

    @Override
    public String toString() {
        return "ChestWorth{" +
                "totalWorth=" + totalWorth +
                ", materials=" + materials +
                ", spawners=" + spawners +
                '}';
    }
}
