package com.songoda.epicspawners.api.utils;

import org.bukkit.Location;

/**
 * A more specific implementation of {@link ProtectionPluginHook} used internally by
 * EpicSpawners to retain more information about default hooks. Often times this
 * interface is not recommended over the ProtectionPluginHook interface as its methods
 * will not often be used by implementation, though they are available if more information
 * is desired. It is, however, recommended to use the former
 * 
 * @author Parker Hawke - 2008Choco
 */
public interface ClaimableProtectionPluginHook extends ProtectionPluginHook {

    /**
     * Check whether the provided location is in the claim with the given String ID
     * 
     * @param location the location to check
     * @param id the ID of the claim to check
     * 
     * @return true if the location is within the claim, false otherwise or if the
     * claim ID does not exist
     */
    public boolean isInClaim(Location location, String id);

    /**
     * Get the ID of the claim with the given name. Often times this is unnecessary
     * as unique IDs are not provided by a claim implementation, though for plugins
     * such as factions, the passed parameter is the name of the faction and the
     * returned String is its unique ID
     * 
     * @param name the name of the claim to check
     * 
     * @return the unique String ID. null if no claim exists
     */
    public String getClaimID(String name);

}