package com.songoda.epicspawners.api.events;

import com.songoda.epicspawners.api.spawner.Spawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public abstract class SpawnerBreakEvent extends SpawnerEvent implements Cancellable {

    public SpawnerBreakEvent(Player player, Spawner spawner) {
        super(player, spawner);
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
