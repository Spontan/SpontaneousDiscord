package spontanicus.discord.interactions;

public enum DiscordCommandArgumentType {

    STRING(3),
    INTEGER(4),
    BOOLEAN(5),
    USER(6),
    CHANNEL(7);

    private final int id;
    DiscordCommandArgumentType(int id) {
        this.id = id;
    }

    /**
     * Gets the internal Discord ID for this argument type.
     * @return the internal Discord ID.
     */
    public int getId() {
        return id;
    }
}
