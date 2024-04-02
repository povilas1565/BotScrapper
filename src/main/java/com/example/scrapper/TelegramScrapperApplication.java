package com.example.scrapper;

import it.tdlight.common.Init;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class TelegramScrapperApplication {

    public static void main(String[] args) {
        loadTelegramNativeLibrary();
        SpringApplication.run(TelegramScrapperApplication.class, args);
    }

    @SneakyThrows
    private static void loadTelegramNativeLibrary() {
        Init.start();
    }
}