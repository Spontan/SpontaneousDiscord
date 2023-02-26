package spontanicus.util;

import java.util.HashMap;
import java.util.Map;

public class ParameterMap {
    public static String GUILD_ID = "guildId";
    public static String BOT_TOKEN = "botToken";
    public static String LIVE_MESSAGE = "liveMessage";
    public static String ONLINE_STATUS = "onlineStatus";
    public static String ACTIVITY_TYPE = "activityType";
    public static String ACTIVITY = "activity";
    public static String CHANNEL_ID = "channelId";
    public static String USER_SETTINGS = "userSettings";

    private Map<String, String> parameterValues = new HashMap<>();

    public void setToDefaults(){
        parameterValues.put(LIVE_MESSAGE, "{user} hat einen Stream gestartet! <3");
        parameterValues.put(ONLINE_STATUS, "ONLINE");
        parameterValues.put(ACTIVITY_TYPE, "watching");
        parameterValues.put(ACTIVITY, "all your streams!");
        parameterValues.put(USER_SETTINGS, "notification.db");
    }

    public ParameterMap(){
        setToDefaults();
    }

    public void set(String parameterName, String parameterValue){
        parameterValues.put(parameterName, parameterValue);
    }

    public String get(String parameterName){
        return parameterValues.get(parameterName);
    }
}
