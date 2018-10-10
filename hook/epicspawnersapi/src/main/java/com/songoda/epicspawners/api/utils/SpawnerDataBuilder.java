package com.songoda.epicspawners.api.utils;

import java.util.Collection;

import com.songoda.epicspawners.api.particles.ParticleDensity;
import com.songoda.epicspawners.api.particles.ParticleEffect;
import com.songoda.epicspawners.api.particles.ParticleType;
import com.songoda.epicspawners.api.spawner.SpawnerData;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

/**
 * A builder interface to easily construct {@link SpawnerData} objects in the confines
 * of a single line through the use of the builder pattern. Every method returns an
 * instance of itself to allow for chained method calls. For reference on what each
 * of these methods do, see the corresponding setter methods in the SpawnerData interface
 * (methods with the "set" prefix)
 */
public interface SpawnerDataBuilder {

    /**
     * Set the display name for this SpawnerDataBuilder
     * 
     * @param name the name to set
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder displayName(String name);

    /**
     * Define the unique id for this spawner.
     *
     * @param uuid unique id for this SpawnerData
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder uuid(int uuid);

    /**
     * Set the pickup cost for this SpawnerDataBuilder
     * 
     * @param cost the cost to set. Must be greater than or equal to 0.0
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder pickupCost(double cost);

    /**
     * Set the spawn blocks for this SpawnerDataBuilder
     * 
     * @param spawnBlocks a collection of materials to set
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder spawnBlocks(Collection<Material> spawnBlocks);

    /**
     * Set the spawn blocks for this SpawnerDataBuilder
     * 
     * @param spawnBlocks a list of materials to set
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder spawnBlocks(Material... spawnBlocks);

    /**
     * Activate this spawner and enable its use by players. This is similar
     * to calling {@code active(true)}
     * 
     * @return this instance. Chained method calls
     * 
     * @see #active(boolean)
     */
    SpawnerDataBuilder active();

    /**
     * Set whether this spawner should be used by nearby players or not
     * 
     * @param active true if active, false otherwise
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder active(boolean active);

    /**
     * Set this spawner to be displayed in the shop. This is similar to
     * calling {@code inShop(true)}
     * 
     * @return this instance. Chained method calls
     * 
     * @see #inShop(boolean)
     */
    SpawnerDataBuilder inShop();

    /**
     * Set whether this spawner should be displayed in the shop or not
     * 
     * @param inShop true if displayed in shop, false otherwise
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder inShop(boolean inShop);

    /**
     * Set entities spawned from this spawner type on fire. This is similar
     * to calling {@code spawnOnFire(true)}
     * 
     * @return this instance. Chained method calls
     * 
     * @see #spawnOnFire(boolean)
     */
    SpawnerDataBuilder spawnOnFire();

    /**
     * Set whether entities spawned from this spawner type should be set
     * on fire or not
     * 
     * @param onFire true if on fire, false otherwise
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder spawnOnFire(boolean onFire);

    /**
     * Set this spawner type as upgradeable. This is similar to calling
     * {@code upgradeable(true)}
     * 
     * @return this instance. Chained method calls
     * 
     * @see #upgradeable(boolean)
     */
    SpawnerDataBuilder upgradeable();

    /**
     * Set whether this spawner is upgradeable or not
     * 
     * @param upgradeable true if upgradeable, false otherwise
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder upgradeable(boolean upgradeable);

    /**
     * Allow this spawner to be converted to another. This is similar to
     * calling {@code convertible(true)}
     * 
     * @return this instance. Chained method calls
     * 
     * @see #convertible(boolean)
     */
    SpawnerDataBuilder convertible();

