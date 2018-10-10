package com.songoda.epicspawners.api.events;

import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.spawner.Spawner;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a spawner has been dropped in the world after being broken
 */
public class SpawnerDropEvent extends SpawnerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean canceled = false;

    @Deprecated private final Location location;
    @Deprecated private final int stackSize;
    @Deprecated private final EntityType type;

    public SpawnerDropEvent(Player player, Spawner spawner) {
        super(player, spawner);

        // Legacy
        this.location = spawner.getLocation();
        this.stackSize = spawner.getSpawnerDataCount();
        this.type = (spawner.getCreatureSpawner() != null) ? spawner.getCreatureSpawner().getSpawnedType() : null;
    }

    @Deprecated
    public SpawnerDropEvent(Location location, Player player) {
        this(player, EpicSpawnersAPI.getSpawnerManager().getSpawnerFromWorld(location));
    }

    /**
     * Get the location at which the spawner was broken
     * 
     * @return the spawner location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Get the stack size of the broken spawner
     * 
     * @return the spawner stack size
     */
    public int getStackSize() {
        return stackSize;
    }

    /**
     * Get the type of entity that was spawned from the broken spawner
     * 
     * @return the spawner type
     */
    public EntityType getType() {
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
        return canceled;
    }

    @Override
    public void setCancelled(boolean canceled) {
        this.canceled = canceled;
    }

    /**
     * Get the multiplier of the broken spawner
     * 
     * @return the stack size
     * 
     * @deprecated see {@link #getStackSize()}
     */
    @Deprecated
    public int getMultiSize() {
        return stackSize;
    }
}