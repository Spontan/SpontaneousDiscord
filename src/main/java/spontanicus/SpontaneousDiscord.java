package spontanicus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import spontanicus.discord.SpontaneousBotService;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class SpontaneousDiscord extends JavaPlugin{
    private static final Logger logger = Logger.getLogger("SpontaneousDiscord");
    private static SpontaneousBotService discordBot;

    public static void main(String[] args){
        try {
            startDiscordBot(new File("config.ini"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(ChatColor.RED + "Disabled " + this.getName());
    }

    @Override
    public void onEnable() {
        Bukkit.getLogger().info(ChatColor.GREEN + "Enabled " + this.getName());
        try {
            Bukkit.getLogger().info("Load config from: " + this.getDataFolder() + "/config.ini");
            startDiscordBot(new File(this.getDataFolder(), "config.ini"));
        } catch (IOException e) {
            Bukkit.getLogger().severe(ChatColor.RED + "Error while loading " + this.getName());
        }
    }



    public static void startDiscordBot(File configFile) throws IOException {
        if (discordBot == null) {
            discordBot = new SpontaneousBotService(configFile);

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
