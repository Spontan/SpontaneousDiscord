package spontanicus.discord.interactions.commands;

import spontanicus.discord.SpontaneousSettings;
import spontanicus.discord.interactions.DiscordCommand;
import spontanicus.discord.interactions.DiscordCommandEvent;

import java.io.*;
import java.nio.file.Files;
import java.util.logging.Logger;

public class Restart7DaysServerCommand extends DiscordCommand {
    private final static Logger logger = Logger.getLogger("SpontaneousDiscord");

    public Restart7DaysServerCommand(SpontaneousSettings settings){
        super(settings, "restart_7days_server", "Restarts the 7 Days to Die server");
    }

    @Override
    public void onCommand(DiscordCommandEvent event) {
        File adminList = new File("admins");
        if(!adminList.exists()){
            try {
                adminList.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                event.reply("No admins file found and can not be created");
            }
            event.reply("No admins file found, created an empty file. Add your discord ID there");
            return;
        }

        try {
            if(!Files.lines(adminList.toPath()).anyMatch(entry -> event.getMember().getId().equals(entry))){
                event.reply("You don't have permission to execute admin commands. " +
                        "Your discord user ID needs to be added to the admins list");
                return;
            }
            if(restart7DaysServer()){
                event.reply("Server is restarting!");
            }
            else{
                event.reply("Server restart failed");
            }

        } catch (IOException e) {
            e.printStackTrace();
            event.reply("Could not open admins file, aborting");
        }
    }

    private boolean restart7DaysServer() throws IOException {
        String keyFile = getSettings().getPrivateKeyFile();
        String serverAddress = getSettings().get7DaysServerAddress();

        BufferedReader in = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("ssh -i " + keyFile + " ubuntu@" + serverAddress + " 'cd 7daysded; ./restart.sh'").getInputStream()));
        while (in.ready()) {
            String s = in.readLine();
            logger.info(s);
            if("Server restarted successfully!".equals(s)){
                return true;
            }
        }
        return false;
    }
}
