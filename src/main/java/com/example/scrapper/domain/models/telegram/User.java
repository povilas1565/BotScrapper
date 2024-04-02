package com.example.scrapper.domain.models.telegram;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.scrapper.domain.models.CrossPromotion;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Setter
@Getter
@NoArgsConstructor
@Entity(name = "t_users")
public class User extends TelegramObject {
    /**
     * User nickname without @
     */
    @Column(name = "nickname")
    private String nickname;

    /**
     * Cross promotion reference
     */
    @ManyToOne
    public CrossPromotion crossPromotion;

    public User(String nickname, String firstName, int telegramId) {
        this.nickname = nickname;
        name = firstName;
        this.telegramId = (long) telegramId;
    }

    @Override
    public String toString() {
        var result = telegramId.toString();

        if (getNickname() != null && !getNickname().isEmpty())
            result += " - @" + getNickname();

        return result;
    }
}