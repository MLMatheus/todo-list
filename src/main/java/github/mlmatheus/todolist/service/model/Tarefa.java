package github.mlmatheus.todolist.service.model;

import github.mlmatheus.todolist.infrastructure.constants.Geral;
import github.mlmatheus.todolist.infrastructure.exception.ValidacaoDominioException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Agregado de domínio Tarefa. Concentra as regras de negócio (criação, edição e transição
 * de status). POJO puro — sem qualquer dependência de framework de persistência.
 */
@Getter
@Builder
@EqualsAndHashCode(of = "id")
public class Tarefa {

    private final String id;
    private String titulo;
    private String descricao;
    private Status status;
    private Prioridade prioridade;
    private LocalDate dataVencimento;
    private final LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private final String usuarioId;

    /** Cria uma nova tarefa válida com status inicial PENDENTE e prioridade default MÉDIA. */
    public static Tarefa criar(String id, String titulo, String descricao, Integer prioridadeCodigo,
                               LocalDate dataVencimento, String usuarioId, LocalDateTime momento) {
        return Tarefa.builder()
                .id(id)
                .titulo(validarTitulo(titulo))
                .descricao(validarDescricao(descricao))
                .status(Status.PENDENTE)
                .prioridade(Prioridade.fromCodigoOrDefault(prioridadeCodigo))
                .dataVencimento(dataVencimento)
                .dataCriacao(momento)
                .dataAtualizacao(momento)
                .usuarioId(usuarioId)
                .build();
    }

    /** Marca como concluída. Idempotente: se já concluída, não altera nada. */
    public void concluir(LocalDateTime momento) {
        if (status != Status.CONCLUIDA) {
            this.status = Status.CONCLUIDA;
            this.dataAtualizacao = momento;
        }
    }

    /** Reabre uma tarefa concluída. Idempotente: se já pendente, não altera nada. */
    public void reabrir(LocalDateTime momento) {
        if (status != Status.PENDENTE) {
            this.status = Status.PENDENTE;
            this.dataAtualizacao = momento;
        }
    }

    /** Atualiza o conteúdo da tarefa (campos opcionais; {@code null} = manter atual). */
    public void atualizarConteudo(String titulo, String descricao, Integer prioridadeCodigo,
                                  LocalDate dataVencimento, LocalDateTime momento) {
        if (titulo != null) {
            this.titulo = validarTitulo(titulo);
        }
        if (descricao != null) {
            this.descricao = validarDescricao(descricao);
        }
        if (prioridadeCodigo != null) {
            this.prioridade = Prioridade.fromCodigo(prioridadeCodigo);
        }
        if (dataVencimento != null) {
            this.dataVencimento = dataVencimento;
        }
        this.dataAtualizacao = momento;
    }

    private static String validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new ValidacaoDominioException("titulo", "O título é obrigatório");
        }
        String normalizado = titulo.strip();
        if (normalizado.length() > Geral.TITULO_MAX) {
            throw new ValidacaoDominioException("titulo",
                    "O título deve ter no máximo " + Geral.TITULO_MAX + " caracteres");
        }
        return normalizado;
    }

    private static String validarDescricao(String descricao) {
        if (descricao != null && descricao.length() > Geral.DESCRICAO_MAX) {
            throw new ValidacaoDominioException("descricao",
                    "A descrição deve ter no máximo " + Geral.DESCRICAO_MAX + " caracteres");
        }
        return descricao;
    }
}
