package com.songoda.epicspawners.api.particles;

/**
 * All possible types of particle patterns to be displayed around a spawner
 */
public enum ParticleEffect {

    /**
     * No particle effect
     */
    NONE,

    /**
     * A circular trail effect encompassing the spawner
     */
    HALO,

    /**
     * Two circles displaying in a target-like formation (one large and one
     * small circle)
     */
    TARGET

}