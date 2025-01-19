create extension if not exists "uuid-ossp";

CREATE TABLE camel_write_db(id uuid not null primary key, data json, time_ms bigint, created_at timestamp default now());
CREATE TABLE camel_read_db(id uuid not null primary key, data json, time_ms bigint, created_at timestamp default now());

CREATE TABLE wso2mi_write_db(id uuid not null primary key default uuid_generate_v4(), data json, time_ms bigint, created_at timestamp default now());
CREATE TABLE wso2mi_read_db(id uuid not null primary key default uuid_generate_v4(), data json, time_ms bigint, created_at timestamp default now());
