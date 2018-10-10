package com.songoda.epicspawners.api.events;

import com.songoda.epicspawners.api.spawner.Spawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public abstract class SpawnerChangeEvent extends SpawnerEvent implements Cancellable {

    public SpawnerChangeEvent(Player who, Spawner spawner) {
        super(who, spawner);
    }

    public int getStackSize() {
        throw new UnsupportedOperationException();
    }

    public int getOldStackSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HandlerList getHandlers() {
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

}
