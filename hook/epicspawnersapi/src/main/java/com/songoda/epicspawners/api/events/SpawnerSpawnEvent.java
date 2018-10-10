package com.songoda.epicspawners.api.events;

import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.spawner.Spawner;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

/**
 * Called when a spawner spawns an entity.
 */
public class SpawnerSpawnEvent extends EntityEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean canceled = false;

    private Spawner spawner;

    public SpawnerSpawnEvent(Entity entity, Spawner spawner) {
        super(entity);
        this.spawner = spawner;
    }

    public Spawner getSpawner() {
        return spawner;
    }

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

}