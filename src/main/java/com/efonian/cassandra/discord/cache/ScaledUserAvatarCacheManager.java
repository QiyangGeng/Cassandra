package com.efonian.cassandra.discord.cache;

import com.efonian.cassandra.discord.event.EventListenerManager;
import com.efonian.cassandra.util.UtilFile;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


// TODO: abstract this into a ScaledUserAvatarCache and a CacheManager
@Component
public class ScaledUserAvatarCacheManager {
    private static final File DIRECTORY = Paths.get("./ProgramData/UserAvatar").toFile();
    private final List<Long> clientIDs;
    
    
    @Autowired
    private ScaledUserAvatarCacheManager(EventListenerManager listenerManager) throws URISyntaxException {
        URL clientsFileURL = ScaledUserAvatarCacheManager.class.getClassLoader().getResource("com/efonian/cassandra/discord/clients.csv");
        if(clientsFileURL == null)
            throw new RuntimeException("Clients file resource not found");
        
        File clientsFile = Paths.get(clientsFileURL.toURI()).toFile();
        String clientsFileContent = UtilFile.readAll(clientsFile);
        if(clientsFileContent == null)
            throw new RuntimeException("Could not read clients file");
        
        clientIDs = List.of(clientsFileContent.split(",")).stream().map(str -> Long.parseLong(str.strip())).collect(Collectors.toList());
        
        registerListeners(listenerManager);
    }
    
    private void refreshAllCache() {
        clientIDs.forEach(this::refreshUserCache);
    }
    
    private void refreshUserCache(Long userID) {
        if(!clientIDs.contains(userID))
            return;
        
//        File userAvatarFile =
    }
    
    private void registerListeners(EventListenerManager listenerManager) {
        listenerManager
                .registerOperator(ReadyEvent.class, event -> refreshAllCache())
                .registerOperator(ReconnectedEvent.class, event -> refreshAllCache())
                .registerOperator(UserUpdateAvatarEvent.class, event -> refreshUserCache(event.getUser().getIdLong()));
    }
}
