package com.example.scrapper.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.scrapper.domain.models.telegram.channels.AdministratingChannel;

public interface AdministratingChannelsRepository extends JpaRepository<AdministratingChannel, Long> {
    /**
     * Find first administrating channel by telegram id
     *
     * @param id telegram id
     *
     * @return Administrating channel
     */
    AdministratingChannel findFirstByTelegramId(Long id);
}