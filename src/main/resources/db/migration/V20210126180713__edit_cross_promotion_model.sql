ALTER TABLE t_cross_promotions
    ALTER COLUMN id
        SET MAXVALUE 2147483647;

ALTER TABLE t_cross_promotions
    DROP CONSTRAINT fk5cjlio0xoamcqojsxa3qwx5w;

ALTER TABLE t_cross_promotions
    DROP COLUMN outside_channel_id;

DROP TABLE t_outside_channels;