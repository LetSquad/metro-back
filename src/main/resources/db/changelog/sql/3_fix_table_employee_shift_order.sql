drop table employee_shift_order;

create table employee_shift_order(
    id                bigint primary key,
    employee_shift_id bigint not null,
    order_id          bigint,
    is_attached       boolean not null,
    action_type       text not null,
    time_start        timestamp not null,
    time_finish       timestamp not null,
    created_at        timestamp not null default current_timestamp,
    FOREIGN KEY (employee_shift_id) REFERENCES employee_shift (id),
    FOREIGN KEY (order_id) REFERENCES passenger_order (id)
);

CREATE SEQUENCE employee_shift_order_id_seq START 1;