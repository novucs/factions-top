package com.songoda.epicspawners.api.events;

import com.songoda.epicspawners.api.spawner.Spawner;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;

/**
 * Represents an abstract {@link Event} given a {@link Player} and {@link Spawner} instance
 */
public abstract class SpawnerEvent extends PlayerEvent {

    protected final Spawner spawner;

    public SpawnerEvent(Player who, Spawner spawner) {
        super(who);
        throw new UnsupportedOperationException();
    }

    /**
     * Get the {@link Spawner} involved in this event
     * 
     * @return the broken spawner
     */
    public Spawner getSpawner() {
        throw new UnsupportedOperationException();
    }

}