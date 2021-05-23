package com.efonian.cassandra.discord.event;

import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.events.channel.category.CategoryCreateEvent;
import net.dv8tion.jda.api.events.channel.category.CategoryDeleteEvent;
import net.dv8tion.jda.api.events.channel.category.GenericCategoryEvent;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.category.update.GenericCategoryUpdateEvent;
import net.dv8tion.jda.api.events.channel.store.GenericStoreChannelEvent;
import net.dv8tion.jda.api.events.channel.store.StoreChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.store.StoreChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.store.update.GenericStoreChannelUpdateEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.text.GenericTextChannelEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.text.update.*;
import net.dv8tion.jda.api.events.channel.voice.GenericVoiceChannelEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.voice.update.*;
import net.dv8tion.jda.api.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.api.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.api.events.emote.GenericEmoteEvent;
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateNameEvent;
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateRolesEvent;
import net.dv8tion.jda.api.events.emote.update.GenericEmoteUpdateEvent;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.events.guild.invite.GenericGuildInviteEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.events.guild.member.update.GenericGuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdatePendingEvent;
import net.dv8tion.jda.api.events.guild.override.GenericPermissionOverrideEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideCreateEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideDeleteEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideUpdateEvent;
import net.dv8tion.jda.api.events.guild.update.*;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.events.http.HttpRequestEvent;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.events.message.guild.*;
import net.dv8tion.jda.api.events.message.guild.react.*;
import net.dv8tion.jda.api.events.message.priv.*;
import net.dv8tion.jda.api.events.message.priv.react.GenericPrivateMessageReactionEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.message.react.*;
import net.dv8tion.jda.api.events.role.GenericRoleEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.*;
import net.dv8tion.jda.api.events.self.*;
import net.dv8tion.jda.api.events.user.GenericUserEvent;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.events.user.update.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
@Scope(value= ConfigurableBeanFactory.SCOPE_SINGLETON)
@ConditionalOnBean(EventListenerManager.class)
@ConditionalOnProperty(value = "cassandra.discord.init", havingValue = "true")
public class EventListener extends ListenerAdapter {
    EventListenerManager manager;
    
