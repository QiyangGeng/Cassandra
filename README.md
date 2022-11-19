# Cassandra
This application includes a Discord bot which uses the [Spring Framework](https://spring.io/),
and communicates with Discord using the popular event-driven API [JDA](https://github.com/DV8FromTheWorld/JDA).

This is a personal toy project to learn various different topics, and is not very good at being a discord bot. 
I'm mostly just doing my own things, and there is not much UX designing going on.
There is also no plan for the bot to become publicly available. However, if you can see this code, you can always fork
it and supply your own token.

Progress is interspersed. I wrote all this because past me is awesome and takes care of future me. 
Currently, I am planning on migrating and updating after JDA 5.0 is out of alpha.

If you are me, or have access to my Discord account for some reason, you can invite the bot using this 
[link](https://discord.com/api/oauth2/authorize?client_id=836993452113264640&permissions=8&scope=bot).

## Contents
- [Summary](#summary)
- [Overview](#overview)
    - [DiscordBot Class](#discordBot-class)
    - [Event Handling](#event-handling)
    - [Command](#command)
    - [Console](#console)
- [TODOs](#todos)
- [Change Log](#change-log)

## Summary
The intention of creating this project is applying object-oriented programming concepts, 
learning relevant design patterns, and exploring various interesting APIs. As such,
many aspects of this program might not be seen as "well-designed" since it is created with the programmer (me) as the
main stakeholder, not users.

The purpose of this project naturally evolved over time. At first, this project was a refactoring of one of my
older projects into the Spring framework. As the project started to take shape, however, I began to see more and more
things I could do very easily with this program due to its modularity and extensibility, and I would start to
implement random things as needs arise.

For example, one night, I found myself wanting to call one of my friends 'dum', but using a QR code. 
The need for a QR code decoder followed thereof. 
This lead to the command class `cmd_QRCode`, which depends on the `UtilQRCode` class to interface with the QR code
API. All in all, they account for around a hundred lines of code, and required no modification of any other files.
Furthermore, since I had ~~copied the provided demo to~~ implemented the `UtilQRCode` class, it was also trivial to create
an additional class, `QRCodeAutoDecoder`, which tries to decode any image received as a QR code. It only took around two
dozen lines of actual code, and no modifications of other files.

This extensibility has lead this application to become a platform for many small toy projects,
and it allows me to show it off to my friends easily.

## Overview
The following is presented roughly in the order in which I coded them.

### DiscordBot Class
The "main" `DiscordBot` class simply builds a `JDA` instance, which lets the bot log onto discord.
This involves registering an event listener, which is aptly named `EventListener`.
The `DiscordBot` class also creates some auxiliary listeners, listening to events such as the `readyEvent`,
which occurs when the bot has established everything necessary with discord.

### Event Handling
The `EventListener` class listens for all incoming events through the JDA API,
and passes everything to the `EventListenerManager`. The manager class has a private map of all listeners,
such as the ready event listener from `DiscordBot`.
The manager class checks whether any listener is listening for the event it just received,
and pass the event to the appropriate listener, should there be any.
The listener can then choose to keep listening, or stop listening.

The `EventListenerManager` class has a public method for registering a listener.
The parameters involve the type of event, and a function which consumes instances of that event
(implemented using a functional interface called `EventOperator`),
and returns a boolean to indicate whether the listener wishes to keep listening for said events.
If not, it is removed from the listener map and left to be garbage collected.

### Command
Command handling starts with the `CommandManager` class, which registers a `MessageListener` with the
`EventListenerManager` post construction, and sets the `handleMessageInput` method as the callback function.
This function will parse the received message into more useful objects, such as separate argument arrays,
and store them in a `CommandContainer`.
It will execute the command with an executor if the request is valid. If the command is multi-staged, it will be stored within
the manager to await callback.

#### Command Class
The commands take advantage of the Spring framework, and allows for initialization, setup, and registration of concrete 
child by just extending the `Command` class and declaring itself a bean. 
This means that no class is necessarily dependent on any command, 
i.e. no class needs to mention the command in their code, i.e. no other files need to be edited. This leads to great
modularity and exten
The abstract class itself contains some standard methods, such as `execute`, which executes the command,
`invokes`, which returns a list of invokes the command responds to, and `description`, which returns a description of
the command.

#### Validation and Access Control
The validation step checks for things such as the origin of the message, both author and place, and whether the user
has access.

It is well known that Discord servers, or guilds, have an implementation of role-based access control (RBAC) system.
While it may seem intuitive to mirror the implementation for use in command access control, there is one caveat: 
each Discord guild has their custom implementation of user roles.
This mostly nullifies the advantages of mirroring the RBAC system, since there is not much to mirror.
An alternative is to enforce a "standard" set of names for roles in servers and apply the same set of rules across all
servers, but this is quite restrictive, and very similar to what Discord has already.

The alternative selected was a Mandatory Access Control (MAC), which assigns values from the enum `CommandAccessLevel`
to users based on record, or lack there of. Access control rules are defined using these access levels.
A RBAC system can be built on top by allowing linkage of a user role to a specific command access level, and the DAC
system will allow users with sufficient access to give access to others. The Biba model can be used to ensure integrity of
the system, so a user can only assign (write) access levels that is below their access level.

#### Annotations
Some annotations aid in the control of commands. Since commands do not need to be registered manually, there needs to be
a way for a command class to exist without being registered, for purposes such as testing. This is accomplished by the
`Disabled` annotation. There are also flag annotations such as `BotFriendly` and `GuildOnly`. Lastly, the
`DeclareCommandAccessLevel` annotation lets the command declare its access level. It is possible to declare the access
levels for private channel and guild channel separately, which is useful for commands such as the print command. It will
cause no harm for the bot to wipe its own record from a private channel; however, wiping the message history of everyone
on a guild channel will mostly likely prove disastrous.

#### Debug Commands
There are a few commands with debug purposes. 
- `cmd_Disabled` is a disabled command, which is used to test the disabling system;
- `cmd_Exception` throws a `RunTimeException`, which can be used for testing command exception handling;
- `cmd_Introspection` sends the file of a command, given the command name or invoke;
- `cmd_Ping` is a classic Discord command for testing response;
- `cmd_Timeout` hangs indefinitely, eventually leading to a timeout event.

### Console
A basic CLI is available for the program. At the moment, the only thing implemented is starting the proper exit of the 
application.

## TODOs
### Short term:
- Implement DebugManager
- Implement `cmd_Crafty`
- Implement GuildAudioManager
- Update UtilGraph methods
- Implement UtilPython
- Compile the python script
- Implement the empty classes

### Medium term:
- ~~Implement staged commands~~ Implement services (see change log)
- Implement annotation-predicate hashMaps for flags in command access levels
- Add the RBAC system
- Implement console
- Implement UtilMessage for Discord Bot
- Implement more fully UtilFile
- Radio
- Bus and flight data

### Long term (mostly speculative):
- Interpreter with discord gui elements and some capabilities
- Compute shaders for computation of stuff
- JIT stuff through command
- Move to GraalVM

## Change Log
This section will include major program decision changes (when I feel like it)

### Moving on from Staged Commands to Services (2021/05/29)

As per what I have never mentioned before, *Cassandra* is the fifth iteration of my Discord Bot program 
(maybe V0.5). Previous versions included implementations of "staged commands", which are similar to the basic
commands, except they expect multiple inputs over time. This was fine with previous implementations of commands.

However, as *Cassandra* slowly became the first iteration where I actually planned for the program components, I came to
the realization that there is a more fundamental difference between simple commands and staged ones, in that the latter
is much, much more complex. Further, using Spring, it is quite convenient to scope the former as singletons, and the 
latter prototypes. (Also, the command package is getting quite large.) As such, I will be separating out "staged commands"
as services rather than commands.

This does not mean they will have nothing to do with commands: it is likely that they will be initiated by commands, but
we will see.

### Making the repo public (2022/11/17)

I decided to make the repo public, so some people can see my code. Don't judge too hard.
