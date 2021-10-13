package com.songoda.epicspawners.api.events;

import com.songoda.epicspawners.api.spawner.Spawner;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

public abstract class SpawnerEvent extends PlayerEvent {

    public SpawnerEvent(Player who, Spawner spawner) {
        super(who);
        throw new UnsupportedOperationException();
    }

    public Spawner getSpawner() {
        throw new UnsupportedOperationException();
    }

}