    @Override
    public void onGenericEvent(@Nonnull GenericEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericUpdate(@Nonnull UpdateEvent<?, ?> event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onRawGateway(@Nonnull RawGatewayEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGatewayPing(@Nonnull GatewayPingEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onResumed(@Nonnull ResumedEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onReconnected(@Nonnull ReconnectedEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onDisconnect(@Nonnull DisconnectEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onShutdown(@Nonnull ShutdownEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onStatusChange(@Nonnull StatusChangeEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onException(@Nonnull ExceptionEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onUserUpdateName(@Nonnull UserUpdateNameEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onUserUpdateDiscriminator(@Nonnull UserUpdateDiscriminatorEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onUserUpdateAvatar(@Nonnull UserUpdateAvatarEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onUserUpdateOnlineStatus(@Nonnull UserUpdateOnlineStatusEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onUserUpdateActivityOrder(@Nonnull UserUpdateActivityOrderEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onUserUpdateFlags(@Nonnull UserUpdateFlagsEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onUserTyping(@Nonnull UserTypingEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onUserActivityStart(@Nonnull UserActivityStartEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onUserActivityEnd(@Nonnull UserActivityEndEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onUserUpdateActivities(@Nonnull UserUpdateActivitiesEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onSelfUpdateAvatar(@Nonnull SelfUpdateAvatarEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onSelfUpdateMFA(@Nonnull SelfUpdateMFAEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onSelfUpdateName(@Nonnull SelfUpdateNameEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onSelfUpdateVerified(@Nonnull SelfUpdateVerifiedEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMessageEmbed(@Nonnull GuildMessageEmbedEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMessageReactionRemoveAll(@Nonnull GuildMessageReactionRemoveAllEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMessageReactionRemoveEmote(@Nonnull GuildMessageReactionRemoveEmoteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onPrivateMessageUpdate(@Nonnull PrivateMessageUpdateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onPrivateMessageDelete(@Nonnull PrivateMessageDeleteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onPrivateMessageEmbed(@Nonnull PrivateMessageEmbedEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onPrivateMessageReactionAdd(@Nonnull PrivateMessageReactionAddEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onPrivateMessageReactionRemove(@Nonnull PrivateMessageReactionRemoveEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onMessageUpdate(@Nonnull MessageUpdateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onMessageBulkDelete(@Nonnull MessageBulkDeleteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onMessageEmbed(@Nonnull MessageEmbedEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onMessageReactionRemove(@Nonnull MessageReactionRemoveEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onMessageReactionRemoveAll(@Nonnull MessageReactionRemoveAllEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onMessageReactionRemoveEmote(@Nonnull MessageReactionRemoveEmoteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onPermissionOverrideDelete(@Nonnull PermissionOverrideDeleteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onPermissionOverrideUpdate(@Nonnull PermissionOverrideUpdateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onPermissionOverrideCreate(@Nonnull PermissionOverrideCreateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onStoreChannelDelete(@Nonnull StoreChannelDeleteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onStoreChannelUpdateName(@Nonnull StoreChannelUpdateNameEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onStoreChannelUpdatePosition(@Nonnull StoreChannelUpdatePositionEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onStoreChannelCreate(@Nonnull StoreChannelCreateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onTextChannelDelete(@Nonnull TextChannelDeleteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onTextChannelUpdateName(@Nonnull TextChannelUpdateNameEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onTextChannelUpdateTopic(@Nonnull TextChannelUpdateTopicEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onTextChannelUpdatePosition(@Nonnull TextChannelUpdatePositionEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onTextChannelUpdateNSFW(@Nonnull TextChannelUpdateNSFWEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onTextChannelUpdateParent(@Nonnull TextChannelUpdateParentEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onTextChannelUpdateSlowmode(@Nonnull TextChannelUpdateSlowmodeEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onTextChannelCreate(@Nonnull TextChannelCreateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onTextChannelUpdateNews(@Nonnull TextChannelUpdateNewsEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onVoiceChannelDelete(@Nonnull VoiceChannelDeleteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onVoiceChannelUpdateName(@Nonnull VoiceChannelUpdateNameEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onVoiceChannelUpdatePosition(@Nonnull VoiceChannelUpdatePositionEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onVoiceChannelUpdateUserLimit(@Nonnull VoiceChannelUpdateUserLimitEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onVoiceChannelUpdateBitrate(@Nonnull VoiceChannelUpdateBitrateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onVoiceChannelUpdateParent(@Nonnull VoiceChannelUpdateParentEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onVoiceChannelCreate(@Nonnull VoiceChannelCreateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onCategoryDelete(@Nonnull CategoryDeleteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onCategoryUpdateName(@Nonnull CategoryUpdateNameEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onCategoryUpdatePosition(@Nonnull CategoryUpdatePositionEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onCategoryCreate(@Nonnull CategoryCreateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildReady(@Nonnull GuildReadyEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildAvailable(@Nonnull GuildAvailableEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUnavailable(@Nonnull GuildUnavailableEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onUnavailableGuildJoined(@Nonnull UnavailableGuildJoinedEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onUnavailableGuildLeave(@Nonnull UnavailableGuildLeaveEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateAfkChannel(@Nonnull GuildUpdateAfkChannelEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateSystemChannel(@Nonnull GuildUpdateSystemChannelEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateAfkTimeout(@Nonnull GuildUpdateAfkTimeoutEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateExplicitContentLevel(@Nonnull GuildUpdateExplicitContentLevelEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateIcon(@Nonnull GuildUpdateIconEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateMFALevel(@Nonnull GuildUpdateMFALevelEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateName(@Nonnull GuildUpdateNameEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateNotificationLevel(@Nonnull GuildUpdateNotificationLevelEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateOwner(@Nonnull GuildUpdateOwnerEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateRegion(@Nonnull GuildUpdateRegionEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateSplash(@Nonnull GuildUpdateSplashEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateVerificationLevel(@Nonnull GuildUpdateVerificationLevelEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateLocale(@Nonnull GuildUpdateLocaleEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateFeatures(@Nonnull GuildUpdateFeaturesEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateVanityCode(@Nonnull GuildUpdateVanityCodeEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateBanner(@Nonnull GuildUpdateBannerEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateDescription(@Nonnull GuildUpdateDescriptionEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateBoostTier(@Nonnull GuildUpdateBoostTierEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateBoostCount(@Nonnull GuildUpdateBoostCountEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateMaxMembers(@Nonnull GuildUpdateMaxMembersEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateMaxPresences(@Nonnull GuildUpdateMaxPresencesEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildInviteCreate(@Nonnull GuildInviteCreateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildInviteDelete(@Nonnull GuildInviteDeleteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMemberUpdateBoostTime(@Nonnull GuildMemberUpdateBoostTimeEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildVoiceMute(@Nonnull GuildVoiceMuteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildVoiceDeafen(@Nonnull GuildVoiceDeafenEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildVoiceGuildMute(@Nonnull GuildVoiceGuildMuteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildVoiceGuildDeafen(@Nonnull GuildVoiceGuildDeafenEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildVoiceSelfMute(@Nonnull GuildVoiceSelfMuteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildVoiceSelfDeafen(@Nonnull GuildVoiceSelfDeafenEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildVoiceSuppress(@Nonnull GuildVoiceSuppressEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildVoiceStream(@Nonnull GuildVoiceStreamEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildTimeout(@Nonnull GuildTimeoutEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateRulesChannel(@Nonnull GuildUpdateRulesChannelEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildUpdateCommunityUpdatesChannel(@Nonnull GuildUpdateCommunityUpdatesChannelEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMemberUpdate(@Nonnull GuildMemberUpdateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGuildMemberUpdatePending(@Nonnull GuildMemberUpdatePendingEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onRoleUpdateColor(@Nonnull RoleUpdateColorEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onRoleUpdateHoisted(@Nonnull RoleUpdateHoistedEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onRoleUpdateMentionable(@Nonnull RoleUpdateMentionableEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onRoleUpdateName(@Nonnull RoleUpdateNameEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onRoleUpdatePermissions(@Nonnull RoleUpdatePermissionsEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onRoleUpdatePosition(@Nonnull RoleUpdatePositionEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onEmoteAdded(@Nonnull EmoteAddedEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onEmoteRemoved(@Nonnull EmoteRemovedEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onEmoteUpdateName(@Nonnull EmoteUpdateNameEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onEmoteUpdateRoles(@Nonnull EmoteUpdateRolesEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onHttpRequest(@Nonnull HttpRequestEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericMessage(@Nonnull GenericMessageEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericMessageReaction(@Nonnull GenericMessageReactionEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericGuildMessage(@Nonnull GenericGuildMessageEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericGuildMessageReaction(@Nonnull GenericGuildMessageReactionEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericPrivateMessage(@Nonnull GenericPrivateMessageEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericPrivateMessageReaction(@Nonnull GenericPrivateMessageReactionEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericUser(@Nonnull GenericUserEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericUserPresence(@Nonnull GenericUserPresenceEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericSelfUpdate(@Nonnull GenericSelfUpdateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericStoreChannel(@Nonnull GenericStoreChannelEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericStoreChannelUpdate(@Nonnull GenericStoreChannelUpdateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericTextChannel(@Nonnull GenericTextChannelEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericTextChannelUpdate(@Nonnull GenericTextChannelUpdateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericVoiceChannel(@Nonnull GenericVoiceChannelEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericVoiceChannelUpdate(@Nonnull GenericVoiceChannelUpdateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericCategory(@Nonnull GenericCategoryEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericCategoryUpdate(@Nonnull GenericCategoryUpdateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericGuild(@Nonnull GenericGuildEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericGuildUpdate(@Nonnull GenericGuildUpdateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericGuildInvite(@Nonnull GenericGuildInviteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericGuildMember(@Nonnull GenericGuildMemberEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericGuildMemberUpdate(@Nonnull GenericGuildMemberUpdateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericGuildVoice(@Nonnull GenericGuildVoiceEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericRole(@Nonnull GenericRoleEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericRoleUpdate(@Nonnull GenericRoleUpdateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericEmote(@Nonnull GenericEmoteEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericEmoteUpdate(@Nonnull GenericEmoteUpdateEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    @Override
    public void onGenericPermissionOverride(@Nonnull GenericPermissionOverrideEvent event) {
        manager.handleEvent(event.getClass(), event);
    }
    
    
    
    @Autowired
    private void setManager(EventListenerManager manager) {
        this.manager = manager;
    }
}
