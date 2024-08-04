package spontanicus.discord.interactions;

import spontanicus.discord.SpontaneousSettings;

import java.util.ArrayList;
import java.util.List;

public abstract class DiscordCommand {
    private final SpontaneousSettings settings;
    private final String name;
    private final String description;
    private final List<DiscordCommandArgument> arguments = new ArrayList<>();
    private String commandId;

    public DiscordCommand(SpontaneousSettings settings, String name, String description) {
        this.settings = settings;
        this.name = name;
        this.description = description;
    }

    public void setId(String id){
        commandId = id;
    }

    public String getId(){
        return commandId;
    }

    public final boolean isDisabled() {
        return false;
    }

    public final boolean isEphemeral() {
        return true;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<DiscordCommandArgument> getArguments() {
        return arguments;
    }

    public void addArgument(DiscordCommandArgument argument) {
        arguments.add(argument);
    }

    abstract public void onCommand(DiscordCommandEvent event);

    protected SpontaneousSettings getSettings(){
        return settings;
    }
}

