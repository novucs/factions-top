package net.novucs.ftop.hook.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PlayerEconomyEvent extends EconomyEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;

    public PlayerEconomyEvent(Player player, double oldBalance, double newBalance) {
        super(oldBalance, newBalance);
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
