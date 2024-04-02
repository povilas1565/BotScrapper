package com.example.scrapper.services;

import com.example.scrapper.repositories.AdministratingChannelsRepository;
import com.example.scrapper.repositories.CrossPromotionRepository;
import com.example.scrapper.transformers.CrossPromotionTransformer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import com.example.scrapper.domain.models.CrossPromotion;
import com.example.scrapper.domain.models.telegram.ChatEvent;
import com.example.scrapper.domain.models.telegram.channels.AdministratingChannel;
import com.example.scrapper.dto.CrossPromotionDto;
import com.example.scrapper.validators.CrossPromotionValidator;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.scrapper.domain.filters.UserFilters.*;

@Service
@RequiredArgsConstructor
public class CrossPromotionService implements ValidationService {
    private final CrossPromotionRepository crossPromotionRepository;

    private final AdministratingChannelsRepository administratingChannelsRepository;

    private final CrossPromotionTransformer transformer;

    /**
     * Start cross promotion
     *
     * @param administratingChannelId Channel id
     * @param newChannelName          Channel name if it doesn't exists
     * @param inviteLink              Link the user joined
     *
     * @return Created cross promotion
     */
    public CrossPromotion start(Long administratingChannelId, String inviteLink, String newChannelName) {
        var administratingChannel = administratingChannelsRepository
                .findFirstByTelegramId(administratingChannelId);

        if (administratingChannel == null)
            administratingChannel = new AdministratingChannel(administratingChannelId, newChannelName);

        var newCrossPromo = new CrossPromotion(new Date(), administratingChannel, inviteLink);

        validateAndThrowIfInvalid(new CrossPromotionValidator(), newCrossPromo);

        crossPromotionRepository.save(newCrossPromo);

        return newCrossPromo;
    }

    /**
     * Get telegram id by cross promotion id
     *
     * @param crossPromotionId Cross promotion id
     *
     * @return telegram id
     */
    public Long getAdministratingChannelTelegramId(Long crossPromotionId) {
        var crossPromotion = crossPromotionRepository
                .findById(crossPromotionId)
                .orElseThrow(() -> new IllegalArgumentException(createExceptionMessage(crossPromotionId)));

        return crossPromotion.getAdministratingChannel().getTelegramId();
    }

    /**
     * End cross promotion
     *
     * @param crossPromotionId Cross promotion id
     * @param allJoinedUsers   Joined users
     *
     * @return Current cross promotion id
     */
    @SneakyThrows
    public Long end(Long crossPromotionId, List<ChatEvent> allJoinedUsers) {
        var crossPromotion = crossPromotionRepository
                .findById(crossPromotionId)
                .orElseThrow(() -> new IllegalArgumentException(createExceptionMessage(crossPromotionId)));

        if (crossPromotion.getEndDate() != null)
            throw new IllegalAccessException("This cross promotion is ended!");

        var crossPromotionStartDate = crossPromotion.getStartDate();

        var filterType = getFilterType(crossPromotion);
        var joinedAfterStart = allJoinedUsers
                .stream()
                .filter(ce -> crossPromotionStartDate.before(ce.getDate()))
                .filter(ce -> userFilters.get(filterType).apply(crossPromotion, ce))
                .map(ChatEvent::getUser)
                .collect(Collectors.toList());

        joinedAfterStart.forEach(u -> u.setCrossPromotion(crossPromotion));

        crossPromotion.setJoinedUsers(joinedAfterStart);
        crossPromotion.setEndDate(new Date());

        crossPromotionRepository.save(crossPromotion);

        return crossPromotion.getId();
    }

    /**
     * Update cross promotion model
     *
     * @param crossPromotionDto Model from request
     *
     * @return Updated model
     */
    public CrossPromotionDto update(CrossPromotionDto crossPromotionDto) {
        var mappedDto = transformer.dtoToEntity(crossPromotionDto);

        var updatedEntity = crossPromotionRepository.save(mappedDto);

        return transformer.entityToDto(updatedEntity);
    }

    /**
     * Get cross promotion report
     *
     * @param crossPromotionId Cross promotion id
     *
     * @return Cross promotion report
     */
    public String getReport(Long crossPromotionId) {
        var crossPromotion = crossPromotionRepository
                .findById(crossPromotionId)
                .orElseThrow(() -> new IllegalArgumentException(createExceptionMessage(crossPromotionId)));

        if (crossPromotion.getEndDate() == null)
            throw new IllegalArgumentException("Cross promotion with id: " + crossPromotionId + " isn't over yet.");

        var builder = new StringBuilder();

        crossPromotion.getJoinedUsers().forEach(u -> builder.append(u).append('\n'));

        return builder.toString();
    }

    private String createExceptionMessage(Long crossPromotionId) {
        return "Cross promotion with id: " + crossPromotionId + " does not exist.";
    }

    /**
     * Get ongoing cross promotions
     *
     * @return Ongoing cross promotions
     */
    public List<CrossPromotion> getOngoing() {
        return crossPromotionRepository.findAllByEndDateIsNullOrderByStartDate();
    }
}
