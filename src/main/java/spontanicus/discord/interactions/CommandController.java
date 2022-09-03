package spontanicus.discord.interactions;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import spontanicus.discord.SpontaneousSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandController extends ListenerAdapter {
    private static Logger logger = Logger.getLogger("CommandController");

    private final Map<String, DiscordCommand> commandMap = new ConcurrentHashMap<>();
    private final Map<String, DiscordCommand> batchRegistrationQueue = new HashMap<>();
    private boolean initialBatchRegistration = false;

    private final JDA jda;
    private final Guild guild;
    private final SpontaneousSettings settings;

    public CommandController(SpontaneousSettings settings, JDA jda, Guild guild) {
        this.settings = settings;
        this.jda = jda;
        this.guild = guild;
        jda.addEventListener(this);
    }

    public void processBatchRegistration() {
        if (!initialBatchRegistration && !batchRegistrationQueue.isEmpty()) {
            initialBatchRegistration = true;
            final List<CommandData> list = new ArrayList<>();
            for (final DiscordCommand command : batchRegistrationQueue.values()) {
                final CommandDataImpl data = new CommandDataImpl(command.getName(), command.getDescription());
                if (command.getArguments() != null) {
                    for (final DiscordCommandArgument argument : command.getArguments()) {

                        data.addOption(OptionType.valueOf(argument.getType().name()), argument.getName(), argument.getDescription(), argument.isRequired());
                    }
                }
                list.add(data);
            }

            guild.updateCommands().addCommands(list).queue(success -> {
                for (final Command command : success) {
                    DiscordCommand internalCommand = batchRegistrationQueue.get(command.getName());
                    internalCommand.setId(command.getId());
                    commandMap.put(command.getName(), internalCommand);
                    batchRegistrationQueue.remove(command.getName());
                    Bukkit.getLogger().info("Registered guild command " + command.getName() + " with id " + command.getId());
                }

                if (!batchRegistrationQueue.isEmpty()) {
                    logger.warning(batchRegistrationQueue.size() + " Discord commands were lost during command registration!");
                    logger.warning("Lost commands: " + batchRegistrationQueue.keySet());
                    batchRegistrationQueue.clear();
                }
            }, failure -> {
                if (failure instanceof ErrorResponseException && ((ErrorResponseException) failure).getErrorResponse() == ErrorResponse.MISSING_ACCESS) {
                    logger.severe("Missing Access");
                    return;
                }
                logger.log(Level.SEVERE, "Error while registering command", failure);
            });
        }
    }

    public void registerCommand(DiscordCommand command) {

        if (commandMap.containsKey(command.getName())) {
            logger.warning("Command is already registered");
        }

        if (!initialBatchRegistration) {
            logger.info("Marked guild command for batch registration: " + command.getName());
            batchRegistrationQueue.put(command.getName(), command);
            return;
        }

        final CommandDataImpl data = new CommandDataImpl(command.getName(), command.getDescription());
        if (command.getArguments() != null) {
            for (final DiscordCommandArgument argument : command.getArguments()) {
                data.addOption(OptionType.valueOf(argument.getType().name()), argument.getName(), argument.getDescription(), argument.isRequired());
            }
        }

        guild.upsertCommand(data).queue(success -> {
            commandMap.put(command.getName(), command);
            command.setId(success.getId());
            logger.info("Registered guild command " + success.getName() + " with id " + success.getId());
        }, failure -> {
            if (failure instanceof ErrorResponseException && ((ErrorResponseException) failure).getErrorResponse() == ErrorResponse.MISSING_ACCESS) {
                logger.severe("Missing Access");
                return;
            }
            logger.log(Level.SEVERE, "Error while registering command", failure);
        });
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getGuild() == null || event.getMember() == null || !commandMap.containsKey(event.getName())) {
            return;
        }

        final DiscordCommand command = commandMap.get(event.getName());

        if (command.isDisabled()) {
            event.reply("Command is disabled").setEphemeral(true).queue();
            return;
        }

        event.deferReply(command.isEphemeral()).queue(null, failure -> logger.log(Level.SEVERE, "Error while deferring Discord command", failure));

        final DiscordCommandEvent discordEvent = new DiscordCommandEvent(event);
        /*if (!DiscordUtil.hasRoles(event.getMember(), settings.getCommandSnowflakes(command.getName()))) {
            interactionEvent.reply("No access to command");
            return;
        }*/
        command.onCommand(discordEvent);
    }

    public void shutdown(){
        try {
            guild.updateCommands().complete();
        } catch (Throwable e) {
            logger.severe("Error while deleting commands: " + e.getMessage());
            e.printStackTrace();
        }
        commandMap.clear();
    }
}
