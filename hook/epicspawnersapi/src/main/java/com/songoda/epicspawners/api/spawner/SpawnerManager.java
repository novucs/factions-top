package com.songoda.epicspawners.api.spawner;

import org.bukkit.Location;

import java.util.Collection;
import java.util.Map;

public interface SpawnerManager {

    @Deprecated
    Map<String, SpawnerData> getRegisteredSpawnerData();

    Spawner getSpawnerFromWorld(Location location);

    @Deprecated
    Map<Location, Spawner> getSpawnersInWorld();

    Collection<Spawner> getSpawners();

}
