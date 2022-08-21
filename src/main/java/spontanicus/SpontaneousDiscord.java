package spontanicus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spontanicus.discord.SpontaneousBotService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;

public class SpontaneousDiscord extends JavaPlugin{
    private static final Logger logger = Logger.getLogger("SpontaneousDiscord");
    private SpontaneousBotService discordBot;

    public static void main(String[] args){

    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(ChatColor.RED + "Disabled " + this.getName());
    }

    @Override
    public void onEnable() {
        Bukkit.getLogger().info(ChatColor.GREEN + "Enabled " + this.getName());
        try {
            Bukkit.getLogger().info("Load config from: " + this.getDataFolder() + "\\config.yml");
            startDiscordBot();
        } catch (IOException e) {
            Bukkit.getLogger().severe(ChatColor.RED + "Error while loading " + this.getName());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Bukkit.getLogger().fine("onCommand: sender: " + sender.getName() + ", command: " + command.getName() +
                ", aliases: " + printList(command.getAliases()) + ", label: " + label);

        if("spontaneousbot".equalsIgnoreCase(command.getName())){
            return onSpontaneousBotCommand(sender, args);
        }
        return false;
    }

    private boolean onSpontaneousBotCommand(CommandSender sender, String[] args) {
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
    }

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

    @Override
    public @Nullable PluginCommand getCommand(@NotNull String name) {
        return super.getCommand(name);
    }

    private void startDiscordBot() throws IOException {
        createDiscordBot();
        try {
            discordBot.startup();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "discordErrorLogin" + new Object[]{e.getMessage()});
            e.printStackTrace();

            discordBot.shutdown();
        }
    }

    private void createDiscordBot() throws IOException {
        this.saveDefaultConfig();

        if (discordBot == null) {
            discordBot = new SpontaneousBotService(new File(getDataFolder(), "config.yml"));
        }
    }
}
