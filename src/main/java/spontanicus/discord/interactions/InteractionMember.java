package spontanicus.discord.interactions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class InteractionMember{
    private final Member member;

    public InteractionMember(Member member) {
        this.member = member;
    }

    public String getName() {
        return member.getUser().getName();
    }

    public String getDiscriminator() {
        return member.getUser().getDiscriminator();
    }

    public String getEffectiveName() {
        return member.getEffectiveName();
    }

    public String getNickname() {
        return member.getNickname();
    }

    public String getId() {
        return member.getId();
    }

    public String getTag() {
        return getName() + "#" + getDiscriminator();
    }

    public boolean isAdmin() {
        return member.hasPermission(Permission.ADMINISTRATOR);
    }

    public boolean hasRoles(List<String> roleDefinitions) {
        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }

        final List<Role> roles = member.getRoles();
        for (String roleDefinition : roleDefinitions) {
            roleDefinition = roleDefinition.trim();

            if (roleDefinition.equals("*") || member.getId().equals(roleDefinition)) {
                return true;
            }

            for (final Role role : roles) {
                if (role.getId().equals(roleDefinition) || role.getName().equalsIgnoreCase(roleDefinition)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Member getJdaObject() {
        return member;
    }

    public CompletableFuture<Boolean> sendPrivateMessage(String content) {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        final CompletableFuture<PrivateChannel> privateFuture = member.getUser().openPrivateChannel().submit();
        privateFuture.thenCompose(privateChannel -> privateChannel.sendMessage(content).submit())
                .whenComplete((m, error) -> {
                    if (error != null) {
                        future.complete(false);
                        return;
                    }
                    future.complete(true);
                });
        return future;
    }
}

