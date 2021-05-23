package com.efonian.cassandra.discord.event;

import net.dv8tion.jda.api.events.GenericEvent;

@FunctionalInterface
public interface EventOperator<E extends GenericEvent> {
    boolean operate(E event);
}
