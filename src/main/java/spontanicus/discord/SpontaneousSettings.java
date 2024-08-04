package spontanicus.discord;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import spontanicus.util.ConfigParser;
import spontanicus.util.ParameterMap;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SpontaneousSettings {
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

    public String getUserSettings() {
        return parameterValues.get(ParameterMap.USER_SETTINGS);
    }

    public String get7DaysServerAddress(){
        return parameterValues.get(ParameterMap.SEVEN_DAYS_SERVER_ADDRESS);
    }

    public String getPrivateKeyFile(){
        return parameterValues.get(ParameterMap.PRIVATE_KEY_FILE);
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
                Files.copy(getConfigTemplate(), Path.of(configFile.toURI()));
            } catch (Exception e) {
                throw new IOException("Could not create default config file at: " + Path.of(configFile.toURI()), e);
            }
        }

        ConfigParser parser = new ConfigParser();

        parameterValues = parser.parseConfigFile(configFile.getPath());
    }

    public List<Pattern> getDiscordFilters(){
        return discordFilters;
    }

    public static ActivityType activityTypeFromName(String activityName) {
        ActivityType[] activityTypes = Activity.ActivityType.values();

        for (ActivityType activityType : activityTypes) {
            if (activityType.name().equalsIgnoreCase(activityName)) {
                return activityType;
            }
        }

        return ActivityType.PLAYING;
    }

    private InputStream getConfigTemplate() throws URISyntaxException {
        return getClass().getClassLoader().getResourceAsStream("config.yml");
    }
}
