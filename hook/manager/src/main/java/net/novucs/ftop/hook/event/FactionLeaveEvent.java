package net.novucs.ftop.hook.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class FactionLeaveEvent extends FactionEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;

    public FactionLeaveEvent(String factionId, Player player) {
        super(factionId);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
