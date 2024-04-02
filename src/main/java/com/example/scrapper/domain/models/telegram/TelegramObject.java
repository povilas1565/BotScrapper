package com.example.scrapper.domain.models.telegram;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@MappedSuperclass
public abstract class TelegramObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /**
     * Telegram id
     */
    @Column(name = "telegram_id")
    protected Long telegramId;

    /**
     * Object name
     */
    @Column(name = "name")
    protected String name;
}