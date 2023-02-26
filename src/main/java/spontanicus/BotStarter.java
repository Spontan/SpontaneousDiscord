package spontanicus;

import spontanicus.discord.SpontaneousBotService;

import java.io.File;
import java.io.IOException;

public class BotStarter {
    private SpontaneousBotService discordBot;

    public static void main(String[] args){

    }

    private void createDiscordBot() throws IOException {
        saveDefaultConfig();

        if (discordBot == null) {
            discordBot = new SpontaneousBotService(getDataFolder());
        }
    }

    private void saveDefaultConfig(){

    }

    private File getDataFolder(){
        return new File("");
    }
}
