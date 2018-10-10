package com.songoda.epicspawners.api;

/**
 * Represents a cost type when making a purchase from EpicSpawners
 */
public enum CostType {

    /**
     * A purchase made with an economy balance (generally an implementation of Vault)
     */
    ECONOMY,

    /**
     * A purchase made with a player's experience levels
     */
    EXPERIENCE

}