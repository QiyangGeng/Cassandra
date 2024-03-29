package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.BotFriendly;
import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import com.efonian.cassandra.discord.commands.annotation.Disabled;
import com.efonian.cassandra.discord.commands.annotation.GuildOnly;
import com.efonian.cassandra.util.UtilFile;
import com.efonian.cassandra.util.UtilRuntime;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.vavr.control.Either;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Component
public final class CommandAccessManager {
    private static final Logger logger = LoggerFactory.getLogger(CommandAccessManager.class);
    
    private static final CommandAccessLevel MULTI_COMMAND_ALLOWED = CommandAccessLevel.ADMIN;
    private static final String USER_ACCESS_LEVELS_FILE_PATH = "ProgramData/UserAccessLevels.json";
    
    private static final Map<Command, CommandAccessLevelContainer> commandAccessLevels = new ConcurrentHashMap<>();
    private static Map<Long, CommandAccessLevel> userAccessLevelRecords = new ConcurrentHashMap<>();
    
    
    
    /**
     * @return string left of error message and (implied) denied permission check, or right slight fail/pass
     */
    static Either<String, Boolean> hasPermission(boolean isFromGuild, User user, Command command) {
        CommandAccessLevelContainer calc = commandAccessLevels.get(command);
        
        if(calc == null)                                                return Either.left("command access level is not registered");
        if(calc.guildAccessLevel.equals(CommandAccessLevel.NULL))       return Either.left("command guild access level is registered as null");
        if(calc.privateAccessLevel.equals(CommandAccessLevel.NULL))     return Either.left("command private access level is registered as null");
        if(isFromGuild && calc.guildOnly)                               return Either.left("this command can only be used in servers");
        if(user.isBot() && !calc.botFriendly)                           return Either.right(false);
        return Either.right(findAndCompareAccessLevel(isFromGuild, user, calc));
    }
    
    /**
     * Permission check, but the error messages are ignored
     * @return      true if the author of the event have permission to use the command, false otherwise
     */
    static boolean hasPermissionSimple(boolean isFromGuild, User user, Command command) {
        return CommandAccessManager.hasPermission(isFromGuild, user, command).getOrElseGet((msg) -> false);
    }
    
    static boolean canRequestMulti(long userId) {
        return MULTI_COMMAND_ALLOWED.compareTo(findUserAccessLevel(userId)) <= 0;
    }
    
    /**
     * Given the information from the command container, determines to which command the caller has access. One caveat
     * is that commands which dynamically assign access level based on the argument will by default be rejected for
     * callers with access levels less than FULL, per the order of access levels in the <@c>CommandAccessLevel</@c>
     * enum.
     *
     * For now, this method is tailor made for the cmd_Help command.
     */
    static List<Command> getAvailableCommands(CommandContainer cc) {
        return commandAccessLevels.keySet().stream()
                .filter(cmd -> findAndCompareAccessLevel(
                        cc.event.isFromGuild(), cc.event.getAuthor(),
                        commandAccessLevels.get(cmd)))
                .collect(Collectors.toList());
    }
    
    private static boolean findAndCompareAccessLevel(boolean isFromGuild, User user, CommandAccessLevelContainer calc) {
        return calc.getAccessLevel(isFromGuild)
                .compareTo(findUserAccessLevel(user.getIdLong())) <= 0;
    }
    
    static CommandAccessLevel findUserAccessLevel(long userId) {
        return userAccessLevelRecords.getOrDefault(userId, CommandAccessLevel.REGULAR);
    }
    
    static boolean checkUserAccessLevel(long userId, CommandAccessLevel cal) {
        return cal.compareTo(findUserAccessLevel(userId)) <= 0;
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
    
    void registerCommand(Command cmd) {
        Class<? extends Command> cla = cmd.getClass();
    
        if(cla.isAnnotationPresent(Disabled.class))
            return;
    
        boolean botFriendly = cla.isAnnotationPresent(BotFriendly.class);
        boolean guildOnly = cla.isAnnotationPresent(GuildOnly.class);
    
        if(!cla.isAnnotationPresent(DeclareCommandAccessLevel.class)) {
            logger.warn(String.format("Command %s did not declare command access level", cla.getName()));
            commandAccessLevels.put(cmd, new CommandAccessLevelContainer(CommandAccessLevel.ADMIN, botFriendly, guildOnly));
            return;
        }
        
        DeclareCommandAccessLevel ann = cla.getAnnotation(DeclareCommandAccessLevel.class);
        
        if(ann.accessLevel() != CommandAccessLevel.NULL) {
            commandAccessLevels.put(cmd, new CommandAccessLevelContainer(ann.accessLevel(), botFriendly, guildOnly));
            return;
        }
    
        commandAccessLevels.put(cmd,
                new CommandAccessLevelContainer(ann.guildAccessLevel(), ann.privateAccessLevel(), botFriendly, guildOnly));
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
        
        private CommandAccessLevel getAccessLevel(boolean isFromGuild) {
            return isFromGuild? this.guildAccessLevel : this.privateAccessLevel;
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
