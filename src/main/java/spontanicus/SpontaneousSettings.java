package spontanicus;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import org.json.JSONObject;
import util.ConfigParser;
import util.ParameterMap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SpontaneousSettings { //TODO: Make config file
    private final static Logger logger = Logger.getLogger("SpontaneousDiscord");

    private final List<Pattern> discordFilters = new ArrayList<>();
    private final File configFile;

    ParameterMap parameterValues;

    public SpontaneousSettings(File configFile) throws IOException {
        this.configFile = configFile;
        reloadConfig();
    }

    public String getOnlineStatus(){
        return parameterValues.get(ParameterMap.ONLINE_STATUS);
    }

    public ActivityType getActivityType(){
        return activityTypeFromName(parameterValues.get(ParameterMap.ACTIVITY_TYPE));
    }

    public String getActivity(){
        return parameterValues.get(ParameterMap.ACTIVITY);
    }

    public long getPrimaryChannelId(){
        return Long.parseLong(parameterValues.get(ParameterMap.CHANNEL_ID));
    }

    public String getMessageChannel(String key){
        return ""; //TODO: remove or do something with it
    }

    public long getChannelId(String key){
        return getPrimaryChannelId();
    }

    public long getGuildId(){
        return Long.parseLong(parameterValues.get(ParameterMap.GUILD_ID));
    }

    public String getBotToken(){
        return parameterValues.get(ParameterMap.BOT_TOKEN);
    }

    public boolean isShowBotMessages(){
        return true;
    }

    public boolean isShowWebhookMessages(){
        return true;
    }

    public boolean isShowDiscordAttachments(){
        return true;
    }

    public boolean isChatFilterNewlines(){
        return false;
    }

    public int getChatDiscordMaxLength(){
        return 1000;
    }

    public void reloadConfig() throws IOException {
        if(configFile==null) {
            throw new NullPointerException("Config file is not set before trying to load settings");
        }
        if(!configFile.isFile()){
            try {
                Files.copy(new FileInputStream(getConfigTemplate()), Path.of(configFile.toURI()));
            } catch (Exception e) {
                throw new IOException("Could not create default config file", e);
            }
        }

        ConfigParser parser = new ConfigParser();

        parameterValues = parser.parseConfigFile(configFile.getPath());
    }

    public List<Pattern> getDiscordFilters(){
        return discordFilters;
    }

    public static ActivityType activityTypeFromName(String activityName) {
        ActivityType[] var1 = Activity.ActivityType.values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            ActivityType activityType = var1[var3];
            if (activityType.name().equalsIgnoreCase(activityName)) {
                return activityType;
            }
        }

        return ActivityType.PLAYING;
    }

    private File getConfigTemplate(){
        return null;
    }
}
