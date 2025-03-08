--liquibase formatted sql
--changeset Felipe:202503021534
--comment: boards_columns table create

CREATE table boards_columns(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    column_order int NOT NULL,
    type VARCHAR(8) NOT NULL,
    board_id BIGINT NOT NULL,
    CONSTRAINT boards__boards_columns_fk FOREIGN KEY (board_id) references boards(id) ON DELETE CASCADE,
    CONSTRAINT id_order_uk UNIQUE KEY unique_board_id_order (board_id, column_order)
) ENGINE=InnoDB

--rollback DROP table boards_columns