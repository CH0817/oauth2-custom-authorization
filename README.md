# Spring OAuth2 自訂驗證練習

Client 資訊存在 DB，Access token 存在 Redis

## 主要依賴

- Spring Boot 2.6.8
- Spring Security OAuth2 Auto Configure 2.4.5
- MySQL 8
- Redis

## Servers

| name                 | id            | port |
|----------------------|---------------|------|
| Authorization Server |               | 8080 |
| Resource Server 1    | my-resource-1 | 8081 |
| Resource Server 2    | my-resource-2 | 8082 |
| Client 1             | my-client-1   | 8083 |
| Client 2             | my-client-2   | 8084 |

## Tables

create OAuth2 client SQL for MySQL

```sql
create table oauth_client_details
(
    client_id               VARCHAR(256) PRIMARY KEY,
    resource_ids            VARCHAR(256),
    client_secret           VARCHAR(256),
    scope                   VARCHAR(256),
    authorized_grant_types  VARCHAR(256),
    web_server_redirect_uri VARCHAR(256),
    authorities             VARCHAR(256),
    access_token_validity   INTEGER,
    refresh_token_validity  INTEGER,
    additional_information  VARCHAR(4096),
    autoapprove             VARCHAR(256)
);
```

insert Client SQL

```sql
-- my-client-1
delete from oauth_client_details where client_id = 'my-client-1';

insert into oauth_client_details (client_id, resource_ids, client_secret, scope, authorized_grant_types,
                                  web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity)
values ('my-client-1', 'my-resource-1,my-resource-2',
        '$argon2id$v=19$m=4096,t=3,p=1$nCbULAX68aldvbkUl5X02w$QxW73hL6RpkCoykRAmWcBFTfyw/gKYKRqd62Iau2QuQ',
        'test-client-1', 'authorization_code,password,implicit,client_credentials,refresh_token,custom', 'https://www.google.com', 'ROLE_ADMIN,ROLE_USER',
        6000, 6000);
-- my-client-2
delete from oauth_client_details where client_id = 'my-client-2';

insert into oauth_client_details (client_id, resource_ids, client_secret, scope, authorized_grant_types,
                                  web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity)
values ('my-client-2', 'my-resource-2',
        '$argon2id$v=19$m=4096,t=3,p=1$0Jqvq9MYELKyscH9vJtMXg$18+XHvUFRCxfehvypHGFmvjay5v5dvtzcpL0WfPlXi0',
        'test-client-2', 'password,refresh_token', 'https://www.google.com', 'ROLE_ADMIN,ROLE_USER',
        6000, 6000);
```

create User information SQL

```sql
create table user_info
(
    id       VARCHAR(36) primary key not null,
    username VARCHAR(50)             not null,
    password VARCHAR(96)             not null,
    mobile   VARCHAR(10)             not null
)
```

insert user information SQL

```sql
delete from user_info where id = 'test_user';

insert into user_info (id, username, password, mobile)
values ('test_user', 'rex',
        '$argon2id$v=19$m=4096,t=3,p=1$YE9siItcoEaIb9LdHWnP5g$MbdFYyV1sd+5ZhlhDWGWBOCuGFhUN31XrcQQp8r622s', '099999999')
```