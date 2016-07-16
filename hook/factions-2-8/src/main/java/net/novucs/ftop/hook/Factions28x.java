package net.novucs.ftop.hook;

import java.util.UUID;

public class Factions28x implements FactionsHook {

    @Override
    public UUID getFactionAt(String worldName, int chunkX, int chunkZ) {
        return null;
    }

    @Override
    public String getFactionName(UUID factionId) {
        return null;
    }
}
