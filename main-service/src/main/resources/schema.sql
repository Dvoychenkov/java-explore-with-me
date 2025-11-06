create table if not exists users (
    id            bigserial primary key,
    name          varchar(255) not null,
    email         varchar(255) not null unique
);

create table if not exists categories (
    id            bigserial primary key,
    name          varchar(255) not null unique
);

create table if not exists events (
    id                 bigserial primary key,
    title              varchar(120)  not null,
    annotation         varchar(2000) not null,
    description        varchar(7000),
    event_date         timestamp     not null,
    created_on         timestamp     not null,
    published_on       timestamp,
    state              varchar(32)   not null,
    paid               boolean       not null default false,
    participant_limit  integer       not null default 0,
    request_moderation boolean       not null default true,
    location_lat       double precision not null,
    location_lon       double precision not null,
    initiator_id       bigint        not null references users(id),
    category_id        bigint        not null references categories(id)
);

create table if not exists compilations (
    id            bigserial primary key,
    title         varchar(255) not null,
    pinned        boolean not null default false
);

create table if not exists compilation_events (
    compilation_id bigint not null references compilations(id) on delete cascade,
    event_id       bigint not null references events(id) on delete cascade,
    primary key (compilation_id, event_id)
);

create table if not exists participation_requests (
    id           bigserial primary key,
    requester_id bigint not null references users(id),
    event_id     bigint not null references events(id),
    status       varchar(32) not null,
    created      timestamp not null,
    unique (requester_id, event_id)
);
