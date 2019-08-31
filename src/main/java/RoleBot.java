import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.io.*;
import java.util.List;

public class RoleBot {
    public static void main(String[] args) {
        // Insert your bot's token here
        String token = "";
        String channelIDT = "";
        String serverIDT = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("src/main/resources/token.cred")));
            token = reader.readLine();
            channelIDT = reader.readLine();
            serverIDT = reader.readLine();
        }catch (IOException e){
            System.out.println("could not find token");
            System.exit(1);
        }
        final String channelID = channelIDT;
        final String serverID = serverIDT;
        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();
        Server server = api.getServerById(serverID).get();
        //Channel ch = api.getChannelById(channelID).get();

        api.addMessageCreateListener(event -> {
            List<Role> roles = server.getRoles();
            long temp = 1000;
            for(int i = 0;i<roles.size();i++){
                if(roles.get(i).getName().equals("RoleBot")){
                    temp = i;
                    break;
                }
            }
            long botIndex = temp;
            if(event.getChannel().getIdAsString().equals(channelID)){
                String message = event.getMessageContent();
                if(message.equals("@Everyone")||message.equals("RoleBot")){
                    event.getMessage().delete();
                }
                else if (event.getMessageAuthor().asUser().isPresent()) {
                    User user = event.getMessageAuthor().asUser().get();
                    boolean found = false;
                    for(Role r:roles){
                        if(r.getName().equals(message)){
                            user.addRole(r);
                            found = true;
                        }
                    }
                    if(!found && user.getRoles(api.getServerById(serverID).get()).stream().filter(r->r.getPosition() > botIndex).count() == 0){
                        event.getMessage().delete();
                    }
                }
            }
        });
        api.addMessageDeleteListener(event -> {
            List<Role> roles = server.getRoles();
            if(event.getChannel().getIdAsString().equals(channelID)){
                String message = event.getMessageContent().orElse("");
                if (event.getMessageAuthor().isPresent()) {
                    User user = event.getMessageAuthor().get().asUser().get();
                    roles.parallelStream().filter(r -> r.getName().equals(message)).forEach(user::removeRole);
                }
            }
        });

        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite(Permissions.fromBitmask(268443648)));
    }
}
