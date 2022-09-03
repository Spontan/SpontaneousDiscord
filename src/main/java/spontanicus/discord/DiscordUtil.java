package spontanicus.discord;

import com.google.common.collect.ImmutableList;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class DiscordUtil {

    public static final List<Message.MentionType> NO_GROUP_MENTIONS;

    static {
        final ImmutableList.Builder<Message.MentionType> types = new ImmutableList.Builder<>();
        types.add(Message.MentionType.USER);
        types.add(Message.MentionType.CHANNEL);
        types.add(Message.MentionType.EMOTE);
        NO_GROUP_MENTIONS = types.build();
    }

    public static String formatMessage(String message, Member member) {
        return message.replace("{user.name}", member.getEffectiveName());
    }
}
