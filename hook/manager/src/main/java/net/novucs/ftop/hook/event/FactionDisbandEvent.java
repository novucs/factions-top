package net.novucs.ftop.hook.event;

import org.bukkit.event.HandlerList;

public class FactionDisbandEvent extends FactionEvent {

    private static final HandlerList handlers = new HandlerList();
    private final String name;

    public FactionDisbandEvent(String factionId, String name) {
        super(factionId);
        this.name = name;
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
