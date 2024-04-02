package com.example.scrapper.domain.models.telegram;

import it.tdlight.jni.TdApi.ChatEventAction;
import it.tdlight.jni.TdApi.ChatEventMemberJoinedByInviteLink;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;


@Getter
@RequiredArgsConstructor
public class ChatEvent {
    /**
     * User of event
     */
    private final User user;

    /**
     * Event date
     */
    private final Date date;

    /**
     * Link the user joined
     */
    private final String inviteLink;

    public ChatEvent(User user, Date date, ChatEventAction action) {
        this.user = user;
        this.date = date;

        inviteLink = action instanceof ChatEventMemberJoinedByInviteLink joinedByInviteLink
                ? joinedByInviteLink.inviteLink.inviteLink
                : "";
    }
}
