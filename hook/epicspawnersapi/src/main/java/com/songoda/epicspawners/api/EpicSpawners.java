package com.songoda.epicspawners.api;

import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerManager;
import org.bukkit.inventory.ItemStack;

public interface EpicSpawners {

    SpawnerManager getSpawnerManager();

    SpawnerData getSpawnerDataFromItem(ItemStack item);

}
