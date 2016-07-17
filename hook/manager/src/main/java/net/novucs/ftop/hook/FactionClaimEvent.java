package net.novucs.ftop.hook;

import com.google.common.collect.Multimap;
import net.novucs.ftop.ChunkPos;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FactionClaimEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final String factionId;
    private final Multimap<String, ChunkPos> claims;

    public FactionClaimEvent(String factionId, Multimap<String, ChunkPos> claims) {
        this.factionId = factionId;
        this.claims = claims;
    }

    public String getFactionId() {
        return factionId;
    }

    public Multimap<String, ChunkPos> getClaims() {
        return claims;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
