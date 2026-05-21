create table iam_client (
    id varchar(19) primary key,
    tenant_id varchar(19) null,
    client_id varchar(64) not null,
    enabled boolean not null,
    created_at timestamp not null,
    created_by varchar(19) null,
    updated_at timestamp not null,
    updated_by varchar(19) null,
    deleted smallint not null default 0,
    version integer not null default 0
);

create unique index uk_iam_client_client_id_deleted on iam_client(client_id, deleted);

create table iam_user (
    id varchar(19) primary key,
    tenant_id varchar(19) null,
    username varchar(64) not null,
    display_name varchar(64) not null,
    password_hash varchar(255) not null,
    status varchar(32) not null,
    created_at timestamp not null,
    created_by varchar(19) null,
    updated_at timestamp not null,
    updated_by varchar(19) null,
    deleted smallint not null default 0,
    version integer not null default 0
);

create unique index uk_iam_user_username_deleted on iam_user(username, deleted);

create table iam_role (
    id varchar(19) primary key,
    tenant_id varchar(19) null,
    code varchar(64) not null,
    name varchar(64) not null,
    enabled boolean not null,
    created_at timestamp not null,
    created_by varchar(19) null,
    updated_at timestamp not null,
    updated_by varchar(19) null,
    deleted smallint not null default 0,
    version integer not null default 0
);

create unique index uk_iam_role_code_deleted on iam_role(code, deleted);

create table iam_permission (
    id varchar(19) primary key,
    tenant_id varchar(19) null,
    code varchar(128) not null,
    name varchar(128) not null,
    enabled boolean not null,
    created_at timestamp not null,
    created_by varchar(19) null,
    updated_at timestamp not null,
    updated_by varchar(19) null,
    deleted smallint not null default 0,
    version integer not null default 0
);

create unique index uk_iam_permission_code_deleted on iam_permission(code, deleted);

create table iam_user_role (
    id varchar(19) primary key,
    tenant_id varchar(19) null,
    user_id varchar(19) not null,
    role_id varchar(19) not null,
    created_at timestamp not null,
    created_by varchar(19) null,
    updated_at timestamp not null,
    updated_by varchar(19) null,
    deleted smallint not null default 0,
    version integer not null default 0
);

create unique index uk_iam_user_role_user_role_deleted on iam_user_role(user_id, role_id, deleted);

create table iam_role_permission (
    id varchar(19) primary key,
    tenant_id varchar(19) null,
    role_id varchar(19) not null,
    permission_id varchar(19) not null,
    created_at timestamp not null,
    created_by varchar(19) null,
    updated_at timestamp not null,
    updated_by varchar(19) null,
    deleted smallint not null default 0,
    version integer not null default 0
);

create unique index uk_iam_role_permission_role_permission_deleted on iam_role_permission(role_id, permission_id, deleted);
