package github.mlmatheus.todolist;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/** FR-016: operações sobre tarefas exigem usuário autenticado. */
class AutenticacaoIT extends AbstractIntegrationTest {

    @Test
    void requisicaoSemTokenRetorna401() throws Exception {
        mvc.perform(get("/tarefas"))
                .andExpect(status().isUnauthorized());
    }
}
