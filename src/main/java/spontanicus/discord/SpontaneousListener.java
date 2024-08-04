package spontanicus.discord;

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceStreamEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import spontanicus.users.User;
import spontanicus.users.UserCache;

import java.text.MessageFormat;
import java.util.Map;
import java.util.logging.Logger;

public class SpontaneousListener extends ListenerAdapter {
    private final static Logger logger = Logger.getLogger("SpontaneousDiscord");

    private final SpontaneousBotService controller;

    public SpontaneousListener(SpontaneousBotService controller) {
        this.controller = controller;
    }

    /*@Override
    public void onMessageReceived(MessageReceivedEvent event){
        logger.info(event.getAuthor().getName() + " sent a message to channel "
                + event.getChannel().getName() + ": \"" + event.getMessage().getContentRaw() + "\"");
        if(!event.isFromGuild())
            return;
        if(event.getAuthor().isBot())
            return;
        if(event.getChannel().getId().equals(String.valueOf(controller.getSettings().getPrimaryChannelId()))){
            controller.sendMessage("Hallo " + event.getMember().getEffectiveName(), true);
        }
    }*/

    @Override
    public void onGuildVoiceStream(GuildVoiceStreamEvent event){
        if(event.isStream()){
            logger.info(DiscordUtil.getCurrentDateTimeForLogging() + " | " + event.getMember().getEffectiveName() + " has started a stream");
            UserCache users = UserCache.getInstance();
            User userData = users.getUser(event.getMember().getIdLong());
            String message = DiscordUtil.formatMessage(userData.getNotificationMessage(), event.getMember());

            if(userData.isNotifyAutomatically()) {
                controller.sendMessage(message, true);
            }

            for(Map.Entry<Long, User> userEntry: users.getUserData().entrySet()){
                User user = userEntry.getValue();
                if(user.isWhisperModeEnabled() && event.getMember().getIdLong() != user.getId())
                    controller.sendPrivateMessage(user.getId(), message);
            }
        }
        else{
            logger.info(DiscordUtil.getCurrentDateTimeForLogging() + " | " + event.getMember().getEffectiveName() + " has ended a stream");
        }
    }

    /**
     * Sanitizes text to be sent to Discord, escaping any Markdown syntax.
     */
    public String sanitizeDiscordMarkdown(String message) {
        if (message == null) {
            return null;
        }

        return message.replace("*", "\\*")
                .replace("~", "\\~")
                .replace("_", "\\_")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("|", "\\|");
    }

    /**
     * Shortcut method allowing for use of varags in {@link MessageFormat} instances
     */
    public String formatMessage(MessageFormat format, Object... args) {
        return format.format(args);
    }
}
