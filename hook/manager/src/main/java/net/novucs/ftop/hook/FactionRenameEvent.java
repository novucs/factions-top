package net.novucs.ftop.hook;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FactionRenameEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final String factionId;
    private final String oldName;
    private final String newName;

    public FactionRenameEvent(String factionId, String oldName, String newName) {
        this.factionId = factionId;
        this.oldName = oldName;
        this.newName = newName;
    }

    public String getFactionId() {
        return factionId;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
