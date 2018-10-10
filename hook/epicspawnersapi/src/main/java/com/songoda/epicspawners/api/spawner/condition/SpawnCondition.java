package com.songoda.epicspawners.api.spawner.condition;

import java.util.function.Predicate;

import com.songoda.epicspawners.api.spawner.Spawner;

/**
 * A predicate on which to check whether a {@link Spawner} should
 * be permitted to perform a spawn or not.
 */
public interface SpawnCondition {

	/**
	 * Get the name of this spawn condition.
	 *
	 * @return the name of this condition
	 */
    String getName();

    /**
     * Get a short description of what this condition imposes on
     * a spawner. This should be brief enough to display to players.
     *
     * @return the description for this condition
     */
    String getDescription();

    /**
     * Check whether the provided spawner meets this condition or not.
     *
     * @param spawner the spawner to check
     *
     * @return true if condition is met, false otherwise
     */
    boolean isMet(Spawner spawner);

    /**
     * Get this SpawnCondition instance as a {@link Predicate}
     *
     * @return this condition as a predicate
     */
    default Predicate<Spawner> asPredicate() {
    	return this::isMet;
    }

}