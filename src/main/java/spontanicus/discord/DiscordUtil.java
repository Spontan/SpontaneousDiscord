package spontanicus.discord;

import com.google.common.collect.ImmutableList;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class DiscordUtil {

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
}
