--liquibase formatted sql
--changeset Felipe:202503021447
--comment: boards table create

CREATE table boards(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
) ENGINE=InnoDB

--rollback DROP table boards