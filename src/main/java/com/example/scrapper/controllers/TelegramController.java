package com.example.scrapper.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.tdlight.jni.TdApi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.web.bind.annotation.*;
import com.example.scrapper.services.TelegramApiExecutorService;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "telegram-controller", description = "API for executing telegram API")
@RestController
@RequestMapping("/telegram")
@RequiredArgsConstructor
public class TelegramController {
    private final TelegramApiExecutorService telegramApiExecutor;

    @Operation(summary = "Login into telegram account", description = "Login by steps")
    @Parameters(value = {
            @Parameter(name = "query", description = "Requested query"),
    })
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String query) {

        return telegramApiExecutor.authorize(query);
    }

    @Operation(summary = "Logout from telegram account", description = "Closes your session")
    @DeleteMapping("/logout")
    public String logout() {

        return telegramApiExecutor.logout();
    }

    @Operation(summary = "Get telegram channels", description = "Returns telegram channels")
    @Parameters(value = {
            @Parameter(name = "titleSubstring", description = "Telegram channel name substring"),
    })
    @GetMapping("/getChannels")
    public List<ImmutablePair<Long, String>> getChannels(@RequestParam String titleSubstring) {

        return telegramApiExecutor.getChannels(titleSubstring);
    }

    @Operation(summary = "Get joined users channel events", description = "Returns users that joined to channel")
    @Parameters(value = {
            @Parameter(name = "channelId", description = "Telegram channel id"),
    })
    @GetMapping("/getJoinedLogUsers")
    public List<TdApi.ChatEvent> getJoinedLogUsers(@RequestParam Long channelId) {

        return telegramApiExecutor.getJoinedLogUsers(channelId, new ArrayList<>(), 0);
    }
}
