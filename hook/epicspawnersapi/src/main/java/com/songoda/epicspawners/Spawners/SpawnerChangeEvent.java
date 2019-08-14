package com.songoda.epicspawners.Spawners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpawnerChangeEvent extends Event {

    public SpawnerChangeEvent(Location location, Player player, int multi, int oldMulti) {
        throw new UnsupportedOperationException();
    }

    public Block getSpawner() {
        throw new UnsupportedOperationException();
    }

    public int getCurrentMulti() {
        throw new UnsupportedOperationException();
    }

    public int getOldMulti() {
        throw new UnsupportedOperationException();
    }

    public Player getPlayer() {
        throw new UnsupportedOperationException();
    }

    public HandlerList getHandlers() {
        throw new UnsupportedOperationException();
    }

    public static HandlerList getHandlerList() {
        throw new UnsupportedOperationException();
    }
}
