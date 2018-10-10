package com.songoda.epicspawners.api.spawner;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.Collection;
import java.util.Map;

/**
 * A manager class to handle {@link Spawner} instances and registered {@link SpawnerData}.
 * Any spawners in the world will be handled in here alongside any created or cached
 * spawner data types
 */
public interface SpawnerManager {

    /**
     * Get {@link SpawnerData} by its identifying name.
     *
     * @param name the name of the spawner data
     *             you are looking for
     * @return spawner data
     */
    SpawnerData getSpawnerData(String name);

    /**
     * Get {@link SpawnerData} from EntityType.
     *
     * @param type the EntityType to convert
     *
     * @return spawner data
     */
    SpawnerData getSpawnerData(EntityType type);

    /**
     * Add spawner data to memory.
     *
     * @param name        the identifying name of this
     *                    spawner data
     * @param spawnerData the object for your spawner
     *                    data
     *
     * @see #addSpawnerData(SpawnerData)
     */
    void addSpawnerData(String name, SpawnerData spawnerData);

    /**
     * Add spawner data to memory
     * 
     * @param spawnerData the spawner data to register
     */
    void addSpawnerData(SpawnerData spawnerData);

    /**
     * Remove {@link SpawnerData} from memory by its
     * identifying name.
     *
     * @param name the name identifying the {@link SpawnerData}
     */
    void removeSpawnerData(String name);

    /**
     * Get a map of all SpawnerData registered to memory.
     *
     * @return map of SpawnerData
     *
     * @deprecated see {@link #getAllSpawnerData()}
     */
    @Deprecated
    Map<String, SpawnerData> getRegisteredSpawnerData();

    /**
     * Get an immutable collection of all SpawnerData
     * registered to memory
     *
     * @return all registered spawner data
     */
    Collection<SpawnerData> getAllSpawnerData();

    /**
     * Whether or not this location contains a registered
     * spawner.
     *
     * @param location the location where the spawner is located
     * @return true if is a spawner, false otherwise
     */
    boolean isSpawner(Location location);

    /**
     * Whether or not the provided string is the identifier for
     * a {@link SpawnerData} registered object or not.
     *
     * @param type the type of spawner
     * @return true if is valid SpawnerData, false otherwise
     */
    boolean isSpawnerData(String type);

    /**
     * Get an existing spawner from the game world that is
     * registered into memory.
     *
     * @param location the location where the spawner should be
     *                 found
     * @return resulting spawner
     */
    Spawner getSpawnerFromWorld(Location location);

    /**
     * Add a spawner into the spawner registry.
     *
     * @param location location where the spawner exists in the
     *                 game world
     * @param spawner  the spawner object
     */
    void addSpawnerToWorld(Location location, Spawner spawner);

    /**
     * Remove a spawner from the spawner registry.
     *
     * @param location location in which spawner has been removed
     * @return the spawner that was removed
     */
    Spawner removeSpawnerFromWorld(Location location);

    /**
     * Get a Map of all the spawners currently registered into
     * memory.
     *
     * @return all registered spawners.
     *
     * @deprecated see {@link #getSpawners()} for a more proper
     * return value. This method will be removed in the near future
     */
    @Deprecated
    Map<Location, Spawner> getSpawnersInWorld();

    /**
     * Get an immutable collection of all spawners currently
     * registered into memory
     *
     * @return all registered spawners
     */
    Collection<Spawner> getSpawners();

}