package spontanicus.discord;

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceStreamEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.text.MessageFormat;
import java.util.logging.Logger;

public class SpontaneousListener extends ListenerAdapter {
    private final static Logger logger = Logger.getLogger("SpontaneousDiscord");

    private final SpontaneousBotService controller;

    public SpontaneousListener(SpontaneousBotService controller) {
        this.controller = controller;
    }

    /*@Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() && !event.isWebhookMessage() && (!controller.getSettings().isShowBotMessages() || event.getAuthor().getId().equals(controller.getJda().getSelfUser().getId()))) {
            return;
        }

        if (event.isWebhookMessage() && (!controller.getSettings().isShowWebhookMessages())) {
            return;
        }

        // Get list of channel names that have this channel id mapped
        final List<String> keys = controller.getSettings().getKeysFromChannelId(event.getChannel().getIdLong());
        if (keys == null || keys.size() == 0) {
            if (controller.isDebug()) {
                logger.log(Level.INFO, "Skipping message due to no channel keys for id " + event.getChannel().getIdLong() + "!");
            }
            return;
        }

        final User user = event.getAuthor();
        final Member member = event.getMember();
        final String effectiveName = member == null ? event.getAuthor().getName() : member.getEffectiveName();
        final Message message = event.getMessage();

        if (!controller.getSettings().getDiscordFilters().isEmpty()) {
            for (final Pattern pattern : controller.getSettings().getDiscordFilters()) {
                if (pattern.matcher(message.getContentDisplay()).find()) {
                    if (controller.isDebug()) {
                        logger.log(Level.INFO, "Skipping message " + message.getId() + " with content, \"" + message.getContentDisplay() + "\" as it matched the filter!");
                    }
                    return;
                }
            }
        }

        final StringBuilder messageBuilder = new StringBuilder(message.getContentDisplay());
        if (controller.getSettings().isShowDiscordAttachments()) {
            for (final Message.Attachment attachment : message.getAttachments()) {
                messageBuilder.append(" ").append(attachment.getUrl());
            }
        }

        // Strip message
        final String strippedMessage = StringUtils.abbreviate(
                messageBuilder.toString()
                        .replace(controller.getSettings().isChatFilterNewlines() ? '\n' : ' ', ' ')
                        .trim(), controller.getSettings().getChatDiscordMaxLength());

        String finalMessage = strippedMessage;

        // Don't send blank messages
        if (finalMessage.trim().length() == 0) {
            if (controller.isDebug()) {
                logger.log(Level.INFO, "Skipping finalized empty message " + message.getId());
            }
            return;
        }
    }*/

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        logger.info(event.getAuthor().getName() + " sent a message to channel "
                + event.getChannel().getName() + ": \"" + event.getMessage().getContentRaw() + "\"");
        if(!event.isFromGuild())
            return;
        if(event.getAuthor().isBot())
            return;
        if(event.getChannel().getId().equals(String.valueOf(controller.getSettings().getPrimaryChannelId()))){
            //controller.sendMessage("Hallo " + event.getMember().getEffectiveName(), true);
        }
    }

    @Override
    public void onGuildVoiceStream(GuildVoiceStreamEvent event){
        if(event.isStream()){
            logger.info(event.getMember().getEffectiveName() + " has started a stream");
            controller.sendMessage(event.getMember().getEffectiveName() + " hat einen Stream gestartet <3", true);
        }
        else{
            logger.info(event.getMember().getEffectiveName() + " has ended a stream");
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
