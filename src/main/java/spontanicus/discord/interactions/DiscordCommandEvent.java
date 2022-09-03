package spontanicus.discord.interactions;

import com.google.common.base.Joiner;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import spontanicus.discord.DiscordUtil;

public class DiscordCommandEvent {

    private final static Logger logger = Logger.getLogger("EssentialsDiscord");
    private final SlashCommandInteractionEvent event;
    private final InteractionMember member;
    private final List<String> replyBuffer = new ArrayList<>();

    public DiscordCommandEvent(final SlashCommandInteractionEvent jdaEvent) {
        this.event = jdaEvent;
        this.member = new InteractionMember(jdaEvent.getMember());
    }

    public void reply(String message) {
        //message = FormatUtil.stripFormat(message).replace("§", ""); // Don't ask
        replyBuffer.add(message);
        String reply = Joiner.on('\n').join(replyBuffer);
        reply = reply.substring(0, Math.min(Message.MAX_CONTENT_LENGTH, reply.length()));
        event.getHook().editOriginal(
                new MessageBuilder()
                        .setContent(reply)
                        .setAllowedMentions(DiscordUtil.NO_GROUP_MENTIONS).build())
                .queue(null, error -> logger.log(Level.SEVERE, "Error while editing command interaction response", error));
    }

    public InteractionMember getMember() {
        return member;
    }

    public String getStringArgument(String key) {
        final OptionMapping mapping = event.getOption(key);
        return mapping == null ? null : mapping.getAsString();
    }

    public Long getIntegerArgument(String key) {
        final OptionMapping mapping = event.getOption(key);
        return mapping == null ? null : mapping.getAsLong();
    }

    public Boolean getBooleanArgument(String key) {
        final OptionMapping mapping = event.getOption(key);
        return mapping == null ? null : mapping.getAsBoolean();
    }

    public InteractionMember getUserArgument(String key) {
        final OptionMapping mapping = event.getOption(key);
        return mapping == null ? null : new InteractionMember(mapping.getAsMember());
    }

    public InteractionChannel getChannelArgument(String key) {
        final OptionMapping mapping = event.getOption(key);
        return mapping == null ? null : new InteractionChannel(mapping.getAsGuildChannel());
    }

    public String getChannelId() {
        return event.getChannel().getId();
    }
}
