package com.efonian.cassandra.discord.command;

/**
 * A simple enum for levels of access to commands
 */
public enum CommandAccessLevel {
    UNTRUSTED,
    LIMITED,
    REGULAR,
    TRUSTED,
    MODERATOR,
    ADMIN,
    DYNAMIC,
    FULL,
    NULL
}
