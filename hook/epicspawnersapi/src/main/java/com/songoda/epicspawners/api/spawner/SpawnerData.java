package com.songoda.epicspawners.api.spawner;

import com.songoda.epicspawners.api.EpicSpawnersAPI;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Represents a {@link Spawner}'s data. All data held within this object is kept in memory
 * when breaking and placing spawner objects in the world
 */
public interface SpawnerData {

    /**
     * Reloads all spawn methods.
     * <p>
     * Use this after adding content to your spawning
     * lists.
     */
    void reloadSpawnMethods();

    /**
     * Returns the uuid for this SpawnerData.
     *
     * @return uuid for SpawnerData
     */
    int getUUID();

    void setUUID(int uuid);

    /**
     * Create an item representation of this SpawnerData.
     *
     * @return new spawner item
     *
     * @see EpicSpawnersAPI#newSpawnerItem(SpawnerData, int, int)
     */
    ItemStack toItemStack();

    /**
     * Create an item representation of this SpawnerData.
     *
     * @param amount the amount of items to create
     *
     * @return new spawner item
     *
     * @see EpicSpawnersAPI#newSpawnerItem(SpawnerData, int, int)
     */
    ItemStack toItemStack(int amount);

    /**
     * Create an item representation of this SpawnerData.
     *
     * @param amount the amount of items to create
     * @param stackSize the amount of stacked spawners
     *
     * @return new spawner item
     *
     * @see EpicSpawnersAPI#newSpawnerItem(SpawnerData, int, int)
     */
    ItemStack toItemStack(int amount, int stackSize);

    /**
     * Get identifying name for this spawner type.
     *
     * @return name of this spawner
     */
    String getIdentifyingName();

    /**
     * Get custom display name as to be used when
     * displaying this spawners of this type.
     *
     * <p>Will return Omni if multiple {@link SpawnerData}
     * objects are present.</p>
     *
     * @return display name
     */
    String getDisplayName();

    /**
     * Set the display name for this spawner type
     * that will be used when displaying spawners of
     * this type.
     *
     * <p>Accepts color codes.</p>
     *
     * @param name new name to identify spawner with.
     */
    void setDisplayName(String name);

    /**
     * Get the cost to be applied when a spawner of
     * this type is broken by a player.
     *
     * @return the spawner types cost
     */
    double getPickupCost();

    /**
     * Set the cost to pickup spawners of this type.
     *
     * @param pickupCost new cost to charge
     */
    void setPickupCost(double pickupCost);

    /**
     * Get an array listing all the blocks in which this
     * spawner type will spawn entities on top of.
     *
     * @return Array of spawn blocks.
     */
    Material[] getSpawnBlocks();

    /**
     * Get a list listing all the blocks in which this
     * spawner type will spawn entities on top of.
     *
     * @return List of spawn blocks.
     */
    List<Material> getSpawnBlocksList();

    /**
     * Replace the list of spawn blocks with a new list.
     *
     * @param spawnBlocks new list of spawn blocks
     */
    void setSpawnBlocks(List<Material> spawnBlocks);

    /**
     * Whether or not this spawner type is enabled or not.
     *
     * @return true if enabled, false otherwise
     */
    boolean isActive();

    /**
     * Enable or disable a spawner from being used by
     * players.
     *
     * @param active new active state.
     */
    void setActive(boolean active);

    /**
     * Whether or not this spawner type is in the shop.
     *
     * @return true if in shop, false otherwise
     */
    boolean isInShop();

    /**
     * Set that this spawner type is enabled in the shop.
     *
     * @param inShop new active state
     */
    void setInShop(boolean inShop);


    /**
     * Whether or not this spawner type will spawn entities
     * on fire.
     *
     * @return true if spawning on fire, false otherwise
     */
    boolean isSpawnOnFire();

    /**
     * Set entities that spawn from this spawner to spawn
     * on fire.
     *
     * @param spawnOnFire new active state
     */
    void setSpawnOnFire(boolean spawnOnFire);

    /**
     * Whether or not this spawner type is upgradable.
     *
     * @return true if upgradable, false otherwise
     */
    boolean isUpgradeable();

    /**
     * Set that this spawner type is upgradeable.
     *
     * @param upgradeable new active state
     */
    void setUpgradeable(boolean upgradeable);

    /**
     * Whether or not this spawner type can be
     * converted to another spawner type.
     *
     * @return true if convertible, false otherwise.
     */
    boolean isConvertible();

    /**
     * Set that this spawner type is convertible.
     *
     * @param convertible new active state.
     */
    void setConvertible(boolean convertible);

    /**
     * The price used when buying this spawner type
     * in the spawner shop.
     *
     * <p>Also used in reference by the convert price
     * calculation.</p>
     *
     * @return the price set
     * @see #getConvertRatio()
     */
    double getShopPrice();

