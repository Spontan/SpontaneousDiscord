package spontanicus.discord.interactions.commands;

import spontanicus.discord.*;
import spontanicus.discord.interactions.DiscordCommand;
import spontanicus.discord.interactions.DiscordCommandArgument;
import spontanicus.discord.interactions.DiscordCommandArgumentType;
import spontanicus.discord.interactions.DiscordCommandEvent;
import spontanicus.users.User;
import spontanicus.users.UserCache;

public class SetModeCommand extends DiscordCommand {
    public SetModeCommand(SpontaneousSettings settings){
        super(settings, "set_mode", "Set whether you want the bot to send a live notification automatically");
        addArgument(new DiscordCommandArgument("mode", "Can be either \"automatic\" or \"manual\"...", DiscordCommandArgumentType.STRING, true));
    }

    @Override
    public void onCommand(DiscordCommandEvent event) {
        UserCache users = UserCache.getInstance();
        User userData = users.getUser(Long.parseLong(event.getMember().getId()));
        String mode = event.getStringArgument("mode");

        boolean newValueIsAutomatic = "automatic".equalsIgnoreCase(mode);
        if(!newValueIsAutomatic && !"manual".equalsIgnoreCase(mode)){
            event.reply("Notification mode has to be set to either automatic or manual");
            return;
        }

        if(userData.isNotifyAutomatically() != newValueIsAutomatic){
            userData.setNotifyAutomatically(newValueIsAutomatic);
            users.updateUser(userData);
        }

        event.reply("Notification mode updated");
    }
}
