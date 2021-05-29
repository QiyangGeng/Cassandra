package com.efonian.cassandra.discord.event;

import com.efonian.cassandra.discord.DiscordBot;
import com.efonian.cassandra.util.UtilRuntime;
import net.dv8tion.jda.api.events.GenericEvent;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


// add miscellaneous "prebuilt" listeners, such as ones which listens to specific channels/guilds
/**
 * The purpose of this class is to provide an easy to use interface for waiting for Discord events. The handleEvent method
 * is called when any event comes through, and this manager will check whether this event is being listened to, and distribute
 * the event to an event operator (functional interface). Any operator should avoid any complicated or time-consuming
 * operations, as the listeners share a single thread. For such operations, it is recommended to use another worker thread
 * to complete the operation; for commands, use the thread pool inside the command manager.
 */
@Component
@Scope(value= ConfigurableBeanFactory.SCOPE_SINGLETON)
@ConditionalOnBean(DiscordBot.class)
@ConditionalOnProperty(value = "cassandra.discord.init", havingValue = "true")
public final class EventListenerManager {
    private static final ScheduledExecutorService deletionScheduler = Executors.newSingleThreadScheduledExecutor();
    
    private final Map<Class<? extends GenericEvent>, List<Consumer<? extends GenericEvent>>> eventListenerRegistry = new ConcurrentHashMap<>();
    
    private long maxResponseNumber = -1;
    
    /**
     * Handles event caught by the listener. This should only be called by the methods in the <c>EventListener</c> class.
     */
    <E extends GenericEvent> void handleEvent(Class<E> eventType, GenericEvent event) {
        // The maxResponseNumber check removes duplicate events which are sent because... networking.
        if(maxResponseNumber < event.getResponseNumber() && eventListenerRegistry.containsKey(eventType)) {
            final List<Consumer<? extends GenericEvent>> registeredListeners = eventListenerRegistry.get(eventType);
            registeredListeners.forEach(eventOperator -> ((Consumer<E>) eventOperator).accept(eventType.cast(event)));
            maxResponseNumber = event.getResponseNumber();
        }
    }
    
    /**
     * Used to register a persistent eventListener.
     * @param eventType The type of event to listen to
     * @param operator What to do with the event
     * @return This manager to enable chained calls
     */
    public <E extends GenericEvent> EventListenerManager registerOperator(@Nonnull Class<E> eventType,
                                                                          @Nonnull Consumer<E> operator) {
        if(eventListenerRegistry.containsKey(eventType))
            eventListenerRegistry.get(eventType).add(operator);
        else
            eventListenerRegistry.put(eventType, new ArrayList<>(Collections.singletonList(operator)));
        return this;
    }
    
    /**
     * Used to register a persistent eventListener with a lifetime.
     * @param eventType The type of event to listen to
     * @param operator What to do with the event
     * @param lifetime How long to wait for this event to occur
     * @param unit The time unit for the given lifetime
     * @return This manager to enable chained calls
     */
    public <E extends GenericEvent> EventListenerManager registerOperator(@Nonnull Class<E> eventType,
                                                                          @Nonnull Consumer<E> operator,
                                                                          long lifetime, TimeUnit unit) {
        if(lifetime > 0) {
            registerOperator(eventType, operator);
            deletionScheduler.schedule(() -> {
                if(eventListenerRegistry.containsKey(eventType))
                    eventListenerRegistry.get(eventType).remove(operator);
            }, lifetime, unit);
        }
        return this;
    }
    
    /**
     * Removes an operator.
     * @param operator  The reference to the registered operator
     * @return This manager to enable chained calls
     */
    public <E extends GenericEvent> EventListenerManager removeOperator(@Nonnull Consumer<E> operator) {
        eventListenerRegistry.values().forEach(list -> list.remove(operator));
        return this;
    }
    
    public int countRegisteredOperators() {
        int sum = 0;
        for(List<Consumer<? extends GenericEvent>> list: eventListenerRegistry.values())
            sum += list.size();
        return sum;
    }
    
    @PreDestroy
    private void shutdown() {
        UtilRuntime.shutDownExecutorService(deletionScheduler);
    }
}
