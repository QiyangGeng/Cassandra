package com.efonian.cassandra.discord.command;

import com.efonian.cassandra.discord.command.annotation.BotFriendly;
import com.efonian.cassandra.discord.command.annotation.DeclareCommandAccessLevel;
import com.efonian.cassandra.discord.command.annotation.Disabled;
import com.efonian.cassandra.discord.command.annotation.GuildOnly;
import com.efonian.cassandra.util.UtilFile;
import com.efonian.cassandra.util.UtilRuntime;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Component
@ConditionalOnProperty(value = "cassandra.discord.init", havingValue = "true")
public final class CommandAccessManager {
    private static final Logger logger = LoggerFactory.getLogger(CommandAccessManager.class);
    
    private static final CommandAccessLevel MULTI_COMMAND_ALLOWED = CommandAccessLevel.ADMIN;
    private static final String USER_ACCESS_LEVELS_FILE_PATH = "ProgramData/UserAccessLevels.json";
    
    private static final Map<Command, CommandAccessLevelContainer> commandAccessLevels = new ConcurrentHashMap<>();
    private static Map<Long, CommandAccessLevel> userAccessLevelRecords = new ConcurrentHashMap<>();
    
    
    /**
     * @return string left of error message and (implied) denied permission check, or right slight fail/pass
     */
    static Either<String, Boolean> hasPermission(CommandContainer cc, Command command) {
        CommandAccessLevelContainer calc = commandAccessLevels.get(command);
    
        if(calc == null)                                                return Either.left("command access level is not registered");
        
        // these should also print to debug channel, when the manager is implemented...
        if(calc.guildAccessLevel.equals(CommandAccessLevel.NULL))       return Either.left("command guild access level is registered as null");
        if(calc.privateAccessLevel.equals(CommandAccessLevel.NULL))     return Either.left("command private access level is registered as null");
        
        if(!cc.event.isFromGuild() && calc.guildOnly)                   return Either.left("this command can only be used in servers");
    
        if(cc.event.getAuthor().isBot() && !calc.botFriendly)           return Either.right(false);
        
        return Either.right(findAndCompareAccessLevel(cc, command, calc));
    }
    
    /**
     * Permission check, but the error messages are ignored
     * @return      true if the author of the event have permission to use the command, false otherwise
     */
    static boolean hasPermissionSimple(CommandContainer cc, Command command) {
        return CommandAccessManager.hasPermission(cc, command).getOrElseGet((msg) -> false);
    }
    
    static List<Command> getAvailableCommands(CommandContainer cc) {
        return commandAccessLevels.keySet().stream().filter(cmd ->
                findAndCompareAccessLevel(cc, cmd)).collect(Collectors.toList());
    }
    
    static boolean canRequestMulti(long userId) {
        return MULTI_COMMAND_ALLOWED.compareTo(findUserAccessLevel(userId)) <= 0;
    }
    
    private static boolean findAndCompareAccessLevel(CommandContainer cc, Command command) {
        CommandAccessLevelContainer calc = commandAccessLevels.get(command);
        return findAndCompareAccessLevel(cc, command, calc);
    }
    
    private static boolean findAndCompareAccessLevel(CommandContainer cc, Command command, CommandAccessLevelContainer calc) {
        CommandAccessLevel commandAL = cc.event.isFromGuild()? calc.guildAccessLevel : calc.privateAccessLevel;
        
        // handle dynamic access level
        if(commandAL.equals(CommandAccessLevel.DYNAMIC))
            commandAL = command.dynamicallyAssignAccessLevel(cc);
        
        return commandAL.compareTo(findUserAccessLevel(cc.event.getAuthor().getIdLong())) <= 0;
    }
    
    static CommandAccessLevel findUserAccessLevel(long userId) {
        return userAccessLevelRecords.getOrDefault(userId, CommandAccessLevel.REGULAR);
    }
    
    // add method to link access level to role
    // owner of server is by default ADMIN
    static void updateUserPermission(long userId, CommandAccessLevel newAccessLevel) {
        if(newAccessLevel.equals(CommandAccessLevel.NULL))
            throw new IllegalArgumentException("Cannot set the command access level of a user to NULL");
        
        userAccessLevelRecords.replace(userId, newAccessLevel);
        syncFileWithRuntimeRecords();
    }
    
