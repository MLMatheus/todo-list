package github.mlmatheus.todolist.infrastructure.exception;

import lombok.Getter;

/** Erro de validação de regra de domínio, mapeado para HTTP 400 com o campo afetado. */
@Getter
public class ValidacaoDominioException extends RuntimeException {

    private final String campo;

    public ValidacaoDominioException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }
}
