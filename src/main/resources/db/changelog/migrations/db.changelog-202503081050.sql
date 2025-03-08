--liquibase formatted sql
--changeset Felipe:202503081050
--comment: card_movements table create

CREATE TABLE card_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,
    from_column_id BIGINT NULL,
    to_column_id BIGINT NOT NULL,
    moved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT card_movements__card_fk FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    CONSTRAINT card_movements__from_column_fk FOREIGN KEY (from_column_id) REFERENCES boards_columns(id) ON DELETE SET NULL,
    CONSTRAINT card_movements__to_column_fk FOREIGN KEY (to_column_id) REFERENCES boards_columns(id) ON DELETE CASCADE
) ENGINE=InnoDB;

--rollback DROP TABLE card_movements