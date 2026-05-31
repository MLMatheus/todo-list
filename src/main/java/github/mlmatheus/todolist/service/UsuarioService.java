package github.mlmatheus.todolist.service;

import github.mlmatheus.todolist.service.model.Usuario;

/** Resolve (provisiona, se necessário) o usuário local a partir das claims do token. */
public interface UsuarioService {

    Usuario resolver(String sub, String email, String nome);
}
