ALTER TABLE if EXISTS t_administrated_channels ADD COLUMN telegram_id int8;
ALTER TABLE if EXISTS t_outside_channels ADD COLUMN telegram_id int8;
ALTER TABLE if EXISTS t_users ADD COLUMN telegram_id int8;