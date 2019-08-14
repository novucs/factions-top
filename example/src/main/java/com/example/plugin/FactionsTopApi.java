/*
 * Copyright (c) 2017 novucs <contact@novucs.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.example.plugin;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.WorthType;
import net.novucs.ftop.entity.FactionWorth;
import net.novucs.ftop.manager.WorthManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

/**
 * Place this file anywhere in your plugin and call these helper methods
 * whenever they suit you best.
 */
public final class FactionsTopApi {

    private FactionsTopApi() {
        throw new IllegalStateException();
    }

    /**
     * Get the current loaded {@link FactionsTopPlugin}.
     *
     * @return the {@link FactionsTopPlugin}, if loaded.
     */
    public static Optional<Plugin> getPlugin() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("FactionsTop");
        if (plugin != null && plugin.isEnabled()) {
            try {
                return plugin instanceof FactionsTopPlugin ? Optional.of(plugin) : Optional.empty();
            } catch (NoClassDefFoundError ignore) {
            }
        }
        return Optional.empty();
    }

    /**
     * Get a players faction ID.
     *
     * @param member the faction member.
     * @return the players faction ID.
     */
    public static Optional<String> getId(Player member) {
        return getPlugin().map(plugin -> ((FactionsTopPlugin) plugin).getFactionsHook().getFaction(member));
    }

    /**
     * Get the claiming faction ID of a location.
     *
     * @param location the location the faction has claimed.
     * @return the faction ID that has claimed this location.
     */
    public static Optional<String> getId(Location location) {
        return getPlugin().map(plugin -> ((FactionsTopPlugin) plugin).getFactionsHook().getFactionAt(location.getBlock()));
    }

    /**
     * Get the {@link Worth} for a particular faction.
     *
     * @param factionId the faction ID to look for.
     * @return the faction worth.
     */
    public static Optional<Worth> getWorth(String factionId) {
        return getPlugin().map(plugin -> ((FactionsTopPlugin) plugin).getWorthManager().getWorth(factionId)).map(Worth::new);
    }

    /**
     * Get the rank of a faction.
     *
     * @param factionId the faction ID.
     * @return the 1-indexed rank of this faction.
     */
    public static Optional<Integer> getRank(String factionId) {
        return getPlugin().map(plugin -> {
            WorthManager manager = ((FactionsTopPlugin) plugin).getWorthManager();
            FactionWorth worth = manager.getWorth(factionId);
            int index = manager.getOrderedFactions().indexOf(worth);
            return index < 0 ? null : index + 1;
        });
    }

    /**
     * Represents the worth value for a faction.
     */
    public static class Worth {
        private final FactionWorth worth;

        private Worth(FactionWorth worth) {
            this.worth = worth;
        }

        /**
         * Gets the faction name.
         *
         * @return the faction name.
         */
        public String getName() {
            return worth.getName();
        }

        /**
         * Gets the combined total worth of all materials, spawners and
         * liquid-economy balances this faction owns.
         *
         * @return the total.
         */
        public double getTotal() {
            return worth.getTotalWorth();
        }

        /**
         * Gets the total worth for a specific worth type.
         *
         * @param type the type of worth to look for.
         * @return the total of the provided worth type.
         */
        public double getTotal(WorthType type) {
            return worth.getWorth(type);
        }

        /**
         * Gets the total number of spawners the faction owns.
         *
         * @return the total number of spawners.
         */
        public int getTotalSpawners() {
            return worth.getTotalSpawnerCount();
        }

        /**
         * Gets the spawner count for a particular spawner type.
         *
         * @param type the spawner type.
         * @return the number of this type of spawner the faction owns.
         */
        public int getSpawnerCount(EntityType type) {
            return worth.getSpawners().get(type);
        }

        /**
         * Gets the material count for a particular material.
         *
         * @param material the material type.
         * @return the number of this type of material the faction owns.
         */
        public int getMaterialCount(Material material) {
            return worth.getMaterials().get(material);
        }
    }
}
