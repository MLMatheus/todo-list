package github.mlmatheus.todolist;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** US1: cadastrar e visualizar tarefas. */
class CriarListarTarefaIT extends AbstractIntegrationTest {

    private RequestPostProcessor ana() {
        return jwt().jwt(j -> j.subject("ana").claim("email", "ana-cl@x.com").claim("name", "Ana"));
    }

    @Test
    void criaTarefaComStatusPendenteEAparecemNaLista() throws Exception {
        String body = """
                {"titulo":"Estudar DDD","descricao":"cap 1","prioridade":1,"data_vencimento":"2026-06-10"}""";

        mvc.perform(post("/tarefas").with(ana()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.prioridade").value(1))
                .andExpect(jsonPath("$.data_vencimento").value("2026-06-10"));

        mvc.perform(get("/tarefas").with(ana()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.titulo=='Estudar DDD')]").exists());
    }

    @Test
    void cadastroSemTituloRetorna400() throws Exception {
        mvc.perform(post("/tarefas").with(ana()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.http_status").value(400))
                .andExpect(jsonPath("$.erros.titulo").isArray());
    }

    @Test
    void usuarioNaoVeTarefasDeOutro() throws Exception {
        RequestPostProcessor bruno =
                jwt().jwt(j -> j.subject("bruno").claim("email", "bruno-cl@x.com").claim("name", "Bruno"));
        mvc.perform(post("/tarefas").with(ana()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Só da Ana\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/tarefas").with(bruno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.titulo=='Só da Ana')]").doesNotExist());
    }
}
