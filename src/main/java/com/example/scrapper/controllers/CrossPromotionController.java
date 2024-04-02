package com.example.scrapper.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.example.scrapper.domain.models.CrossPromotion;
import com.example.scrapper.dto.CrossPromotionDto;
import com.example.scrapper.services.CrossPromotionService;
import com.example.scrapper.services.TelegramApiExecutorService;

import java.util.List;

@Tag(name = "cross-promotion-controller", description = "API for cross promotion actions")
@RestController
@RequestMapping("/crossPromotion")
@RequiredArgsConstructor
public class CrossPromotionController {
    private final CrossPromotionService crossPromotionService;

    private final TelegramApiExecutorService telegramApiExecutorService;

    @Operation(summary = "Start cross promotion", description = "Starts cross promotion and returns it dto")
    @Parameters(value = {
            @Parameter(name = "administratingChannelId", description = "Channel id"),
            @Parameter(name = "inviteLink", description = "Cross promotion invite link"),
            @Parameter(name = "newChannelName", description = "Channel name for db record"),
    })
    @PostMapping("/start")
    public CrossPromotion start(@RequestParam Long administratingChannelId,
                                @RequestParam(required = false) String inviteLink,
                                @RequestParam(required = false) String newChannelName) {
        return crossPromotionService.start(administratingChannelId, inviteLink, newChannelName);
    }

    @Operation(summary = "End cross promotion", description = "Ends cross promotion and returns it id")
    @Parameters(value = {
            @Parameter(name = "crossPromotionId", description = "Channel id"),
    })
    @PatchMapping("/end")
    public Long end(@RequestParam Long crossPromotionId) {
        var tgId = crossPromotionService.getAdministratingChannelTelegramId(crossPromotionId);

        var allJoinedUsers = telegramApiExecutorService.getJoinedDomainEventsByChannelId(tgId);

        return crossPromotionService.end(crossPromotionId, allJoinedUsers);
    }

    @Operation(summary = "Update cross promotion model", description = "Updates cross promotion model and returns it updated model")
    @Parameters(value = {
            @Parameter(name = "crossPromotionDto", description = "Updated cross promotion model"),
    })
    @PatchMapping("/update")
    public CrossPromotionDto update(@RequestBody CrossPromotionDto crossPromotionDto) {
        return crossPromotionService.update(crossPromotionDto);
    }

    @Operation(summary = "Get cross promotion report", description = "Returns cross promotion report with usernames and tg ids")
    @Parameters(value = {
            @Parameter(name = "crossPromotionId", description = "Cross promotion id"),
    })
    @GetMapping("/getReport")
    public String getReport(@RequestParam Long crossPromotionId) {
        return crossPromotionService.getReport(crossPromotionId);
    }

    @Operation(summary = "Get ongoing cross promotions", description = "Returns ongoing cross promotions")
    @GetMapping("/getOngoing")
    public List<CrossPromotion> getOngoing() {
        return crossPromotionService.getOngoing();
    }
}
