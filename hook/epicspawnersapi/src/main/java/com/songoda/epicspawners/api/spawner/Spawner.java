package com.songoda.epicspawners.api.spawner;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface Spawner {

    CreatureSpawner getCreatureSpawner();

    Collection<SpawnerStack> getSpawnerStacks();

    @Deprecated
    boolean stack(Player player, String type, int amt);
}
