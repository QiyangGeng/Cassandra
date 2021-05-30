# Cassandra
This (very much still work in progress) discord bot uses the [Spring Framework](https://spring.io/),
and communicates with Discord using the popular event-driven API [JDA](https://github.com/DV8FromTheWorld/JDA).

This is a toy project, and there is no plan for the bot to become publicly available. However, the code is all here...

## Contents
- [Summary](#summary)
- [Overview](#overview)
    - [DiscordBot Class](#discordBot-class)
    - [Event Handling](#event-handling)
    - [Command](#command)
    - [Console](#console)
- [TODOs](#todos)

## Summary
The purposes of this project is applying OO concepts, learning relevant design patterns, and exploring various interesting
APIs. As such,
many aspects of this program can be seen as "over-engineered" since concepts are not applied based on necessity,
but on whether I would like to try said concept.
As such, be prepared to see some weird design choices.

The learning goal of this project naturally evolved over time. At first, this project was a refactoring of one of my
older projects into the Spring framework. As the project started to take shape, however, I began to see more and more
things I could do very easily with this program due to its extensibility, and I would start to
implement apparently random things as needs arise.

For example, one night, I found myself wanting to call one of my friends 'dum' subtlety, id est calling them 'dum' but
encoding the string in which I call them 'dum' into a QR code. Of course, I also needed to make a QR code decoder. This
turned into the command class `cmd_QRCode`, alongside a `UtilQRCode` class which actually interfaces with the QR code
API. All in all, they account for around a hundred lines of code, and required no modification of any other files.
Furthermore, since I had ~~copied the provided demo~~ implemented the `UtilQRCode` class, it was also trivial to create
an additional class, `QRCodeAutoDecoder`, which tries to decode any image received as a QR code. It took around two
dozen lines of actual code, and no modifications of other files. (Of course, this sort of makes half of the command class
obsolete, too bad!)

As such, this extensibility has lead this program to become a platform for many small toy projects using various APIs,
and never having to ask whether I should do. (Also, using discord means I can avoid front end stuff.) You can get an
overview of these APIs in the `pom` file.

## Overview
The following is presented roughly in the order in which I coded them.

### DiscordBot Class
The "main" `DiscordBot` class simply builds a `JDA` instance, which lets the bot log in to discord.
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
The parameters involves the type of event and a function which consumes an instance of said event
(implemented using a functional interface called `EventOperator`),
and returns a boolean to indicate whether the listener wishes to keep listening for said events.
If not, it is removed from the listener map and left to be garbage collected.

### Command
Command handling starts with the `CommandManager` class, which registers a `MessageListener` with the
`EventListenerManager` post construction, and sets the `handleMessageInput` method as the callback function.
This function will parse the received message into more useful objects, such as separate argument arrays,
and store them in a `CommandContainer`.
It will execute the command with an executor if the request is valid. If the command is staged, it will be stored within
the manager to await callback.

#### Command Class
The commands takes advantage of the Spring framework, and allows for initialization, setup, and registration all by
just extending the `Command` class and declaring itself a bean. This means that no class is dependent on any command.
The abstract class itself contains some standard boring methods, such as `execute`, which executes the command,
`invokes`, which returns a list of invokes the command responds to, and `description`, which returns a description of
the command.

#### Validation and Access Control
The validation step checks for things such as the origin of the message, both author and place, and whether the user
has access.

It is well known that Discord servers, or guilds, have an implementation of role-based access control (RBAC) system.
While it may seem intuitive to mirror the implementation for use in command access control, there is one fact which brings doubt.
Namely, each Discord guild has their custom implementation of user roles.
This mostly nullifies the advantages of mirroring the RBAC system.
An alternative is to enforce a "standard" set of names for roles in servers and apply the same set of rules across all
servers.

The alternative selected was a Mandatory Access Control (MAC), which assigns values from the enum `CommandAccessLevel`
to users based on record, or lack there of. Access control rules are defined using these access levels.
A RBAC system can be built on top by allowing linkage of a user role to a specific command access level, and the DAC
system will allow users with sufficient access to give access to others. The Biba model can be used to ensure integrity of
the system, so a user can only assign (write) access levels that is below their access level.

At the moment, only the MAC system is implemented.

#### Annotations
Some annotations aid in the control of commands. Since commands do not need to be registered manually, there needs to be
a way for a command class to exist without being registered, for purposes such as testing. This is accomplished by the
`Disabled` annotation. There are also flag annotations such as `BotFriendly` and `GuildOnly`. Lastly, the
`DeclareCommandAccessLevel` annotation lets the command declare its access level. It is possible to declare the access
levels for private channel and guild channel separately, which is useful for commands such as the print command. It will
cause no harm for the bot to wipe its own record from a private channel; however, wiping the message history of everyone
on a guild channel will mostly likely prove disastrous.

### Console
A basic CLI is available for the program. At the moment, the only thing implemented is starting the proper exit of the 
application.

## TODOs
Finally, here is a non-inclusive list of things I am considering getting done:

### Short term:
- Upgrade CommandContainer parser
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

### Long term (mostly speculative):
- Interpreter with discord gui elements and some capabilities
- Compute shaders for computation of stuff
- JIT stuff through command
- Move to GraalVM

## Change Log
This section will include major program decision changes (when I feel like it)

### Moving on from Staged Commands to Services (2021/05/29)

As per what I have never mentioned before, *Cassandra* is the fifth iteration of my Discord Bot program 
(V 0.5, if you will). Previous versions included implementations of "staged commands", which are similar to the basic
commands, except they expect multiple inputs over time. This was fine with previous implementations of commands.

However, as *Cassandra* slowly became the first iteration where I actually planned for the program components, I came to
the realization that there is a more fundamental difference between simple commands and staged ones, in that the latter
is much, much more complex. Further, using Spring, it is quite convenient to scope the former as singletons, and the 
latter prototypes. (Also, the command package is getting quite large.) As such, I will be separating out "staged commands"
as services rather than commands.

This does not mean they will have nothing to do with commands: it is likely that they will be initiated by commands, but
we will see.
