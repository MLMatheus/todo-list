package github.mlmatheus.todolist.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** Entidade de domínio: proprietário das tarefas, provisionado a partir do token Google. */
@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario {

    private final String id;
    private final String nome;
    private final String email;
}
