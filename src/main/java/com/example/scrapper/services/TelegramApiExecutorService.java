package com.example.scrapper.services;


import com.example.scrapper.configuration.TelegramProperties;
import com.example.scrapper.domain.models.telegram.ChatEvent;
import it.tdlight.common.ResultHandler;
import it.tdlight.common.TelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.tdlight.ClientManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import com.example.scrapper.domain.models.telegram.User;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.example.scrapper.services.TelegramApiExecutorService.AuthMessage.*;


@Service
@Scope("singleton")
@Slf4j
@RequiredArgsConstructor
public class TelegramApiExecutorService {
    /**
     * Fetched chats
     */
    private static final ConcurrentHashMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<>();

    /**
     * Auth result
     */
    private static volatile String result;

    /**
     * Spin wait flag
     */
    private static final AtomicBoolean next = new AtomicBoolean(false);

    /**
     * Client configuration
     */
    private final TelegramProperties telegramProperties;

    /**
     * Client
     */
    private TelegramClient client;

    /**
     * Auth query string
     */
    private volatile String queryParam;

    /**
     * Current auth state
     */
    private TdApi.AuthorizationState authorizationState;

    @PostConstruct
    private synchronized void createClient() {
        client = ClientManager.create();
        client.initialize(new UpdateHandler(), null, null);
        client.execute(new TdApi.SetLogVerbosityLevel(0));
    }

    /**
     * Default update handler
     */
    private class UpdateHandler implements ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.UpdateAuthorizationState.CONSTRUCTOR -> onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                case TdApi.UpdateNewChat.CONSTRUCTOR -> {
                    var updateNewChat = (TdApi.UpdateNewChat) object;
                    var chat = updateNewChat.chat;
                    chats.put(chat.id, chat);
                }

