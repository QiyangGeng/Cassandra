package com.efonian.cassandra.discord.services;

import net.dv8tion.jda.api.entities.User;

public abstract class Service {
    private User client;
    
    // Define stages
    
    protected User getClient() {
        return client;
    }
}
