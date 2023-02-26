package spontanicus;

import spontanicus.discord.SpontaneousBotService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpontaneousDiscord{
    private static final Logger logger = Logger.getLogger("SpontaneousDiscord");
    private static SpontaneousBotService discordBot;

    public static void main(String[] args){
        try {
            startDiscordBot();
        } catch (IOException e) {
            logger.severe("Error while starting bot :( Shutting down");
            e.printStackTrace();
        }
    }

    public void shutdown() {
        logger.info("Shutting down discord bot");
        discordBot.shutdown();
    }

    //TODO: enable console commands for bot control
 /*   private boolean onConsoleCommand(String[] args) {
        if(args.length < 1 || "help".equalsIgnoreCase(args[0]) || "?".equalsIgnoreCase(args[0])){
            sender.sendMessage("Usage: /spontaneousBot [enable|disable|reload]");
            return true;
        }
        if(discordBot == null) {
            try {
                createDiscordBot();
            } catch (IOException e) {
                sender.sendMessage("Error while trying to create the bot");
                return false;
            }
        }

        if("enable".equalsIgnoreCase(args[0])){
            if(discordBot.isRunning()){
                sender.sendMessage("Bot is already running");
            }
            else{
                try {
                    discordBot.startup();
                    sender.sendMessage("Bot started");
                } catch (Exception e) {
                    sender.sendMessage("Error while trying to start the bot");
                    return false;
                }
            }
            return true;
        }

        if("disable".equalsIgnoreCase(args[0])){
            if(!discordBot.isRunning()){
                sender.sendMessage("Bot is not running");
            }
            else{
                discordBot.shutdown();
                sender.sendMessage("Bot has been stopped");
            }
            return true;
        }
        if("reload".equalsIgnoreCase(args[0])){
            try {
                discordBot.reloadConfig();
                sender.sendMessage("Config reloaded and bot restarted");
                return true;
            } catch (IOException e) {
                sender.sendMessage("Error while loading config");
                return false;
            } catch (LoginException e) {
                sender.sendMessage("Error while logging in bot");
                return false;
            } catch (Exception e) {
                sender.sendMessage("Error while starting bot");
                return false;
            }
        }
        return false;
    }*/

    private String printList(List<String> list) {
        String printString = "[";
        if(list.size() > 0) {
            printString += list.get(0);
            for (int i = 1; i < list.size(); i++)
                printString += ", " + list.get(i);
        }
        printString += "]";
        return printString;
    }

    private static void startDiscordBot() throws IOException {
        createDiscordBot();
        try {
            discordBot.startup();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "discordErrorLogin" + e.getMessage());
            e.printStackTrace();

            discordBot.shutdown();
        }
    }

    private static void createDiscordBot() throws IOException {
        if (discordBot == null) {
            discordBot = new SpontaneousBotService(new File("./"));
        }
    }
}
