ALTER TABLE t_cross_promotions_entered_users
    RENAME COLUMN entered_users_id TO joined_users_id;

ALTER TABLE t_cross_promotions_entered_users
    RENAME TO t_cross_promotions_joined_users;

ALTER TABLE t_administrated_channels
    ALTER COLUMN id
        SET MAXVALUE 2147483647;

ALTER TABLE t_administrated_channels
    RENAME TO t_administrating_channels;

ALTER TABLE IF EXISTS t_cross_promotions_joined_users
    DROP CONSTRAINT IF EXISTS uk_mv47rgddll020hcu5fqsji69o;

ALTER TABLE IF EXISTS t_cross_promotions_joined_users
    ADD CONSTRAINT uk_mv47rgddll020hcu5fqsji69o UNIQUE (joined_users_id);