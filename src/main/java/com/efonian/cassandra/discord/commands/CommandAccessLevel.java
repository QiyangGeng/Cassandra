package com.efonian.cassandra.discord.commands;

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
    FULL,
    NULL
}
