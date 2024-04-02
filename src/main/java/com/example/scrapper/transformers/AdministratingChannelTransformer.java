package com.example.scrapper.transformers;

import org.mapstruct.Mapper;
import com.example.scrapper.domain.models.telegram.channels.AdministratingChannel;
import com.example.scrapper.dto.AdministratingChannelDto;


@Mapper(componentModel = "spring")
public interface AdministratingChannelTransformer extends Transformer<AdministratingChannel, AdministratingChannelDto> {
}
