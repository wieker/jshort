create table url_records (
    id serial primary key,
    shortUrl varchar(255),
    longUrl varchar(255)
);

create table stat_records (
    id serial primary key,
    url_record_id integer references url_records,
    dateTime timestamp,
    counter numeric
);

create index longIndex on url_records (longUrl);
create index shortIndex on url_records (shortUrl);
create index statIndex on stat_records (url_record_id, dateTime);
