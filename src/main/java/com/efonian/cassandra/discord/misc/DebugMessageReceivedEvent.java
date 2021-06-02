package com.efonian.cassandra.discord.misc;

import com.efonian.cassandra.discord.DiscordBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import org.apache.commons.collections4.Bag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Formatter;
import java.util.List;

public class DebugMessageReceivedEvent extends MessageReceivedEvent {
    private Instant instantCreated = Instant.now();
    
    public DebugMessageReceivedEvent(@NotNull JDA api, String contentRaw) {
        super(api, -1, new Message() {
            @Nullable
            @Override
            public Message getReferencedMessage() {
                return null;
            }
    
            @NotNull
            @Override
            public List<User> getMentionedUsers() {
                return List.of();
            }
    
            @NotNull
            @Override
            public Bag<User> getMentionedUsersBag() {
                // wtf is a bag
                return null;
            }
    
            @NotNull
            @Override
            public List<TextChannel> getMentionedChannels() {
                return List.of();
            }
    
            @NotNull
            @Override
            public Bag<TextChannel> getMentionedChannelsBag() {
                return null;
            }
    
            @NotNull
            @Override
            public List<Role> getMentionedRoles() {
                return List.of();
            }
    
            @NotNull
            @Override
            public Bag<Role> getMentionedRolesBag() {
                return null;
            }
    
            @NotNull
            @Override
            public List<Member> getMentionedMembers(@NotNull Guild guild) {
                return List.of();
            }
    
            @NotNull
            @Override
            public List<Member> getMentionedMembers() {
                return List.of();
            }
    
            @NotNull
            @Override
            public List<IMentionable> getMentions(@NotNull MentionType... mentionTypes) {
                return List.of();
            }
    
            @Override
            public boolean isMentioned(@NotNull IMentionable iMentionable, @NotNull MentionType... mentionTypes) {
                return false;
            }
    
            @Override
            public boolean mentionsEveryone() {
                return false;
            }
    
            @Override
            public boolean isEdited() {
                return false;
            }
    
            @Nullable
            @Override
            public OffsetDateTime getTimeEdited() {
                return null;
            }
    
            @NotNull
            @Override
            public User getAuthor() {
                return DiscordBot.getSelfUser();
            }
    
            @Nullable
            @Override
            public Member getMember() {
                return null;
            }
    
            @NotNull
            @Override
            public String getJumpUrl() {
                // wtf is jump url
                return null;
            }
    
            @NotNull
            @Override
            public String getContentDisplay() {
                // ???
                return null;
            }
    
            @NotNull
            @Override
            public String getContentRaw() {
                return contentRaw;
            }
    
            @NotNull
            @Override
            public String getContentStripped() {
                return contentRaw;
            }
    
            @NotNull
            @Override
            public List<String> getInvites() {
                return List.of();
            }
    
            @Nullable
            @Override
            public String getNonce() {
                return null;
            }
    
            @Override
            public boolean isFromType(@NotNull ChannelType channelType) {
                return false;
            }
    
            @NotNull
            @Override
            public ChannelType getChannelType() {
                return ChannelType.PRIVATE;
            }
    
            @Override
            public boolean isWebhookMessage() {
                return false;
            }
    
            @NotNull
            @Override
            public MessageChannel getChannel() {
                return null;
            }
    
            @NotNull
            @Override
            public PrivateChannel getPrivateChannel() {
                return null;
            }
    
            @NotNull
            @Override
            public TextChannel getTextChannel() {
                return null;
            }
    
            @Nullable
            @Override
            public Category getCategory() {
                return null;
            }
    
            @NotNull
            @Override
            public Guild getGuild() {
                return null;
            }
    
            @NotNull
            @Override
            public List<Attachment> getAttachments() {
                return null;
            }
    
            @NotNull
            @Override
            public List<MessageEmbed> getEmbeds() {
                return null;
            }
    
            @NotNull
            @Override
            public List<Emote> getEmotes() {
                return null;
            }
    
            @NotNull
            @Override
            public Bag<Emote> getEmotesBag() {
                return null;
            }
    
            @NotNull
            @Override
            public List<MessageReaction> getReactions() {
                return null;
            }
    
            @NotNull
            @Override
            public List<MessageSticker> getStickers() {
                return null;
            }
    
            @Override
            public boolean isTTS() {
                return false;
            }
    
            @Nullable
            @Override
            public MessageActivity getActivity() {
                return null;
            }
    
            @NotNull
            @Override
            public MessageAction editMessage(@NotNull CharSequence charSequence) {
                return null;
            }
    
            @NotNull
            @Override
            public MessageAction editMessage(@NotNull MessageEmbed messageEmbed) {
                return null;
            }
    
            @NotNull
            @Override
            public MessageAction editMessageFormat(@NotNull String s, @NotNull Object... objects) {
                return null;
            }
    
            @NotNull
            @Override
            public MessageAction editMessage(@NotNull Message message) {
                return null;
            }
    
            @NotNull
            @Override
            public AuditableRestAction<Void> delete() {
                return null;
            }
    
            @NotNull
            @Override
            public JDA getJDA() {
                return null;
            }
    
            @Override
            public boolean isPinned() {
                return false;
            }
    
            @NotNull
            @Override
            public RestAction<Void> pin() {
                return null;
            }
    
            @NotNull
            @Override
            public RestAction<Void> unpin() {
                return null;
            }
    
            @NotNull
            @Override
            public RestAction<Void> addReaction(@NotNull Emote emote) {
                return null;
            }
    
            @NotNull
            @Override
            public RestAction<Void> addReaction(@NotNull String s) {
                return null;
            }
    
            @NotNull
            @Override
            public RestAction<Void> clearReactions() {
                return null;
            }
    
            @NotNull
            @Override
            public RestAction<Void> clearReactions(@NotNull String s) {
                return null;
            }
    
            @NotNull
            @Override
            public RestAction<Void> clearReactions(@NotNull Emote emote) {
                return null;
            }
    
            @NotNull
            @Override
            public RestAction<Void> removeReaction(@NotNull Emote emote) {
                return null;
            }
    
            @NotNull
            @Override
            public RestAction<Void> removeReaction(@NotNull Emote emote, @NotNull User user) {
                return null;
            }
    
            @NotNull
            @Override
            public RestAction<Void> removeReaction(@NotNull String s) {
                return null;
            }
    
            @NotNull
            @Override
            public RestAction<Void> removeReaction(@NotNull String s, @NotNull User user) {
                return null;
            }
    
            @NotNull
            @Override
            public ReactionPaginationAction retrieveReactionUsers(@NotNull Emote emote) {
                return null;
            }
    
            @NotNull
            @Override
            public ReactionPaginationAction retrieveReactionUsers(@NotNull String s) {
                return null;
            }
    
            @Nullable
            @Override
            public MessageReaction.ReactionEmote getReactionByUnicode(@NotNull String s) {
                return null;
            }
    
            @Nullable
            @Override
            public MessageReaction.ReactionEmote getReactionById(@NotNull String s) {
                return null;
            }
    
            @Nullable
            @Override
            public MessageReaction.ReactionEmote getReactionById(long l) {
                return null;
            }
    
            @NotNull
            @Override
            public AuditableRestAction<Void> suppressEmbeds(boolean b) {
                return null;
            }
    
            @NotNull
            @Override
            public RestAction<Message> crosspost() {
                return null;
            }
    
            @Override
            public boolean isSuppressedEmbeds() {
                return false;
            }
    
            @NotNull
            @Override
            public EnumSet<MessageFlag> getFlags() {
                return null;
            }
    
            @NotNull
            @Override
            public MessageType getType() {
                return MessageType.DEFAULT;
            }
    
            @Override
            public void formatTo(Formatter formatter, int flags, int width, int precision) {
        
            }
    
            @Override
            public long getIdLong() {
                return -1;
            }
        });
    }
}
