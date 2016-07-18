package net.novucs.ftop.hook.event;

import org.bukkit.event.HandlerList;

public class FactionEconomyEvent extends EconomyEvent {

    private static final HandlerList handlers = new HandlerList();
    private final String factionId;

    public FactionEconomyEvent(String factionId, double oldBalance, double newBalance) {
        super(oldBalance, newBalance);
        this.factionId = factionId;
    }

    public String getFactionId() {
        return factionId;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
