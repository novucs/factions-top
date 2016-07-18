package net.novucs.ftop.hook.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EconomyEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final double oldBalance;
    private final double newBalance;

    public EconomyEvent(double oldBalance, double newBalance) {
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
    }

    public double getOldBalance() {
        return oldBalance;
    }

    public double getNewBalance() {
        return newBalance;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
