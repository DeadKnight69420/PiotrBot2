package com.ramxd.PiotrBot2;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.voice.AudioProvider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PiotrBot2  {
    static final class LavaPlayerAudioProvider extends AudioProvider {

        private final AudioPlayer player;
        private final MutableAudioFrame frame = new MutableAudioFrame();

        public LavaPlayerAudioProvider(final AudioPlayer player) {
            super(
                    ByteBuffer.allocate(
                            StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()
                    )
            );
            frame.setBuffer(getBuffer());
            this.player = player;
        }

        @Override
        public boolean provide() {
            final boolean didProvide = player.provide(frame);
            if (didProvide) {
                getBuffer().flip();
            }
            return didProvide;
        }
    }
    static final class TrackScheduler implements AudioLoadResultHandler {

        private final AudioPlayer player;

        public TrackScheduler(final AudioPlayer player) {
            this.player = player;
        }

        @Override
        public void trackLoaded(final AudioTrack track) {
            player.playTrack(track);
        }

        @Override
        public void playlistLoaded(final AudioPlaylist playlist) {
        }

        @Override
        public void noMatches() {
        }

        @Override
        public void loadFailed(final FriendlyException exception) {
        }

    }
    public static void main(String[] args) {
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();


        playerManager.getConfiguration()
                .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);


        AudioSourceManagers.registerRemoteSources(playerManager);


        final AudioPlayer player = playerManager.createPlayer();



        AudioProvider provider = new LavaPlayerAudioProvider(player);

        commands.put("join", event -> Mono.justOrEmpty(event.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                .flatMap(channel -> channel.join(spec -> spec.setProvider(provider)))
                .then());
        final TrackScheduler scheduler = new TrackScheduler(player);
        commands.put("play", event -> Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> Arrays.asList(content.split(" ")))
                .doOnNext(command -> playerManager.loadItem(command.get(1), scheduler))
                .then());




        final GatewayDiscordClient client = DiscordClientBuilder.create(args[0]).build()
                .login()
                .block();
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(event -> Mono.just(event.getMessage().getContent())
                        .flatMap(content -> Flux.fromIterable(commands.entrySet())
                                .filter(entry -> content.startsWith('!' + entry.getKey()))
                                .flatMap(entry -> entry.getValue().execute(event))
                                .next()))
                .subscribe();
        client.onDisconnect().block();


    }
    interface Command {
        Mono<Void> execute(MessageCreateEvent event);
    }
    public static final Map<String, Command> commands = new HashMap<>();
    static {
        commands.put("ping", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("Pong!"))
                .then());
    }

    }



