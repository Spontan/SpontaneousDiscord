package spontanicus;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.google.common.collect.ImmutableList;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SpontaneousBotService{
    private final static Logger logger = Logger.getLogger("SpontaneousBot");

    public final static String ADVANCED_RELAY_NAME = "EssX Advanced Relay";

    private JDA jda;
    private Guild guild;
    private BaseGuildMessageChannel primaryChannel;
    private WebhookClient consoleWebhook;
    private String lastConsoleId;
    private final Map<String, WebhookClient> channelIdToWebhook = new HashMap<>();
    private boolean invalidStartup = false;

    private final Map<String, SpontaneousEventType> registeredTypes = new HashMap<>();
    private final Map<SpontaneousEventType, String> typeToChannelId = new HashMap<>();

    private final List<Message.MentionType> NO_GROUP_MENTIONS;
    private final CopyOnWriteArrayList<String> ACTIVE_WEBHOOKS = new CopyOnWriteArrayList<>();

    public final static AllowedMentions ALL_MENTIONS_WEBHOOK = AllowedMentions.all();
    public final static AllowedMentions NO_GROUP_MENTIONS_WEBHOOK = new AllowedMentions().withParseEveryone(false).withParseRoles(false).withParseUsers(true);
    private SpontaneousSettings settings;

    public SpontaneousBotService(){
        settings = new SpontaneousSettings();
        final ImmutableList.Builder<Message.MentionType> types = new ImmutableList.Builder<>();
        types.add(Message.MentionType.USER);
        types.add(Message.MentionType.CHANNEL);
        types.add(Message.MentionType.EMOTE);
        NO_GROUP_MENTIONS = types.build();
    }

    public BaseGuildMessageChannel getChannel(String key, boolean primaryFallback) {
        if (isLong(key)) {
            return getDefinedChannel(key, primaryFallback);
        }
        return getDefinedChannel(settings.getMessageChannel(key), primaryFallback);
    }

    public BaseGuildMessageChannel getDefinedChannel(String key, boolean primaryFallback) {
        final long resolvedId = settings.getChannelId(key);

        if (isDebug()) {
            logger.log(Level.INFO, "Channel definition " + key + " resolved as " + resolvedId);
        }
        BaseGuildMessageChannel channel = guild.getTextChannelById(resolvedId);
        if (channel == null && primaryFallback) {
            if (isDebug()) {
                logger.log(Level.WARNING, "Resolved channel id " + resolvedId + " was not found! Falling back to primary channel.");
            }
            channel = primaryChannel;
        }
        return channel;
    }

    public boolean isLong(final String sLong) {
        try {
            Long.parseLong(sLong);
        } catch (final NumberFormatException e) {
            return false;
        }
        return true;
    }

    public WebhookMessage getWebhookMessage(String message) {
        return getWebhookMessage(message, jda.getSelfUser().getAvatarUrl(), guild.getSelfMember().getEffectiveName(), false);
    }

    public WebhookMessage getWebhookMessage(String message, String avatarUrl, String name, boolean groupMentions) {
        return new WebhookMessageBuilder()
                .setAvatarUrl(avatarUrl)
                .setAllowedMentions(groupMentions ? ALL_MENTIONS_WEBHOOK : NO_GROUP_MENTIONS_WEBHOOK)
                .setUsername(name)
                .setContent(message)
                .build();
    }

    public void sendMessage(String message, boolean groupMentions) {
        final BaseGuildMessageChannel channel = getChannel("ALL", true); //TODO: remove or change

        final String webhookChannelId = typeToChannelId.get(SpontaneousEventType.DefaultTypes.CHAT);
        if (webhookChannelId != null) {
            final WebhookClient client = channelIdToWebhook.get(webhookChannelId);
            if (client != null) {
                final String avatarUrl = jda.getSelfUser().getAvatarUrl();
                final String name = guild.getSelfMember().getEffectiveName();
                client.send(getWebhookMessage(message, avatarUrl, name, groupMentions));
                return;
            }
        }

        if (!channel.canTalk()) {
            logger.warning("discordNoSendPermission: " + channel.getName());
            return;
        }
        channel.sendMessage(message)
                .allowedMentions(groupMentions ? null : NO_GROUP_MENTIONS)
                .queue();
    }

    public void startup() throws LoginException, InterruptedException {
        shutdown();

        invalidStartup = true;
        logger.info("discordLoggingIn");
        if (settings.getBotToken().replace("INSERT-TOKEN-HERE", "").trim().isEmpty()) {
            throw new IllegalArgumentException("discordErrorNoToken");
        }

        jda = JDABuilder.createDefault(settings.getBotToken())
                .addEventListeners(new SpontaneousListener(this))
                .enableCache(CacheFlag.EMOTE, CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.MEMBER_OVERRIDES)
                .setContextEnabled(false)
                .build()
                .awaitReady();
        invalidStartup = false;
        updatePresence();
        logger.info("discordLoggingInDone: " + jda.getSelfUser().getAsTag());

        if (jda.getGuilds().isEmpty()) {
            invalidStartup = true;
            throw new IllegalArgumentException("discordErrorNoGuildSize");
        }

        guild = jda.getGuildById(settings.getGuildId());
        if (guild == null) {
            invalidStartup = true;
            throw new IllegalArgumentException("discordErrorNoGuild");
        }

        // Load emotes into cache, JDA will handle updates from here on out.
        guild.retrieveEmotes().queue();

        updatePrimaryChannel();

        updateTypesRelay();

        // We will see you in the future :balloon:
        // DiscordUtil.cleanWebhooks(guild, DiscordUtil.CONSOLE_RELAY_NAME);
        // DiscordUtil.cleanWebhooks(guild, DiscordUtil.ADVANCED_RELAY_NAME);

        if(!invalidStartup)
            logger.info("Bot started!");
    }

    public void updatePrimaryChannel() {
        BaseGuildMessageChannel channel = guild.getTextChannelById(settings.getPrimaryChannelId());
        if (channel == null) {
            channel = guild.getDefaultChannel();
            if (channel == null) {
                throw new RuntimeException("discordErrorNoPerms");
            }
            logger.warning("discordErrorNoPrimary: " + channel.getName());
        }

        if (!channel.canTalk()) {
            throw new RuntimeException("discordErrorNoPrimaryPerms: " + channel.getName());
        }
        primaryChannel = channel;
    }

    public void updateTypesRelay() {
        /*if (!getSettings().isShowAvatar() && !getSettings().isShowName() && !getSettings().isShowDisplayName()) {
            for (WebhookClient webhook : channelIdToWebhook.values()) {
                webhook.close();
            }
            typeToChannelId.clear();
            channelIdToWebhook.clear();
            return;
        }*/

        for (SpontaneousEventType type : SpontaneousEventType.DefaultTypes.values()) {
            if (!type.isPlayer()) {
                continue;
            }

            final BaseGuildMessageChannel channel = getChannel(type.getKey(), true);
            if (channel.getId().equals(typeToChannelId.get(type))) {
                continue;
            }

            final Webhook webhook = getOrCreateWebhook(channel, ADVANCED_RELAY_NAME).join();
            if (webhook == null) {
                final WebhookClient current = channelIdToWebhook.get(channel.getId());
                if (current != null) {
                    current.close();
                }
                channelIdToWebhook.remove(channel.getId());
                continue;
            }
            typeToChannelId.put(type, channel.getId());
            channelIdToWebhook.put(channel.getId(), getWebhookClient(webhook.getIdLong(), webhook.getToken(), jda.getHttpClient()));
        }
    }

    public String parseMessageEmotes(String message) {
        for (final Emote emote : guild.getEmoteCache()) {
            message = message.replaceAll(":" + Pattern.quote(emote.getName()) + ":", emote.getAsMention());
        }
        return message;
    }

    public void updatePresence() {
        jda.getPresence().setPresence(OnlineStatus.fromKey(settings.getOnlineStatus()),
                Activity.of(settings.getActivityType(), settings.getActivity()));
    }

    public WebhookClient getWebhookClient(long id, String token, OkHttpClient client) {
        return new WebhookClientBuilder(id, token)
                .setWait(false)
                .setAllowedMentions(AllowedMentions.none())
                .setHttpClient(client)
                .setDaemon(true)
                .build();
    }

    public CompletableFuture<Webhook> getOrCreateWebhook(final BaseGuildMessageChannel channel, final String webhookName) {
        if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MANAGE_WEBHOOKS)) {
            return CompletableFuture.completedFuture(null);
        }

        final CompletableFuture<Webhook> future = new CompletableFuture<>();
        channel.retrieveWebhooks().queue(webhooks -> {
            for (final Webhook webhook : webhooks) {
                if (webhook.getName().equals(webhookName) && webhook.getToken() != null) {
                    ACTIVE_WEBHOOKS.addIfAbsent(webhook.getId());
                    future.complete(webhook);
                    return;
                }
            }
            createWebhook(channel, webhookName).thenAccept(future::complete);
        });
        return future;
    }

    public CompletableFuture<Webhook> createWebhook(BaseGuildMessageChannel channel, String webhookName) {
        if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MANAGE_WEBHOOKS)) {
            return CompletableFuture.completedFuture(null);
        }

        final CompletableFuture<Webhook> future = new CompletableFuture<>();
        channel.createWebhook(webhookName).queue(webhook -> {
            future.complete(webhook);
            ACTIVE_WEBHOOKS.addIfAbsent(webhook.getId());
        });
        return future;
    }

    private void shutdownConsoleRelay(final boolean closeInjector) {
        if (consoleWebhook != null && !consoleWebhook.isShutdown()) {
            consoleWebhook.close();
        }
        consoleWebhook = null;
    }

    public void shutdown() {

        if (jda != null) {

            shutdownConsoleRelay(true);

            // Unregister leftover jda listeners
            for (Object obj : jda.getRegisteredListeners()) {
                if (!(obj instanceof EventListener)) { // Yeah bro I wish I knew too :/
                    jda.removeEventListener(obj);
                }
            }

            // Creates a future which will be completed when JDA fully shutdowns
            final CompletableFuture<Void> future = new CompletableFuture<>();
            jda.addEventListener(new ListenerAdapter() {
                @Override
                public void onShutdown(@NotNull ShutdownEvent event) {
                    future.complete(null);
                }
            });

            // Tell JDA to wrap it up
            jda.shutdown();
            try {
                // Wait for JDA to wrap it up
                future.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.warning("JDA took longer than expected to shutdown, this may have caused some problems.");
            } finally {
                jda = null;
            }
        }
    }

    public JDA getJda() {
        return jda;
    }

    public Guild getGuild() {
        return guild;
    }

    public WebhookClient getConsoleWebhook() {
        return consoleWebhook;
    }

    public boolean isInvalidStartup() {
        return invalidStartup;
    }

    public SpontaneousSettings getSettings(){
        return settings;
    }

    public boolean isDebug(){
        return false;
    }
}
