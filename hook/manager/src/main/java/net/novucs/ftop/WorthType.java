package net.novucs.ftop;

public enum WorthType {

    CHEST,
    PLAYER_BALANCE,
    FACTION_BALANCE,
    SPAWNER,
    BLOCK;

    private static final WorthType[] PLACED = {CHEST, SPAWNER, BLOCK};

    public static WorthType[] getPlaced() {
        return PLACED;
    }

    public static boolean isPlaced(WorthType worthType) {
        switch (worthType) {
            case PLAYER_BALANCE:
            case FACTION_BALANCE:
                return false;
        }
        return true;
    }
}
