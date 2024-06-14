CREATE INDEX IF NOT EXISTS employee_user_id_idx ON employee (user_id);

CREATE INDEX IF NOT EXISTS employee_shift_date_idx ON employee_shift (shift_date);

CREATE INDEX IF NOT EXISTS employee_shift_order_shift_id_idx ON employee_shift_order (employee_shift_id);

CREATE INDEX IF NOT EXISTS employee_shift_order_id_idx ON employee_shift_order (order_id);

CREATE INDEX IF NOT EXISTS passenger_order_time_idx ON passenger_order (order_time);

CREATE INDEX IF NOT EXISTS passenger_phone_passenger_id_idx ON passenger_phone (passenger_id);
