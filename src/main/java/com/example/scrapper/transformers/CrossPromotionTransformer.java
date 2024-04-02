package com.example.scrapper.transformers;

import org.mapstruct.Mapper;
import com.example.scrapper.domain.models.CrossPromotion;
import com.example.scrapper.dto.CrossPromotionDto;

@Mapper(componentModel = "spring", uses = {AdministratingChannelTransformer.class, UserTransformer.class})
public interface CrossPromotionTransformer extends Transformer<CrossPromotion, CrossPromotionDto> {
}