    /**
     * Set whether this spawner may be converted to another type or not
     * 
     * @param convertible true if convertible, false otherwise
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder convertible(boolean convertible);

    /**
     * Set the price to be used in this shop when purchasing a spawner
     * of this type
     * 
     * @param price the price to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder shopPrice(double price);

    /**
     * Set the convertion ratio for this spawner type. The ratio represents
     * the amount to subtract from the shop price to create a convertion
     * price
     * 
     * @param ratio the ratio to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder convertRatio(String ratio);

    /**
     * Set the cost required to upgrade this spawner type using economy
     * and player balance (i.e. Vault implementations)
     * 
     * @param cost the cost to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder upgradeCostEconomy(double cost);

    /**
     * Set the cost required to upgrade this spawner type using a player's
     * experience
     * 
     * @param cost the cost to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder upgradeCostExperience(int cost);

    /**
     * Set the goal needed for entities of this spawner type to drop this
     * spawner data
     * 
     * @param goal the kill goal to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder killGoal(int goal);

    /**
     * Set the item that represents this spawner type in both GUIs and
     * within the spawner block itself
     * 
     * @param material the display item to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder displayItem(Material material);

    /**
     * Set the entities to spawn from this spawner when a spawn event
     * has been triggered
     * 
     * @param entities the entities to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder entities(Collection<EntityType> entities);

    /**
     * Set the entities to spawn from this spawner when a spawn event
     * has been triggered
     * 
     * @param entities the entities to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder entities(EntityType... entities);

    /**
     * Set the blocks to spawn from this spawner when a spawn event
     * has been triggered
     * 
     * @param blocks the blocks to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder blocks(Collection<Material> blocks);

    /**
     * Set the blocks to spawn from this spawner when a spawn event
     * has been triggered
     * 
     * @param blocks the blocks to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder blocks(Material... blocks);

    /**
     * Set the items to spawn from this spawner when a spawn event
     * has been triggered
     * 
     * @param items the items to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder items(Collection<ItemStack> items);

    /**
     * Set the items to spawn from this spawner when a spawn event
     * has been triggered
     * 
     * @param items the items to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder items(ItemStack... items);

    /**
     * Set the commands to be executed when a spawn event has been
     * triggered
     * 
     * @param commands the commands to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder commands(Collection<String> commands);

    /**
     * Set the commands to be executed when a spawn event has been
     * triggered
     * 
     * @param commands the commands to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder commands(String... commands);

    /**
     * Set the items to be dropped after an entity from this spawner
     * has been killed
     * 
     * @param items the items to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder entityDroppedItems(Collection<ItemStack> items);

    /**
     * Set the items to be dropped after an entity from this spawner
     * has been killed
     * 
     * @param items the items to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder entityDroppedItems(ItemStack... items);

    /**
     * Set the tick rate at which this spawner will trigger a spawn
     * event. The tick rate is used to calculate the spawn delay for
     * this particular spawner type
     * 
     * @param tickRate the tick rate to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder tickRate(String tickRate);

    /**
     * Set the particle effect to be used when displaying complex or
     * complicated particle effects
     * 
     * @param particle the particle effect to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder particleEffect(ParticleEffect particle);

    /**
     * Set the particle type to be used when displaying complex or
     * complicated particle effects
     * 
     * @param particle the particle type to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder spawnEffectParticle(ParticleType particle);

    /**
     * Set the particle type to be used when entities have been
     * spawned from this spawner
     * 
     * @param particle the particle type to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder entitySpawnParticle(ParticleType particle);

    /**
     * Set the particle type to be used when entities have been
     * spawned from this spawner
     * 
     * @param particle the particle type to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder spawnerSpawnParticle(ParticleType particle);

    /**
     * Set the particle density to be used when a particle effect
     * is to be displayed to nearby players
     * 
     * @param density the particle density to set
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder particleDensity(ParticleDensity density);

    /**
     * Set complicated / complex particles to only be displayed on
     * boosted spawners. This is similar to calling
     * {@code particleEffectBoostedOnly(true)}
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder particleEffectBoostedOnly();

    /**
     * Set whether complicated / complex particles will only be
     * displayed on boosted spawners
     * 
     * @param boostedOnly true if boosted only, false otherwise
     * 
     * @return this instance. Chained method calls
     */
    SpawnerDataBuilder particleEffectBoostedOnly(boolean boostedOnly);

    /**
     * Complete the building of this {@link SpawnerData} instance and
     * return the completed object with all changes applied
     * 
     * @return the final SpawnerData instance
     */
    SpawnerData build();

}