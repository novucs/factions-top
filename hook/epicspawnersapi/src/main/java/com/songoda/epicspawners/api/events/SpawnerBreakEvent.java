package com.songoda.epicspawners.api.events;

import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.spawner.Spawner;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a spawner has been broken in the world
 */
public class SpawnerBreakEvent extends SpawnerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    @Deprecated private final Location location;
    @Deprecated private final int stackSize;
    @Deprecated private final String type;

    public SpawnerBreakEvent(Player player, Spawner spawner) {
        super(player, spawner);

        this.location = spawner.getLocation();
        this.stackSize = spawner.getSpawnerDataCount();
        this.type = spawner.getIdentifyingName();
    }

    @Deprecated
    public SpawnerBreakEvent(Location location, Player player) {
        this(player, EpicSpawnersAPI.getSpawnerManager().getSpawnerFromWorld(location));
    }

    /**
     * Get the location at which the spawner was broken
     * 
     * @return the spawner location
     * 
     * @deprecated refer to the {@link #getSpawner()} method
     */
    @Deprecated
    public Location getLocation() {
        return location;
    }

    /**
     * Get the amount of SpawnerData objects held by the broken spawner
     * 
     * @return the spawner multiplier
     * 
     * @deprecated refer to the {@link #getSpawner()} method
     */
    @Deprecated
    public int getMulti() {
        return stackSize;
    }

    /**
     * Get the type of spawner data
     * 
     * @return the spawner data
     * 
     * @deprecated refer to the {@link #getSpawner()} method
     */
    @Deprecated
    public String getType() {
        return type;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean canceled) {
        this.cancelled = canceled;
    }

}
