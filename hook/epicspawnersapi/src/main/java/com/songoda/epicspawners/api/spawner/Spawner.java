package com.songoda.epicspawners.api.spawner;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collection;

/**
 * Represents a spawner stack container in the game world.
 *
 * @since 5.0
 */
public interface Spawner {

    /**
     * Get identifying name of this spawner.
     *
     * <p>Will return Omni if multiple {@link SpawnerData}
     * objects are present.</p>
     *
     * @return name of this spawner
     */
    String getIdentifyingName();

    /**
     * Get identifying SpawnerData for this spawner.
     *
     * @return SpawnerData for this spawner
     */
    SpawnerData getIdentifyingData();

    /**
     * Get custom display name as to be used when
     * displaying this spawner.
     * 
     * <p>Will return Omni if multiple {@link SpawnerData}
     * objects are present.</p>
     *
     * @return display name
     */
    String getDisplayName();

    /**
     * Get location of the spawner object.
     *
     * @return location of spawner
     */
    Location getLocation();

    /**
     * Get the X coordinate for the spawner object.
     *
     * @return X coordinate.
     */
    int getX();

    /**
     * Get the Y coordinate for the spawner object.
     *
     * @return Y coordinate.
     */
    int getY();

    /**
     * Get the Z coordinate for the spawner object.
     *
     * @return Z coordinate.
     */
    int getZ();

    /**
     * Get the world that the spawner is in.
     *
     * @return spawner world.
     */
    World getWorld();

    /**
     * Get the {@link CreatureSpawner} object from this
     * spawner blocks {@link BlockState}
     *
     * @return this blocks CreatureSpawner
     */
    CreatureSpawner getCreatureSpawner();

    /**
     * Get the SpawnerStacks contained in this spawner.
     *
     * @return SpawnerStacks
     */
    Collection<SpawnerStack> getSpawnerStacks();


    /**
     * Add a SpawnerStack to this spawner.
     *
     * @param spawnerStack the desired SpawnerStack.
     */
    void addSpawnerStack(SpawnerStack spawnerStack);

    /**
     * This will clear the {@link SpawnerStack} objects from
     * this spawner.
     */
    void clearSpawnerStacks();


    /**
     * This will return the {@link SpawnerStack} located at the
     * top of the {@link ArrayDeque}.
     *
     * @return first SpawnerStack
     */
    SpawnerStack getFirstStack();

    /**
     * Get the player who placed this Spawner.
     *
     * @return the placer. null if not placed by player
     */
    OfflinePlayer getPlacedBy();

    /**
     * Set the amount of spawns that this spawner has initiated
     *
     * @param count number of spawns
     */
    void setSpawnCount(int count);

    /**
     * Get the amount of spawns that this spawner has initiated.
     *
     * @return amount of spawns
     */
    int getSpawnCount();

    /**
     * Get the total number of {@link SpawnerData} objects
     * contained by this spawner.
     *
     * @return count
     */
    int getSpawnerDataCount();

    /**
     * Check spawner conditions before spawning.
     *
     * @return true of conditions met, false otherwise
     */
    boolean checkConditions();

    /**
     * Extracts the spawner type and amount from the provided
     * {@link ItemStack} then forwards the Stack method.
     *
     * @param player the player performing the stacking
     * @param item   the spawner item to be stacked
     * @return true if successful, false otherwise
     */
    boolean preStack(Player player, ItemStack item);


    /**
     * Converts the provided ItemStack to a Spawner stack and
     * adds it to this Spawner.
     *
     * @param player the player performing the stacking
     * @param type   the type of spawner to stack
     * @param amt    the amount of that spawner type to stack
     * @return true if successful, false otherwise
     * @deprecated see {@link #stack(Player, SpawnerData, int)}
     */
    @Deprecated
    boolean stack(Player player, String type, int amt);

    /**
     * Converts the provided ItemStack to a Spawner stack and
     * adds it to this Spawner.
     *
     * @param player the player performing the stacking
     * @param data   the type of spawner to stack
     * @param amount the amount of that spawner type to stack
     * @return true if successful, false otherwise
     */
    boolean stack(Player player, SpawnerData data, int amount);

    /**
     * Removes the topmost {@link SpawnerData} from this
     * spawner.
     *
     * @param player the player performing the unstacking
     * @return true if successful, false otherwise
     */
    boolean unstack(Player player);


    /**
     * Get the total boosted amount from the spawner.
     *
     * @return the total boost
     */
    int getBoost();

    /**
     * Get the end of life for the current closest to
     * end boost.
     *
     * @return time that the boost will end in
     * miliseconds.
     */
    Instant getBoostEnd();

    /**
     * Updates the delay of the spawner to use the equation
     * defined by EpicSpawners as apposed to using the default
     * Minecraft delay.
     *
     * @return delay set
     */
    int updateDelay(); // Updates delay of the spawner

    /**
     * You can use this method to force a spawn of this spawner.
     */
    void spawn();
}