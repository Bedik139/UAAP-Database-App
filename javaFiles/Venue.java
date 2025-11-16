public enum Venue {

    MALL_OF_ASIA_ARENA("Mall of Asia Arena", 15000),
    SMART_ARANETA_COLISEUM("Smart Araneta Coliseum", 16000),
    PHILSPORTS_ARENA("PhilSports Arena", 10000),
    YNARES_CENTER("Ynares Center", 7000),
    FILOIL_ECOOIL_CENTRE("Filoil EcoOil Centre", 6000);

    private final String displayName;
    private final int capacity;

    Venue(String displayName, int capacity) {
        this.displayName = displayName;
        this.capacity = capacity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Venue fromName(String name) {
        if (name == null) {
            return null;
        }
        for (Venue venue : values()) {
            if (venue.displayName.equalsIgnoreCase(name.trim())) {
                return venue;
            }
        }
        return null;
    }
}
