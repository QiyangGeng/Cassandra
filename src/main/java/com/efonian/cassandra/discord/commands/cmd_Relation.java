package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import com.efonian.cassandra.misc.ColouredVertex;
import com.efonian.cassandra.misc.RelationshipEdge;
import com.efonian.cassandra.util.UtilGraph;
import com.google.common.collect.Lists;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.Multigraph;
import org.jungrapht.visualization.layout.algorithms.CircleLayoutAlgorithm;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.MODERATOR)
class cmd_Relation extends Command {
    @Override
    void execute(CommandContainer cc) {
        List<User> users = userList(cc);
        Graph<ColouredVertex<String>, RelationshipEdge> graph = graphUsers(users, cc);
        BufferedImage renderedGraph = UtilGraph.renderWithColouredVertices(graph, new CircleLayoutAlgorithm<>());
        sendImage(cc.event.getChannel(), renderedGraph, "relations");
    }
    
    private List<User> userList(CommandContainer cc) {
        // Here, the user collection is a list, as index is needed for the Vertex Factory in graphUsers method
        List<User> list = new ArrayList<>();
        Lists.newArrayList(cc.args).forEach(str -> {
            try {
                long id = Long.parseLong(str);
                User user = cc.event.getJDA().retrieveUserById(id).complete();
                if(user != null)
                    list.add(user);
            } catch(NumberFormatException ignored) {}
        });
        return list;
    }
    
    private Graph<ColouredVertex<String>, RelationshipEdge> graphUsers(List<User> users, CommandContainer cc) {
        Graph<ColouredVertex<String>, RelationshipEdge> graph = new Multigraph<>(RelationshipEdge.class);
        
        switch(users.size()) {
            case 0:
                User caller = cc.event.getAuthor();
                users.add(caller);
                users.add(getSelfUser());
                
//                ColouredVertex<String> selfVertex = new ColouredVertex<>(getSelfName(), userAvatarAverageColour(getSelfUser()));
//                ColouredVertex<String> userVertex = new ColouredVertex<>(caller.getName(), userAvatarAverageColour(caller));
//
//                users.forEach(u -> graph.addVertex(new ColouredVertex<>(u.getName(), userAvatarAverageColour(u))));
//                // The getMutualGuilds() method doesn't return anything
//                cc.event.getAuthor().getMutualGuilds().forEach(guild -> graph.addEdge(userVertex, selfVertex, new RelationshipEdge(guild.getName())));
//
//                return graph;
                break;

            case 1:
                // At this point, the users list has one user. Our goal is to populate the list with at least two
                // users, where we have the author and the bot as fallback in that order. Thus:
                //  - if the list is a user that is not the author, we add the author to the list
                //  - if the list is a user that is the author, we add the bot to the list
                if(users.contains(cc.event.getAuthor()))
                    users.add(getSelfUser());
                else
                    users.add(cc.event.getAuthor());
                
                break;
        }
    
        // Add relevant users to the graph as vertices
        users.forEach(u -> graph.addVertex(new ColouredVertex<>(u.getName(), userAvatarAverageColour(u))));
        
        cc.event.getJDA().getGuilds().forEach(guild -> {
            List<User> guildUsers = guild.findMembers(member -> users.contains(member.getUser())).get()
                    .stream().map(Member::getUser).collect(Collectors.toList());
            
            // We only need to add additional edges if there are at least two relevant users in the guild
            if(guildUsers.size() < 2)
                return;
            
            // We create a new map with the sublist of relevant users present in this guild
            Graph<ColouredVertex<String>, RelationshipEdge> guildGraph = new Multigraph<>(new Supplier<>() {
                private int index = 0;
    
                @Override
                public ColouredVertex<String> get() {
                    User user = guildUsers.get(index++); // Sublist of user present in this guild
                    return new ColouredVertex<>(user.getName(), userAvatarAverageColour(user));
                }
            }, () -> new RelationshipEdge(guild.getName()), false);
            
            // We generate a complete graph, drawing an edge between all relevant users in this guild
            UtilGraph.generateCompleteGraph(guildGraph, guildUsers.size());
            
            // We add the edges to the main graph by combining the two graphs
            Graphs.addGraph(graph, guildGraph);
        });
        
        return graph;
    }
    
    @Override
    List<String> invokes() {
        return List.of("rel", "relation", "relations");
    }
    
    @Override
    String description() {
        return "[user ID]+ gives an overview of shared guilds among a group of users in which this bot is present; " +
                "if only one user ID is provided, then the user ID of the caller will also be user, otherwise, the " +
                "user ID of the caller has to be explicitly included to be added to the graph;" +
                "if no user ID is provided, the shared guilds between the caller and the bot will be given";
    }
}
