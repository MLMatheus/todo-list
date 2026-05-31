package github.mlmatheus.todolist.service.repository;

import github.mlmatheus.todolist.service.model.Usuario;
import java.util.Optional;

/** Porta de domínio para persistência de usuários (implementada por adaptador JPA). */
public interface UsuarioRepository {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorEmail(String email);
}
