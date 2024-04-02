package com.example.scrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdministratingChannelDto {

    private Long id;

    /**
     * Telegram id
     */
    private Long telegramId;

    /**
     * Channel name
     */
    private String name;
}
