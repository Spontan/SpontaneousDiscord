package spontanicus;

/**
 * Indicates the type of message being sent and its literal channel name used in the config.
 */
public final class SpontaneousEventType {
    private final String key;
    private final boolean player;

    /**
     * Creates a {@link SpontaneousEventType} which will send channels to the specified channel key.
     * <p>
     * The message type key may only contain: lowercase letters, numbers, and dashes.
     * @param key The channel key defined in the {@code message-types} section of the config.
     */
    public SpontaneousEventType(final String key) {
        this(key, false);
    }

    /**
     * Internal constructor used by EssentialsX Discord
     */
    private SpontaneousEventType(String key, boolean player) {
        if (!key.matches("^[a-z0-9-]*$")) {
            throw new IllegalArgumentException("Key must match \"^[a-z0-9-]*$\"");
        }
        this.key = key;
        this.player = player;
    }

    /**
     * Gets the key used in {@code message-types} section of the config.
     * @return The config key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Checks if this message type should be beholden to player-specific config settings.
     * @return true if message type should be beholden to player-specific config settings.
     */
    public boolean isPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return key;
    }

    /**
     * Default {@link SpontaneousEventType MessageTypes} provided and documented by EssentialsX Discord.
     */
    public static final class DefaultTypes {
        public final static SpontaneousEventType JOIN = new SpontaneousEventType("join", true);
        public final static SpontaneousEventType LEAVE = new SpontaneousEventType("leave", true);
        public final static SpontaneousEventType CHAT = new SpontaneousEventType("chat", true);
        public final static SpontaneousEventType DEATH = new SpontaneousEventType("death", true);
        public final static SpontaneousEventType AFK = new SpontaneousEventType("afk", true);
        public final static SpontaneousEventType ADVANCEMENT = new SpontaneousEventType("advancement", true);
        public final static SpontaneousEventType ACTION = new SpontaneousEventType("action", true);
        public final static SpontaneousEventType SERVER_START = new SpontaneousEventType("server-start", false);
        public final static SpontaneousEventType SERVER_STOP = new SpontaneousEventType("server-stop", false);
        public final static SpontaneousEventType KICK = new SpontaneousEventType("kick", false);
        public final static SpontaneousEventType MUTE = new SpontaneousEventType("mute", false);
        private final static SpontaneousEventType[] VALUES = new SpontaneousEventType[]{JOIN, LEAVE, CHAT, DEATH, AFK, ADVANCEMENT, ACTION, SERVER_START, SERVER_STOP, KICK, MUTE};

        /**
         * Gets an array of all the default {@link SpontaneousEventType MessageTypes}.
         * @return An array of all the default {@link SpontaneousEventType MessageTypes}.
         */
        public static SpontaneousEventType[] values() {
            return VALUES;
        }
    }
}
