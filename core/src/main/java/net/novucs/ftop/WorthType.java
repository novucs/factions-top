package net.novucs.ftop;

public enum WorthType {

    CHEST,
    LIQUID,
    SPAWNER,
    BLOCK;

    private static final WorthType[] PLACED = {CHEST, SPAWNER, BLOCK};

    public static WorthType[] getPlaced() {
        return PLACED;
    }

    public static boolean isPlaced(WorthType worthType) {
        switch (worthType) {
            case LIQUID:
                return false;
        }
        return true;
    }
}
