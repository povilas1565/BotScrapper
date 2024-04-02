package com.example.scrapper.domain.models.telegram.channels;

import lombok.NoArgsConstructor;
import com.example.scrapper.domain.models.telegram.TelegramObject;

import javax.persistence.Entity;


@Entity(name = "t_administrating_channels")
@NoArgsConstructor
public class AdministratingChannel extends TelegramObject {
    public AdministratingChannel(Long telegramId, String name) {
        this.telegramId = telegramId;
        this.name = name;
    }
}