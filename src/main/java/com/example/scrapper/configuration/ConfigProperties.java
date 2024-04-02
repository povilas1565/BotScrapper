package com.example.scrapper.configuration;

import it.tdlight.common.TelegramClient;
import it.tdlight.tdlight.ClientManager;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ConfigProperties {
    @Bean
    @ConfigurationProperties(prefix = "telegram")
    public TelegramProperties telegramProperties() {
        return new TelegramProperties();
    }


    @Bean
    public TelegramClient telegramClient() {
        return ClientManager.create();
    }
}