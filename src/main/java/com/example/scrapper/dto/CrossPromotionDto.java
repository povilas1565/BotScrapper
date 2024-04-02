package com.example.scrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class CrossPromotionDto {
    private Long id;

    /**
     * Start date
     */
    private Date startDate;

    /**
     * End date
     */
    private Date endDate;

    /**
     * Link the user joined
     */
    private String inviteLink;

    /**
     * Joined users
     */
    private List<UserDto> joinedUsers;

    /**
     * Administrating channel
     */
    private AdministratingChannelDto administratingChannel;
}