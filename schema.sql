create extension if not exists "uuid-ossp";

CREATE TABLE camel_archive_db(id uuid not null primary key, data json, created_at timestamp default now());
CREATE TABLE camel_read_db(id uuid not null primary key, data json, created_at timestamp default now());
