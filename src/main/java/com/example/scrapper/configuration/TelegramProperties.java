package com.example.scrapper.configuration;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TelegramProperties {
    private boolean useMessageDatabase;
    private boolean useSecretChats;
    private int apiId;
    private String apiHash;
    private String systemLanguageCode;
    private String deviceModel;
    private String applicationVersion;
    private boolean enableStorageOptimizer;
}