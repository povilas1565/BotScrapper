package com.example.scrapper.transformers;

import org.mapstruct.Mapper;
import com.example.scrapper.domain.models.telegram.User;
import com.example.scrapper.dto.UserDto;

@Mapper(componentModel = "spring", uses = CrossPromotionTransformer.class)
public interface UserTransformer extends Transformer<User, UserDto> {
}
