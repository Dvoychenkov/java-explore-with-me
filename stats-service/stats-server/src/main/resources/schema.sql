create table if not exists hits (
    id bigserial primary key,
    app varchar(255) not null,
    uri varchar(1024) not null,
    ip  varchar(45) not null,
    timestamp timestamp not null
);
