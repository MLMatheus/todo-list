package github.mlmatheus.todolist.service.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entidade JPA de persistência da tarefa (tabela {@code tarefa}). */
@Entity
@Table(name = "tarefa")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarefaEntity {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 2000)
    private String descricao;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private int prioridade;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @Column(name = "usuario_id", nullable = false, length = 36)
    private String usuarioId;
}
