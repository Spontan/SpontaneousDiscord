package spontanicus.discord.interactions.commands;

import spontanicus.discord.SpontaneousSettings;
import spontanicus.discord.interactions.DiscordCommand;
import spontanicus.discord.interactions.DiscordCommandEvent;
import spontanicus.users.User;
import spontanicus.users.UserCache;

public class ToggleWhisperModeCommand extends DiscordCommand {
    public ToggleWhisperModeCommand(SpontaneousSettings settings){
            super(settings, "toggle_whisper", "Enables or disables if the bot will send you a PM whenever someone else goes live.");
        }

    @Override
    public void onCommand(DiscordCommandEvent event) {
        UserCache users = UserCache.getInstance();
        User userData = users.getUser(Long.parseLong(event.getMember().getId()));
        String mode = event.getStringArgument("mode");

        boolean enable = !userData.isWhisperModeEnabled();

        userData.setWhisperModeEnabled(enable);
        users.updateUser(userData);

        if(enable)
            event.reply("Whisper mode was enabled for you. When someone goes live you will receive a notification from now on.");
        else
            event.reply("Whisper mode was disabled for you. You will no longer get notifications when people go live");
    }
}

