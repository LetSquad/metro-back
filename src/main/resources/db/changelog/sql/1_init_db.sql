create table passenger_category
(
    code text primary key,
    name text
);

create table passenger
(
    id            bigint primary key,
    first_name    text,
    last_name     text,
    middle_name   text,
    sex_name      text,
    comment       text,
    has_pacemaker boolean,
    category_code text,
    created_at    timestamp,
    deleted_at    timestamp,
    FOREIGN KEY (category_code) REFERENCES passenger_category (code)
);

CREATE SEQUENCE passenger_id_seq START 1;

create table passenger_phone
(
    id           bigint primary key,
    phone_number text,
    description  text,
    passenger_id bigint,
    FOREIGN KEY (passenger_id) REFERENCES passenger (id)
);

CREATE SEQUENCE passenger_phone_id_seq START 1;

create table order_status
(
    code text primary key,
    name text
);

create table metro_line
(
    id   bigint primary key,
    name text
);

CREATE SEQUENCE metro_line_id_seq START 1;

create table metro_station
(
    id      bigint primary key,
    name    text,
    line_id bigint,
    FOREIGN KEY (line_id) REFERENCES metro_line (id)
);

CREATE SEQUENCE metro_station_id_seq START 1;

create table passenger_order
(
    id                    bigint primary key,
    start_description     text,
    finish_description    text,
    order_application     text,

    duration              interval,
    transfers             jsonb,
    passenger_count       int,

    male_employee_count   integer,
    female_employee_count integer,
    baggage               jsonb,

    additional_info       text,
    order_time            timestamp,
    start_time            timestamp,

    finish_time           timestamp,
    absence_time          timestamp,
    cancel_time           timestamp,

    order_status_code     text,
    passenger_id          bigint,
    passenger_category    text,

    start_station_id      bigint,
    finish_station_id     bigint,
    created_at            timestamp,

    updated_at            timestamp,
    deleted_at            timestamp,
    FOREIGN KEY (order_status_code) REFERENCES order_status (code),
    FOREIGN KEY (passenger_id) REFERENCES passenger (id),
    FOREIGN KEY (passenger_category) REFERENCES passenger_category (code),
    FOREIGN KEY (start_station_id) REFERENCES metro_station (id),
    FOREIGN KEY (finish_station_id) REFERENCES metro_station (id)
);

CREATE SEQUENCE passenger_order_id_seq START 1;

create table order_change
(
    id                bigint primary key,
    order_change_code text,
    order_change_log  jsonb,
    employee_login    text,
    time_edit         timestamp,
    order_id          bigint,
    FOREIGN KEY (order_id) REFERENCES passenger_order (id)
);

CREATE SEQUENCE order_change_id_seq START 1;

create table metro_station_transfer
(
    start_station_id  bigint,
    finish_station_id bigint,
    duration          interval,
    is_crosswalking   boolean,
    FOREIGN KEY (start_station_id) REFERENCES metro_station (id),
    FOREIGN KEY (finish_station_id) REFERENCES metro_station (id),
    PRIMARY KEY (start_station_id, finish_station_id)
);

create table employee_rank
(
    code       text primary key,
    name       text,
    short_name text,
    role       text
);

create table metro_user
(
    id                    bigint primary key,
    login                 text,
    password              text,
    is_password_temporary boolean
);

CREATE SEQUENCE metro_user_id_seq START 1;

create table employee
(
    id              bigint primary key,
    first_name      text,
    last_name       text,
    middle_name     text,
    sex             text,
    work_start      time,
    work_finish     time,
    shift_time      text,
    work_phone      text,
    personal_phone  text,
    employee_number bigint,
    light_duties    boolean,
    rank_code       text,
    user_id         bigint,
    FOREIGN KEY (rank_code) REFERENCES employee_rank (code),
    FOREIGN KEY (user_id) REFERENCES metro_user (id)
);

CREATE SEQUENCE employee_id_seq START 1;

create table employee_shift
(
    id          bigint primary key,
    shift_date  timestamp,
    work_start  time,
    work_finish time,
    employee_id bigint,
    FOREIGN KEY (employee_id) REFERENCES employee (id)
);

CREATE SEQUENCE employee_shift_id_seq START 1;

create table employee_shift_order
(
    employee_shift_id bigint,
    order_id          bigint,
    is_attached       boolean,
    FOREIGN KEY (employee_shift_id) REFERENCES employee_shift (id),
    FOREIGN KEY (order_id) REFERENCES passenger_order (id),
    PRIMARY KEY (employee_shift_id, order_id)
);

