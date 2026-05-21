create table synapse_data_probe (
    id varchar(19) primary key,
    tenant_id varchar(19),
    created_at timestamp,
    created_by varchar(19),
    updated_at timestamp,
    updated_by varchar(19),
    deleted smallint default 0,
    version integer default 0
);
