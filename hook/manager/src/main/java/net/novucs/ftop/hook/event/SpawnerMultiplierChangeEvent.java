package net.novucs.ftop.hook.event;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

public class SpawnerMultiplierChangeEvent extends BlockEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final CreatureSpawner spawner;
    private final int oldMultiplier;
    private final int newMultiplier;

    public SpawnerMultiplierChangeEvent(CreatureSpawner spawner, int oldMultiplier, int newMultiplier) {
        super(spawner.getBlock());
        this.spawner = spawner;
        this.oldMultiplier = oldMultiplier;
        this.newMultiplier = newMultiplier;
    }

    public CreatureSpawner getSpawner() {
        return spawner;
    }

    public int getOldMultiplier() {
        return oldMultiplier;
    }

    public int getNewMultiplier() {
        return newMultiplier;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
