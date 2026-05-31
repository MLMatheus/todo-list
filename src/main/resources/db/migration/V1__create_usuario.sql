CREATE TABLE usuario (
    id    CHAR(36)     NOT NULL,
    nome  VARCHAR(255) NOT NULL,
    email VARCHAR(320) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
