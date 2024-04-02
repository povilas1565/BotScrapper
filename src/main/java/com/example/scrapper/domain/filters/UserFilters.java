package com.example.scrapper.domain.filters;

import com.example.scrapper.domain.models.telegram.ChatEvent;
import com.example.scrapper.domain.models.CrossPromotion;

import java.util.Map;
import java.util.function.BiFunction;

import static com.example.scrapper.domain.filters.UserFilters.FilterType.*;


public class UserFilters {
    public enum FilterType {
        /**
         * Empty filter
         */
        NONE,
        /**
         * Filter by invite link
         */
        INVITE_LINK
    }

    public static final Map<FilterType, BiFunction<CrossPromotion, ChatEvent, Boolean>> userFilters = Map.of(
            NONE, ((crossPromotion, chatEvent) -> true),

            INVITE_LINK, ((crossPromotion, chatEvent) -> {
                var link = chatEvent.getInviteLink();

                // Telegram hiding links that created by other admins
                var usefulPartOfLink = link.replace("...", "");

                return crossPromotion.getInviteLink().contains(usefulPartOfLink);
            })
    );

    public static FilterType getFilterType(CrossPromotion crossPromotion) {

        return crossPromotion.getInviteLink().isBlank()
                ? NONE
                : INVITE_LINK;
    }
}
