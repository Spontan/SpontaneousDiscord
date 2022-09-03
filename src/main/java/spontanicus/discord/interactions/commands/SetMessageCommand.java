package spontanicus.discord.interactions.commands;

import spontanicus.discord.*;
import spontanicus.discord.interactions.DiscordCommand;
import spontanicus.discord.interactions.DiscordCommandArgument;
import spontanicus.discord.interactions.DiscordCommandArgumentType;
import spontanicus.discord.interactions.DiscordCommandEvent;
import spontanicus.users.User;
import spontanicus.users.UserCache;

public class SetMessageCommand extends DiscordCommand {
    public SetMessageCommand(SpontaneousSettings settings){
        super(settings, "set_message", "Set a personalized live message");
        addArgument(new DiscordCommandArgument("message", "The message that should be displayed when you go live", DiscordCommandArgumentType.STRING, true));
    }

    @Override
    public void onCommand(DiscordCommandEvent event) {
        UserCache users = UserCache.getInstance();
        User userData = users.getUser(Long.parseLong(event.getMember().getId()));
        String message = event.getStringArgument("message");

        if(message == null || message.isEmpty()){
            event.reply("Notification message can not be empty");
            return;
        }

        if(!userData.getNotificationMessage().equals(message)){
            userData.setNotificationMessage(message);
            users.updateUser(userData);
        }

        event.reply("Notification message updated");
    }
}
