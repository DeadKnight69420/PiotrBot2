package com.ramxd.PiotrBot2;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class pingCommand {
    /*interface Command {
        // Since we are expecting to do reactive things in this method, like
        // send a message, then this method will also return a reactive type.
        Mono<Void> execute(MessageCreateEvent event);}
    public static final Map<String, Command> commands = new HashMap<>();
    static {
        commands.put("ping", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("Pong!"))
                .then());}*/
}
