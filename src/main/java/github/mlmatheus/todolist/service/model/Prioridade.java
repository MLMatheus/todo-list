package github.mlmatheus.todolist.service.model;

/**
 * Prioridade da tarefa. Convenção P1/P2/P3: menor código = maior prioridade.
 * {@code 1=ALTA, 2=MÉDIA, 3=BAIXA}. Quando omitida no cadastro, assume {@link #MEDIA}.
 */
public enum Prioridade {
    ALTA(1),
    MEDIA(2),
    BAIXA(3);

    private final int codigo;

    Prioridade(int codigo) {
        this.codigo = codigo;
    }

    public int codigo() {
        return codigo;
    }

    /** Converte o código inteiro (1..3) na prioridade correspondente. */
    public static Prioridade fromCodigo(int codigo) {
        for (Prioridade prioridade : values()) {
            if (prioridade.codigo == codigo) {
                return prioridade;
            }
        }
        throw new IllegalArgumentException("Prioridade inválida: " + codigo);
    }

    /** Igual a {@link #fromCodigo(int)}, mas retorna {@link #MEDIA} quando o código é {@code null}. */
    public static Prioridade fromCodigoOrDefault(Integer codigo) {
        return codigo == null ? MEDIA : fromCodigo(codigo);
    }
}
