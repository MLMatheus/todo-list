package github.mlmatheus.todolist;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** US4: excluir tarefa. */
class ExcluirTarefaIT extends AbstractIntegrationTest {

    private RequestPostProcessor ana() {
        return jwt().jwt(j -> j.subject("ana").claim("email", "ana-ex@x.com").claim("name", "Ana"));
    }

    private String criar(String titulo) throws Exception {
        String resp = mvc.perform(post("/tarefas").with(ana()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"" + titulo + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asText();
    }

    @Test
    void excluiTarefaESomeDaLista() throws Exception {
        String id = criar("Para excluir");

        mvc.perform(delete("/tarefas/" + id).with(ana()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/tarefas").with(ana()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id=='" + id + "')]").doesNotExist());
    }

    @Test
    void excluirTarefaDeOutroRetorna404() throws Exception {
        String id = criar("Da Ana");
        RequestPostProcessor bruno =
                jwt().jwt(j -> j.subject("bruno").claim("email", "bruno-ex@x.com").claim("name", "Bruno"));

        mvc.perform(delete("/tarefas/" + id).with(bruno))
                .andExpect(status().isNotFound());
    }
}