    private static void syncFileWithRuntimeRecords() {
        Type calType = new TypeToken<ConcurrentHashMap<Long, CommandAccessLevel>>(){}.getType();
        
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(calType, new UserAccessLevelsDeserializer())
                .create();
    
        UtilFile.write(USER_ACCESS_LEVELS_FILE_PATH, gson.toJson(userAccessLevelRecords));
    }
    
    @PostConstruct
    private void loadUserAccessLevelRecords() {
        Type calType = new TypeToken<ConcurrentHashMap<Long, CommandAccessLevel>>(){}.getType();
        
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(calType,
                        new UserAccessLevelsDeserializer())
                .create();
        
        String inFile = UtilFile.readAll(USER_ACCESS_LEVELS_FILE_PATH);
        if(inFile == null) {
            logger.error("Could not read user access levels file");
            UtilRuntime.shutdown(2);
            return;     // this line should be unreachable, just here to make the IDE happy
        }
        
        if(inFile.equals("") || inFile.equals("[]"))
            return;
        
        userAccessLevelRecords = gson.fromJson(inFile, calType);
    }
    
    @Autowired
    private void populateCommandAccessLevels(List<? extends Command> commands) {
        for(Command cmd: commands) {
            Class<? extends Command> cla = cmd.getClass();
            
            if(cla.isAnnotationPresent(Disabled.class))
                continue;
    
            boolean botFriendly = cla.isAnnotationPresent(BotFriendly.class);
            boolean guildOnly = cla.isAnnotationPresent(GuildOnly.class);
            
            if(!cla.isAnnotationPresent(DeclareCommandAccessLevel.class)) {
                logger.warn(String.format("Command %s did not declare command access level", cla.getName()));
                commandAccessLevels.put(cmd, new CommandAccessLevelContainer(CommandAccessLevel.NULL, botFriendly, guildOnly));
                continue;
            }
            
            DeclareCommandAccessLevel ann = cla.getAnnotation(DeclareCommandAccessLevel.class);
            if(ann.accessLevel() != CommandAccessLevel.NULL) {
                commandAccessLevels.put(cmd, new CommandAccessLevelContainer(ann.accessLevel(), botFriendly, guildOnly));
                continue;
            }
            
            commandAccessLevels.put(cmd,
                    new CommandAccessLevelContainer(ann.guildAccessLevel(), ann.privateAccessLevel(), botFriendly, guildOnly));
        }
    }
    
    // TODO: the boolean flags can be abstrated to a more general set of predicates such that only one place need to change
    //      code when changing/adding flags, the reflections library imported can be used to check for all annotations
    //      in the appropriate package
    /**
     * A container to put in the map, contains information on command access requirements
     */
    private static class CommandAccessLevelContainer {
        private final CommandAccessLevel guildAccessLevel, privateAccessLevel;
        private final boolean botFriendly, guildOnly;
        
        private CommandAccessLevelContainer(CommandAccessLevel guildAccessLevel,
                                            CommandAccessLevel privateAccessLevel,
                                            boolean botFriendly,
                                            boolean guildOnly) {
            this.guildAccessLevel = guildAccessLevel;
            this.privateAccessLevel = privateAccessLevel;
            this.botFriendly = botFriendly;
            this.guildOnly = guildOnly;
        }
    
        private CommandAccessLevelContainer(CommandAccessLevel accessLevel,
                                            boolean botFriendly,
                                            boolean guildOnly) {
            this(accessLevel, accessLevel, botFriendly, guildOnly);
        }
    
        @Override
        public String toString() {
            return "Guild Access Level: " + guildAccessLevel +
                    ", Private Access Level: " + privateAccessLevel +
                    ", Bot Friendly: " + botFriendly +
                    ", Guild Only: " + guildOnly;
        }
    }
    
    /**
     * Json deserializer for user access level json file
     */
    private static class UserAccessLevelsDeserializer implements JsonDeserializer<ConcurrentHashMap<Long, CommandAccessLevel>> {
        @Override
        public ConcurrentHashMap<Long, CommandAccessLevel> deserialize(
                JsonElement jsonElement,
                Type type,
                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonElement.getAsJsonObject().entrySet().parallelStream()
                    .collect(Collectors.toConcurrentMap(
                            k -> Long.valueOf(k.getKey()),
                            v -> jsonDeserializationContext.deserialize(v.getValue(), CommandAccessLevel.class),
                            (o1, o2) -> o1,
                            ConcurrentHashMap::new
                    ));
        }
    }
}
