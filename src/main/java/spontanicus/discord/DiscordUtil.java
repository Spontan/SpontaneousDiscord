package spontanicus.discord;

import com.google.common.collect.ImmutableList;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class DiscordUtil {
    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    public static final List<Message.MentionType> NO_GROUP_MENTIONS;
    private static final String NOTIFICATION_PREFIX = "{user.name} hat einen Stream gestartet <3";

    static {
        final ImmutableList.Builder<Message.MentionType> types = new ImmutableList.Builder<>();
        types.add(Message.MentionType.USER);
        types.add(Message.MentionType.CHANNEL);
        types.add(Message.MentionType.EMOTE);
        NO_GROUP_MENTIONS = types.build();
    }

    public static String formatMessage(String message, Member member) {
        if(message == null || message.isEmpty()){
            message = NOTIFICATION_PREFIX;
        }else{
            message = NOTIFICATION_PREFIX + "\n" + message;
        }

        return message.replace("{user.name}", member.getEffectiveName());
    }

    public static String getCurrentDateTimeForLogging(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }
}
