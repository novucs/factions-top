package com.songoda.epicspawners.api;

import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerManager;

import org.bukkit.inventory.ItemStack;

/**
 * The access point of the EpicSpawnersAPI, a class acting as a bridge between API
 * and plugin implementation. It is from here where developers should access the
 * important and core methods in the API. All static methods in this class will
 * call directly upon the implementation at hand (in most cases this will be the
 * EpicSpawners plugin itself), therefore a call to {@link #getImplementation()} is
 * not required and redundant in most situations. Method calls from this class are
 * preferred the majority of time, though an instance of {@link EpicSpawners} may
 * be passed if absolutely necessary.
 * 
 * @see EpicSpawners
 * @since 5.0.0
 */
public class EpicSpawnersAPI {

    private static EpicSpawners implementation;

    /**
     * Set the EpicSpawners implementation. Once called for the first time, this
     * method will throw an exception on any subsequent invocations. The implementation
     * may only be set a single time, presumably by the EpicSpawners plugin
     * 
     * @param implementation the implementation to set
     */
    public static void setImplementation(EpicSpawners implementation) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the EpicSpawners implementation. This method may be redundant in most
     * situations as all methods present in {@link EpicSpawners} will be mirrored
     * with static modifiers in the {@link EpicSpawnersAPI} class
     * 
     * @return the EpicSpawners implementation
     */
    public static EpicSpawners getImplementation() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get an instance of the {@link SpawnerManager}
     * 
     * @return the spawner manager
     */
    public static SpawnerManager getSpawnerManager() {
        throw new UnsupportedOperationException();
    }

    /**
     * Create an item representation of the provided SpawnerData.
     * 
     * @param data the SpawnerData for which to create an item
     * @param amount the amount of items to create
     * 
     * @return new spawner item
     * 
     * @see SpawnerData#toItemStack(int)
     */
    public static ItemStack newSpawnerItem(SpawnerData data, int amount) {
        throw new UnsupportedOperationException();
    }

    /**
     * Create an item representation of the provided SpawnerData.
     * 
     * @param data the SpawnerData for which to create an item
     * @param amount the amount of items to create
     * @param stackSize the amount of stacked spawners
     * 
     * @return new spawner item
     * 
     * @see SpawnerData#toItemStack(int, int)
     */
    public static ItemStack newSpawnerItem(SpawnerData data, int amount, int stackSize) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the {@link SpawnerData} applicable to this ItemStack.
     * 
     * @param item the item from which to retrieve SpawnerData
     * 
     * @return the SpawnerData. null if item is null or has no meta
     */
    public static SpawnerData getSpawnerDataFromItem(ItemStack item) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the amount of spawners stacked in an ItemStack.
     * 
     * @param item the ItemStack to check
     * 
     * @return the stack size. 1 if invalid or null.
     */
    public static int getStackSizeFromItem(ItemStack item) {
        throw new UnsupportedOperationException();
    }

}
