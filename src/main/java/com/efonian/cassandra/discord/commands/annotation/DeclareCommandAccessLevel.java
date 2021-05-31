package com.efonian.cassandra.discord.commands.annotation;

import com.efonian.cassandra.discord.commands.CommandAccessLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DeclareCommandAccessLevel {
    CommandAccessLevel accessLevel() default CommandAccessLevel.NULL;
    CommandAccessLevel guildAccessLevel() default CommandAccessLevel.REGULAR;
    CommandAccessLevel privateAccessLevel() default CommandAccessLevel.REGULAR;
}
