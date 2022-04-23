package spontanicus;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SpontaneousSettings { //TODO: Make config file
    private final static Logger logger = Logger.getLogger("SpontaneousDiscord");

    private final Map<String, Long> nameToChannelIdMap = new HashMap<>();
    private final Map<Long, List<String>> channelIdToNamesMap = new HashMap<>();
    private final List<Pattern> discordFilters = new ArrayList<>();

    public SpontaneousSettings() {
        reloadConfig();
    }

    public String getOnlineStatus(){
        return "ONLINE";
    }

    public ActivityType getActivityType(){
        return activityTypeFromName("watching");
    }

    public String getActivity(){
        return "all of your streams!";
    }

    public long getPrimaryChannelId(){
        return 0L;
    }

    public String getMessageChannel(String key){
        return ""; //TODO: remove or do something with it
    }

    public long getChannelId(String key){
        return getPrimaryChannelId();
    }

    public long getGuildId(){
        return 0L;
    }

    public String getBotToken(){
        return "";
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

    public void reloadConfig() {
        nameToChannelIdMap.clear();
        channelIdToNamesMap.clear();
        Map<String, String> section = new HashMap<>();
        section.put("ALL", String.valueOf(getPrimaryChannelId()));
        for (Map.Entry<String, String> entry : section.entrySet()) {
            try{
                final long value = Long.parseLong(entry.getValue());
                nameToChannelIdMap.put(entry.getKey(), value);
                channelIdToNamesMap.computeIfAbsent(value, o -> new ArrayList<>()).add(entry.getKey());
            }
            catch(NumberFormatException e){
                logger.warning("Channel ID: " + entry.getValue() + " for channel " + entry.getKey() + " is not a number");
            }
        }
    }

    public List<Pattern> getDiscordFilters(){
        return discordFilters;
    }

    public List<String> getKeysFromChannelId(long channelId){
        return channelIdToNamesMap.get(channelId);
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
}
