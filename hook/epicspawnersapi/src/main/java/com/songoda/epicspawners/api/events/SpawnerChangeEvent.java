package com.songoda.epicspawners.api.events;

import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerManager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a spawner has been changed. This includes changes such as stack size as
 * well as a change in {@link SpawnerData}
 */
public class SpawnerChangeEvent extends SpawnerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    public enum ChangeType {
        STACK_SIZE, SPAWNER_DATA;
    }

    private boolean canceled = false;

    private final int stackSize, oldStackSize;
    private final SpawnerData spawnerData, oldSpawnerData;
    private final ChangeType type;

    public SpawnerChangeEvent(Player player, Spawner spawner, int stackSize, int oldStackSize) {
        super(player, spawner);
        throw new UnsupportedOperationException();
    }

    public SpawnerChangeEvent(Player player, Spawner spawner, SpawnerData data, SpawnerData oldSpawnerData) {
        super(player, spawner);
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public SpawnerChangeEvent(Location location, Player player, int stackSize, int oldStackSize) {
        this(player, EpicSpawnersAPI.getSpawnerManager().getSpawnerFromWorld(location), stackSize, oldStackSize);
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public SpawnerChangeEvent(Location location, Player player, SpawnerData data, SpawnerData oldSpawnerData) {
        this(player, EpicSpawnersAPI.getSpawnerManager().getSpawnerFromWorld(location), data, oldSpawnerData);
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public SpawnerChangeEvent(Location location, Player player, String type, String oldType) {
        super(player, EpicSpawnersAPI.getSpawnerManager().getSpawnerFromWorld(location));
        throw new UnsupportedOperationException();
    }

    /**
     * Get the new stack size of the spawner after this event completes. If this
     * event is not to do with stack size changing, this method simply returns
     * the spawner's current stack size
     * 
     * @return the new stack size
     */
    public int getStackSize() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the old stack size of the spawner from before this event was called. If
     * this event is not to do with stack size changing, this method simply returns
     * the spawner's current stack size
     * 
     * @return the old stack size
     */
    public int getOldStackSize() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the new spawner data after this event completes. If this event is not
     * to do with the spawner data changing, this method simply returns null
     * 
     * @return the new spawner data
     */
    public SpawnerData getSpawnerData() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the old spawner data from before this event was called. If this event
     * is not to do with the spawner data changing, this method simply returns null
     * 
     * @return the old spawner data
     */
    public SpawnerData getOldSpawnerData() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the type of change performed in this event
     * 
     * @return the change type
     */
    public ChangeType getChange() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HandlerList getHandlers() {
        throw new UnsupportedOperationException();
    }

    public static HandlerList getHandlerList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCancelled(boolean canceled) {
        throw new UnsupportedOperationException();
    }

    /**
     * The spawner's multiplier (stack size)
     * 
     * @return the stack size
     * 
     * @deprecated see {@link #getStackSize()}
     */
    @Deprecated
    public int getCurrentMulti() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the old stack size
     * 
     * @return the old stack size
     * 
     * @deprecated see {@link #getOldStackSize()}
     */
    @Deprecated
    public int getOldMulti() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the new spawner data type
     * 
     * @return the spawner type
     * 
     * @deprecated see {@link #getSpawnerData()}
     */
    @Deprecated
    public String getType() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the old spawner data type
     * 
     * @return the spawner type
     * 
     * @deprecated see {@link #getOldSpawnerData()}
     */
    @Deprecated
    public String getOldType() {
        throw new UnsupportedOperationException();
    }

}
