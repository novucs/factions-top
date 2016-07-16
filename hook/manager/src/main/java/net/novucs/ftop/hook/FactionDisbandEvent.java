package net.novucs.ftop.hook;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FactionDisbandEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final String factionId;
    private final String name;

    public FactionDisbandEvent(String factionId, String name) {
        this.factionId = factionId;
        this.name = name;
    }

    public String getFactionId() {
        return factionId;
    }

    public String getName() {
        return name;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
