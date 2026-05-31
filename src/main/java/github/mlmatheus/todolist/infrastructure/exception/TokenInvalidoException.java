package github.mlmatheus.todolist.infrastructure.exception;

/**
 * Lançada quando o token autenticado não traz as informações mínimas de identidade
 * (ex.: claim {@code email} ausente). Mapeada para HTTP 401.
 */
public class TokenInvalidoException extends RuntimeException {

    public TokenInvalidoException(String mensagem) {
        super(mensagem);
    }
}
