import spontanicus.SpontaneousBotService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Starter {
    private static final Logger logger = Logger.getLogger("SpontaneousDiscord");
    private static SpontaneousBotService discordBot;

    public static void main(String[] args){
        try {
            startDiscordBot();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void startDiscordBot() throws IOException {
        if (discordBot == null) {
            discordBot = new SpontaneousBotService();

            try {
                discordBot.startup();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "discordErrorLogin" + new Object[]{e.getMessage()});
                e.printStackTrace();

                discordBot.shutdown();
            }
        }
    }
}
