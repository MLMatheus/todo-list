CREATE TABLE tarefa (
    id               CHAR(36)      NOT NULL,
    titulo           VARCHAR(150)  NOT NULL,
    descricao        VARCHAR(2000),
    status           VARCHAR(20)   NOT NULL,
    prioridade       INT           NOT NULL,
    data_vencimento  DATE,
    data_criacao     DATETIME(3)   NOT NULL,
    data_atualizacao DATETIME(3)   NOT NULL,
    usuario_id       CHAR(36)      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_tarefa_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    INDEX idx_tarefa_usuario (usuario_id),
    INDEX idx_tarefa_usuario_status (usuario_id, status),
    INDEX idx_tarefa_usuario_venc (usuario_id, data_vencimento)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
