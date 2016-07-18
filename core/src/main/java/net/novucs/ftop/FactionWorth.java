package net.novucs.ftop;

import java.util.EnumMap;
import java.util.Objects;

public class FactionWorth implements Comparable<FactionWorth> {

    private final String factionId;
    private final EnumMap<WorthType, Double> worth;
    private String name;
    private double totalWorth = 0;

    public FactionWorth(String factionId, String name) {
        this(factionId, new EnumMap<>(WorthType.class), name);
    }

    public FactionWorth(String factionId, EnumMap<WorthType, Double> worth, String name) {
        this.factionId = factionId;
        this.worth = worth;
        this.name = name;
        worth.values().forEach(v -> totalWorth += v);
    }

    public String getFactionId() {
        return factionId;
    }

    public double getWorth(WorthType worthType) {
        return worth.getOrDefault(worthType, 0d);
    }

    protected void setWorth(WorthType worthType, double worth) {
        worth = Math.max(0, worth);
        Double prev = this.worth.put(worthType, worth);
        totalWorth += worth - (prev == null ? 0 : prev);
    }

    protected void addWorth(WorthType worthType, double worth) {
        setWorth(worthType, getWorth(worthType) + worth);
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
        return Double.compare(totalWorth, o.totalWorth);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactionWorth that = (FactionWorth) o;
        return Double.compare(that.totalWorth, totalWorth) == 0 &&
                Objects.equals(factionId, that.factionId) &&
                Objects.equals(worth, that.worth) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(factionId, worth, name, totalWorth);
    }

    @Override
    public String toString() {
        return "FactionWorth{" +
                "factionId=" + factionId +
                ", worth=" + worth +
                ", name='" + name + '\'' +
                ", totalWorth=" + totalWorth +
                '}';
    }
}
