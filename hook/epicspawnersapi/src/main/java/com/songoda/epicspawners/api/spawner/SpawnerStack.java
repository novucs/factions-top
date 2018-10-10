package com.songoda.epicspawners.api.spawner;

/**
 * Represents a stack of spawners
 */
public interface SpawnerStack {

    /**
     * Get the SpawnerData object assigned to
     * this SpawnerStack.
     *
     * @return the attached SpawnerData
     */
    SpawnerData getSpawnerData();

    /**
     * Get if this stack has SpawnerData.
     *
     * @return if has spawner data.
     */
    boolean hasSpawnerData();

    /**
     * Set the SpawnerData object for this
     * SpawnerStack.
     *
     * @param data SpawnerData to set
     */
    void setSpawnerData(SpawnerData data);

    /**
     * Get the stack size for this
     * SpawnerStack.
     *
     * @return the stack size
     */
    int getStackSize();

    /**
     * Set the stack size for this
     * SpawnerStack.
     *
     * @param size the stack size
     */
    void setStackSize(int size);

}
