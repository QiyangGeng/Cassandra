package com.efonian.cassandra.discord.command.annotation;

import com.efonian.cassandra.discord.command.CommandAccessLevel;

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
