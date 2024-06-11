create table passenger_category
(
    code       text primary key,
    name       text not null,
    short_name text not null,
    comment    text,
    created_at timestamp not null default current_timestamp
);

CREATE SEQUENCE passenger_id_seq START 1;

create table passenger
(
    id            bigint primary key,
    first_name    text not null,
    last_name     text not null,
    middle_name   text,
    sex           text not null,
    comment       text,
    has_pacemaker boolean not null default false,
    category_code text not null,
    created_at    timestamp not null default current_timestamp,
    updated_at    timestamp,
    deleted_at    timestamp,
    FOREIGN KEY (category_code) REFERENCES passenger_category (code)
);

create table passenger_phone
(
    id           bigint primary key,
    phone_number text not null,
    description  text,
    passenger_id bigint not null,
    created_at   timestamp not null default current_timestamp,
    FOREIGN KEY (passenger_id) REFERENCES passenger (id)
);

CREATE SEQUENCE passenger_phone_id_seq START 1;

create table order_status
(
    code       text primary key,
    name       text not null,
    created_at timestamp not null default current_timestamp
);

create table metro_line
(
    id         bigint primary key,
    name       text not null,
    color      text not null,
    created_at timestamp not null default current_timestamp
);

CREATE SEQUENCE metro_line_id_seq START 1;

create table metro_station
(
    id         bigint primary key,
    name       text not null,
    line_id    bigint not null,
    created_at timestamp not null default current_timestamp,
    FOREIGN KEY (line_id) REFERENCES metro_line (id)
);

CREATE SEQUENCE metro_station_id_seq START 1;

create table passenger_order
(
    id                    bigint primary key,
    start_description     text,
    finish_description    text,
    order_application     text,

    duration              interval not null,
    transfers             jsonb not null,
    passenger_count       int not null,

    male_employee_count   integer not null,
    female_employee_count integer not null,
    baggage               jsonb,

    additional_info       text,
    order_time            timestamp not null,
    start_time            timestamp,

    finish_time           timestamp,
    absence_time          timestamp,
    cancel_time           timestamp,

    order_status_code     text not null,
    passenger_id          bigint not null,
    passenger_category    text,

    start_station_id      bigint not null,
    finish_station_id     bigint not null,

    created_at            timestamp not null default current_timestamp,
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
    order_change_code text not null,
    order_change_log  jsonb not null,
    employee_login    text not null,
    order_id          bigint not null,
    created_at        timestamp not null default current_timestamp,
    FOREIGN KEY (order_id) REFERENCES passenger_order (id)
);

CREATE SEQUENCE order_change_id_seq START 1;

create table metro_station_transfer
(
    start_station_id  bigint,
    finish_station_id bigint,
    duration          interval not null,
    is_crosswalking   boolean not null,
    created_at        timestamp not null default current_timestamp,
    FOREIGN KEY (start_station_id) REFERENCES metro_station (id),
    FOREIGN KEY (finish_station_id) REFERENCES metro_station (id),
    PRIMARY KEY (start_station_id, finish_station_id)
);

create table employee_rank
(
    code       text primary key,
    name       text not null,
    short_name text,
    role       text not null,
    created_at timestamp not null default current_timestamp
);

create table metro_user
(
    id                    bigint primary key,
    login                 text unique not null,
    password              text not null,
    is_password_temporary boolean not null,
    created_at            timestamp not null default current_timestamp
);

CREATE SEQUENCE metro_user_id_seq START 1;

CREATE TABLE user_refresh_token
(
    user_login    TEXT PRIMARY KEY REFERENCES metro_user (login),
    refresh_token TEXT
);

create table employee
(
    id              bigint primary key,
    first_name      text not null,
    last_name       text not null,
    middle_name     text,
    sex             text not null,
    work_start      time not null,
    work_finish     time not null,
    shift_type      text not null,
    work_phone      text,
    personal_phone  text,
    employee_number bigint unique not null,
    light_duties    boolean not null,
    rank_code       text not null,
    user_id         bigint not null,
    created_at      timestamp not null default current_timestamp,
    updated_at      timestamp,
    deleted_at      timestamp,
    FOREIGN KEY (rank_code) REFERENCES employee_rank (code),
    FOREIGN KEY (user_id) REFERENCES metro_user (id)
);

CREATE SEQUENCE employee_id_seq START 1;

create table employee_shift
(
    id          bigint primary key,
    shift_date  timestamp not null,
    work_start  time not null,
    work_finish time not null,
    employee_id bigint not null,
    created_at  timestamp not null default current_timestamp,
    FOREIGN KEY (employee_id) REFERENCES employee (id)
);

CREATE SEQUENCE employee_shift_id_seq START 1;

create table employee_shift_order
(
    employee_shift_id bigint,
    order_id          bigint,
    is_attached       boolean not null,
    created_at        timestamp not null default current_timestamp,
    FOREIGN KEY (employee_shift_id) REFERENCES employee_shift (id),
    FOREIGN KEY (order_id) REFERENCES passenger_order (id),
    PRIMARY KEY (employee_shift_id, order_id)
);
