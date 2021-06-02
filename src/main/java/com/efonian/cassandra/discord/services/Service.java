package com.efonian.cassandra.discord.services;

import net.dv8tion.jda.api.entities.User;

public abstract class Service {
    private User client;
    
    // Define stages
    
    public abstract void start();
    
    public abstract void stop();
    
    protected User getClient() {
        return client;
    }
}
