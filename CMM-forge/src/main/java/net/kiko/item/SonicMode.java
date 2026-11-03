package net.kiko.item;

/** The modes of the sonic screwdriver. The order is the order sneak + right-click cycles through. */
public enum SonicMode {
    DISABLED("disabled"),
    REDSTONE("redstone"),     // red
    SCANNER("scanner"),       // green
    ANALYZER("analyzer"),     // teal
    HIGHLIGHT("highlight"),   // magenta
    ENVIRONMENT("environment"), // yellow
    LIGHT("light");           // white

    private final String key;

    SonicMode(String key) {
        this.key = key;
    }

    public String langKey() {
        return "item.kiko.sonic_screwdriver.mode." + key;
    }

    public SonicMode next() {
        SonicMode[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
