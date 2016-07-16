package net.novucs.ftop;

import java.util.UUID;

public interface FactionsHook {

    UUID getFactionAt(ChunkPos chunkPos);

    String getFactionName(UUID factionId);

}
