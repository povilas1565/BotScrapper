package com.example.scrapper.domain.models;

import com.example.scrapper.domain.models.telegram.User;
import com.example.scrapper.domain.models.telegram.channels.AdministratingChannel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "t_cross_promotions")
public class CrossPromotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /**
     * Start date
     */
    @Column(name = "start_date")
    private Date startDate;

    /**
     * End date
     */
    @Column(name = "end_date")
    private Date endDate;

    /**
     * Link the user joined
     */
    @Column(name = "invite_link")
    private String inviteLink;

    /**
     * Joined users
     */
    @OneToMany(cascade = CascadeType.ALL)
    private List<User> joinedUsers;

    /**
     * Administrating channel
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "administrating_channel_id")
    private AdministratingChannel administratingChannel;

    public CrossPromotion(Date startDate, AdministratingChannel administratingChannel, String inviteLink) {
        this.startDate = startDate;
        this.administratingChannel = administratingChannel;
        this.inviteLink = inviteLink;
    }
}