    /**
     * Set the price of the spawner type in the
     * spawner shop.
     *
     * @param shopPrice return the price of
     *                  the spawner in the
     *                  spawner shop
     */
    void setShopPrice(double shopPrice);

    /**
     * Get the ratio to be subtracted from the
     * spawner shop price to create the
     * conversion price.
     *
     * @return conversion ratio
     */
    String getConvertRatio();

    /**
     * Set the conversion ratio for the spawner
     * type.
     *
     * @param ratio ratio to apply
     */
    void setConvertRatio(String ratio);

    /**
     * Get calculated price for spawner
     * conversion from this spawner type to
     * another.
     *
     * @return conversion price
     */
    double getConvertPrice();

    /**
     * Get the cost needed to upgrade this
     * spawner type with economy.
     *
     * @return economy cost
     */
    double getUpgradeCostEconomy();

    /**
     * Set the cost needed to upgrade the
     * spawner type with economy.
     *
     * @param cost economy cost
     */
    void setUpgradeCostEconomy(double cost);

    /**
     * Get the cost needed to upgrade the
     * spawner type with experience.
     *
     * @return experience cost
     */
    int getUpgradeCostExperience();

    /**
     * Set the cost needed to upgrade the
     * spawner type with experience.
     *
     * @param cost experience cost
     */
    void setUpgradeCostExperience(int cost);

    /**
     * Get the goal needed for entities of
     * this spawner type to drop a spawner.
     *
     * @return kill goal
     */
    int getKillGoal();

    /**
     * Set the goal needed for entities of
     * this spawner type to drop a spawner.
     *
     * @param killGoal kill goal
     */
    void setKillGoal(int killGoal);

    /**
     * Get the item that represents this
     * spawner type in both GUIs and
     * within the spawner block itself.
     *
     * @return material used as the display
     * item
     */
    Material getDisplayItem();

    /**
     * Set the item that represents this
     * spawner type in both GUIs and
     * within the spawner block itself.
     *
     * @param displayItem material used as the
     *                    display item
     */
    void setDisplayItem(Material displayItem);

    /**
     * Gets the entities that are to spawn
     * from the spawner type when a spawn
     * is triggered.
     *
     * @return list of applicable entities
     */
    List<EntityType> getEntities();

    /**
     * Set the entities that are to spawn
     * from the spawner type when a spawn
     * is triggered.
     *
     * @param entities list of applicable
     *                 entities
     */
    void setEntities(List<EntityType> entities);

    /**
     * Get the blocks that are to be
     * systematically placed around a spawner
     * block of this type when a spawn is
     * triggered.
     *
     * @return list of applicable blocks
     */
    List<Material> getBlocks();

    /**
     * Set the blocks that are to be
     * systematically placed around a spawner
     * block of this type when a spawn is
     * triggered.
     *
     * @param blocks list of applicable blocks
     */
    void setBlocks(List<Material> blocks);

    /**
     * Get the items that will drop when a
     * entity is killed after spawning from
     * this spawner.
     *
     * @return list of to be dropped  ItemStacks
     */
    List<ItemStack> getEntityDroppedItems();

    /**
     * Set the items that will drop when a
     * entity is killed after spawning from
     * this spawner.
     *
     * @param items list of to be dropped  ItemStacks
     */
    void setEntityDroppedItems(List<ItemStack> items);

    /**
     * Get the items that are to be
     * spawned out of a spawner block
     * of this type when a spawn is
     * triggered.
     *
     * @return list of applicable ItemStacks
     */
    List<ItemStack> getItems();

    /**
     * Set the items that are to be
     * spawned out of a spawner block
     * of this type when a spawn is
     * triggered.
     *
     * @param item list of applicable ItemStacks
     */
    void setItems(List<ItemStack> item);

    /**
     * Get the list of commands executed
     * by spawners of this type when a
     * spawn is triggered.
     *
     * @return list of applicable commands
     */
    List<String> getCommands();

    /**
     * Set the list of commands executed
     * by spawners of this type when a
     * spawn is triggered.
     *
     * @param commands list of applicable commands
     */
    void setCommands(List<String> commands);

    /**
     * Get the tick rate in which is used
     * to calculate the spawn delay.
     *
     * @return tick rate
     */
    String getTickRate();

    /**
     * Set the tick rate in which is used
     * to calculate the spawn delay
     *
     * @param tickRate tick rate
     */
    void setTickRate(String tickRate);

    /**
     * Whether or not complicated particles effects
     * are constant or effect only boosted spawners.
     *
     * @return active particle use state
     */
    boolean isParticleEffectBoostedOnly();

    /**
     * Set whether or not complicated particles effects
     * are constant or effect only boosted spawners.
     *
     * @param boostedOnly true if complicated particles
     *                    will only appear for boosted
     *                    spawners, false otherwise
     */
    void setParticleEffectBoostedOnly(boolean boostedOnly);

}