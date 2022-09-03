package spontanicus.discord.interactions.commands;

import spontanicus.discord.*;
import spontanicus.discord.interactions.DiscordCommand;
import spontanicus.discord.interactions.DiscordCommandArgument;
import spontanicus.discord.interactions.DiscordCommandArgumentType;
import spontanicus.discord.interactions.DiscordCommandEvent;
import spontanicus.users.User;
import spontanicus.users.UserCache;

public class SendNotificationCommand  extends DiscordCommand {
    private final SpontaneousBotService botService;

    public SendNotificationCommand(SpontaneousBotService botService){
        super(botService.getSettings(), "send_notification", "Dispatches a live notification");
        this.botService = botService;
        addArgument(new DiscordCommandArgument("message", "The message to use. If none is provided, your default message is used instead", DiscordCommandArgumentType.STRING, false));
    }

    @Override
    public void onCommand(DiscordCommandEvent event) {
        UserCache users = UserCache.getInstance();
        User userData = users.getUser(Long.parseLong(event.getMember().getId()));
        String message = event.getStringArgument("message");

        if(message == null || message.isEmpty()){
            message = userData.getNotificationMessage();
        }

        botService.sendMessage(DiscordUtil.formatMessage(message, event.getMember().getJdaObject()), true);

        event.reply("Notification message sent!");
    }
}
