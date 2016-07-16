package net.novucs.ftop;

import java.util.EnumMap;
import java.util.Objects;
import java.util.UUID;

public class FactionWorth implements Comparable<FactionWorth> {

    private final UUID uniqueId;
    private final EnumMap<WorthType, Double> worth;
    private String name;
    private double totalWorth = 0;

    public FactionWorth(UUID uniqueId, String name) {
        this(uniqueId, new EnumMap<>(WorthType.class), name);
    }

    public FactionWorth(UUID uniqueId, EnumMap<WorthType, Double> worth, String name) {
        this.uniqueId = uniqueId;
        this.worth = worth;
        this.name = name;
        worth.values().forEach(v -> totalWorth += v);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public double getWorth(WorthType worthType) {
        return worth.getOrDefault(worthType, 0d);
    }

    public void setWorth(WorthType worthType, double worth) {
        double prev = this.worth.put(worthType, worth);
        totalWorth += worth - prev;
    }

    public void addWorth(WorthType worthType, double worth) {
        setWorth(worthType, getWorth(worthType) + worth);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotalWorth() {
        return totalWorth;
    }

    @Override
    public int compareTo(FactionWorth o) {
        return Double.compare(totalWorth, o.totalWorth);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactionWorth that = (FactionWorth) o;
        return Double.compare(that.totalWorth, totalWorth) == 0 &&
                Objects.equals(uniqueId, that.uniqueId) &&
                Objects.equals(worth, that.worth) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, worth, name, totalWorth);
    }

    @Override
    public String toString() {
        return "FactionWorth{" +
                "uniqueId=" + uniqueId +
                ", worth=" + worth +
                ", name='" + name + '\'' +
                ", totalWorth=" + totalWorth +
                '}';
    }
}