                default -> {
                }
            }
        }
    }

    /**
     * Get channels by title substring
     *
     * @param titleSubstring title substring
     *
     * @return id - title pairs
     */
    @SneakyThrows
    public List<ImmutablePair<Long, String>> getChannels(String titleSubstring) {
        var errorMessage = new AtomicReference<String>();

        next.set(false);

        client.send(new TdApi.GetChats(new TdApi.ChatListMain(), Long.MAX_VALUE, 0, Integer.MAX_VALUE), object -> {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR -> errorMessage.set("Receive an error for getChannels: " + object);
                case TdApi.Chats.CONSTRUCTOR -> log.info("Received chats: %s".formatted(object));
                default -> log.error("Receive wrong response from TDLib: %s".formatted(object));
            }
            next.set(true);
        });

        while (!next.get())
            Thread.onSpinWait();

        if (errorMessage.get() != null)
            throw new IllegalStateException(errorMessage.get());

        return chats.values()
                .stream()
                .filter(c -> c.type.getConstructor() == TdApi.ChatTypeSupergroup.CONSTRUCTOR && ((TdApi.ChatTypeSupergroup) c.type).isChannel)
                .filter(c -> c.title.toLowerCase().contains(titleSubstring.toLowerCase()))
                .sorted(Comparator.comparing(c -> c.title))
                .map(c -> new ImmutablePair<>(c.id, c.title))
                .toList();
    }

    /**
     * Get joined events
     *
     * @param channelId Telegram id
     *
     * @return Joined events
     */
    @SuppressWarnings({"LoopConditionNotUpdatedInsideLoop"})
    public List<TdApi.ChatEvent> getJoinedLogUsers(Long channelId, List<TdApi.ChatEvent> allChatEvents, long fromEventId) {
        var filter = new TdApi.ChatEventLogFilters();
        filter.memberJoins = true;

        // You can't get channel event log before fetching this channel
        if (!chats.containsKey(channelId))
            getChannels("");

        while (!chats.containsKey(channelId))
            Thread.onSpinWait();

        next.set(false);
        var currentStepEvents = new ArrayList<TdApi.ChatEvent>();
        client.send(new TdApi.GetChatEventLog(channelId, null, fromEventId, 100, filter, new int[]{}), object -> {
            if (object.getConstructor() == TdApi.ChatEvents.CONSTRUCTOR)
                currentStepEvents.addAll(Arrays.asList(((TdApi.ChatEvents) object).events));
            next.set(true);
        });

        while (!next.get())
            Thread.onSpinWait();

        if (!currentStepEvents.isEmpty()) {
            allChatEvents.addAll(currentStepEvents);
            var minEventIdOpt = currentStepEvents.stream()
                    .map(e -> e.id)
                    .min(Long::compareTo);

            if (minEventIdOpt.isEmpty())
                throw new IllegalStateException("The chat event log empty!");

            getJoinedLogUsers(channelId, allChatEvents, minEventIdOpt.get());
        }

        return allChatEvents;
    }

    /**
     * Get joined domain events
     *
     * @param channelId Telegram id
     *
     * @return Joined domain events
     */
    public List<ChatEvent> getJoinedDomainEventsByChannelId(Long channelId) {
        var domainEvents = new ConcurrentHashMap<Date, ChatEvent>();

        var events = getJoinedLogUsers(channelId, new ArrayList<>(), 0);

        var counter = new AtomicInteger(events.size());
        events.forEach(e ->
                client.send(new TdApi.GetUser(e.userId), object -> {
                    if (object.getConstructor() == TdApi.User.CONSTRUCTOR) {
                        var tgUser = (TdApi.User) object;

                        var user = new User(tgUser.username, tgUser.firstName, tgUser.id);
                        var eventDate = new Date(e.date * 1000L);

                        var domainEvent = new ChatEvent(user, eventDate, e.action);

                        domainEvents.put(eventDate, domainEvent);
                    }

                    counter.set(counter.decrementAndGet());
                }));

        while (counter.get() != 0)
            Thread.onSpinWait();

        return new ArrayList<>(domainEvents.values());
    }

    //region Auth

    /**
     * Authorize
     *
     * @param param String for auth step
     *
     * @return Auth step result
     */
    @SneakyThrows
    public String authorize(String param) {
        queryParam = param;

        next.set(false);
        if (authorizationState != null)
            onAuthorizationStateUpdated(authorizationState);

        while (!next.get())
            Thread.onSpinWait();

        queryParam = null;

        return result;
    }

    /*
     * Logout
     */
    @SneakyThrows
    public String logout() {
        next.set(false);

        client.send(new TdApi.LogOut(), new AuthorizationRequestHandler());

        while (!next.get())
            Thread.onSpinWait();

        return result;
    }

    /**
     * Authorization steps actions
     *
     * @param authorizationState current auth state
     */
    private void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
        if (authorizationState != null)
            this.authorizationState = authorizationState;

        switch (this.authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                var parameters = new TdApi.TdlibParameters();

                parameters.useMessageDatabase = telegramProperties.isUseMessageDatabase();
                parameters.useSecretChats = telegramProperties.isUseSecretChats();
                parameters.apiId = telegramProperties.getApiId();
                parameters.apiHash = telegramProperties.getApiHash();
                parameters.systemLanguageCode = telegramProperties.getSystemLanguageCode();
                parameters.deviceModel = telegramProperties.getDeviceModel();
                parameters.applicationVersion = telegramProperties.getApplicationVersion();
                parameters.enableStorageOptimizer = telegramProperties.isEnableStorageOptimizer();

                client.send(new TdApi.SetTdlibParameters(parameters), new TelegramApiExecutorService.AuthorizationRequestHandler());
                return;
            }
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                client.send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
                return;
            }
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                if (queryParam == null) {
                    setAuthResultAndGoNext(ENTER_PHONE_NUMBER);
                    break;
                }
                client.send(new TdApi.SetAuthenticationPhoneNumber(queryParam, null), new TelegramApiExecutorService.AuthorizationRequestHandler());
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                if (queryParam == null) {
                    setAuthResultAndGoNext(ENTER_AUTHENTICATION_CODE);
                    break;
                }
                client.send(new TdApi.CheckAuthenticationCode(queryParam), new AuthorizationRequestHandler());
            }

            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                if (queryParam == null) {
                    setAuthResultAndGoNext(ENTER_PASSWORD);
                    break;
                }
                client.send(new TdApi.CheckAuthenticationPassword(queryParam), new TelegramApiExecutorService.AuthorizationRequestHandler());
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR -> setAuthResultAndGoNext(YOU_ARE_AUTHORIZED);
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR -> setAuthResultAndGoNext(YOU_ARE_LOGGED_OUT);
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR -> {
                createClient();
                setAuthResultAndGoNext(YOU_ARE_LOGGED_OUT);
            }

            default -> setAuthResultAndGoNext(UNSUPPORTED_STATE);
        }

        queryParam = null;
    }

    /**
     * Set auth result and go to nest auth step
     *
     * @param message Auth result message
     */
    private static void setAuthResultAndGoNext(AuthMessage message) {
        result = message.toString();
        next.set(true);
    }

    enum AuthMessage {
        ENTER_PHONE_NUMBER,
        ENTER_AUTHENTICATION_CODE,
        ENTER_PASSWORD,
        YOU_ARE_AUTHORIZED,
        YOU_ARE_LOGGED_OUT,
        UNSUPPORTED_STATE
    }

    private class AuthorizationRequestHandler implements ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR -> {
                    log.error("Receive an error: %s".formatted(object));
                    onAuthorizationStateUpdated(null); // repeat last action
                }
                case TdApi.Ok.CONSTRUCTOR -> {
                    // result already received through UpdateAuthorizationState, nothing to do
                }
                default -> log.error("Receive wrong response from TDLib: %s".formatted(object));
            }
        }
    }

//endregion
}