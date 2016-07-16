package net.novucs.ftop;

import java.util.EnumMap;
import java.util.Objects;

public class ChunkWorth {

    private final WorthManager manager;
    private final EnumMap<WorthType, Double> worth;
    private double totalWorth = 0;

    public ChunkWorth(WorthManager manager) {
        this(manager, new EnumMap<>(WorthType.class));
    }

    public ChunkWorth(WorthManager manager, EnumMap<WorthType, Double> worth) {
        this.manager = manager;
        this.worth = worth;
        worth.forEach((k, v) -> this.totalWorth += k == WorthType.LIQUID ? 0d : v);
    }

    public double getWorth(WorthType worthType) {
        return worth.getOrDefault(worthType, 0d);
    }

    public void setWorth(WorthType worthType, double worth) {
        if (worthType == WorthType.LIQUID) {
            throw new IllegalArgumentException("Liquid worth cannot be associated with chunks!");
        }

        double prev = this.worth.put(worthType, worth);
        totalWorth += worth - prev;
    }

    public void addWorth(WorthType worthType, double worth) {
        setWorth(worthType, getWorth(worthType) + worth);
    }

    public double getTotalWorth() {
        return totalWorth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkWorth that = (ChunkWorth) o;
        return Double.compare(that.totalWorth, totalWorth) == 0 &&
                Objects.equals(manager, that.manager) &&
                Objects.equals(worth, that.worth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(manager, worth, totalWorth);
    }

    @Override
    public String toString() {
        return "ChunkWorth{" +
                "manager=" + manager +
                ", worth=" + worth +
                ", totalWorth=" + totalWorth +
                '}';
    }
}
