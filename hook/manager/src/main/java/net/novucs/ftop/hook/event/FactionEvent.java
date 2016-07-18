package net.novucs.ftop.hook.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FactionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final String factionId;

    public FactionEvent(String factionId) {
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
