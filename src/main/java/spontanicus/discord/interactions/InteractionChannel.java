package spontanicus.discord.interactions;

import net.dv8tion.jda.api.entities.GuildChannel;

public class InteractionChannel {
    private final GuildChannel channel;

    public InteractionChannel(GuildChannel channel) {
        this.channel = channel;
    }

    public String getName() {
        return channel.getName();
    }

    public GuildChannel getJdaObject() {
        return channel;
    }

    public String getId() {
        return channel.getId();
    }
}
