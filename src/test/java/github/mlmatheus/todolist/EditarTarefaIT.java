package github.mlmatheus.todolist;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** US3: editar conteúdo da tarefa. */
class EditarTarefaIT extends AbstractIntegrationTest {

    private RequestPostProcessor ana() {
        return jwt().jwt(j -> j.subject("ana").claim("email", "ana-ed@x.com").claim("name", "Ana"));
    }

    private String criar() throws Exception {
        String resp = mvc.perform(post("/tarefas").with(ana()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Original\",\"prioridade\":3}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asText();
    }

    @Test
    void editaTituloEPrioridade() throws Exception {
        String id = criar();

        mvc.perform(patch("/tarefas/" + id).with(ana()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Atualizado\",\"prioridade\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Atualizado"))
                .andExpect(jsonPath("$.prioridade").value(1));
    }

    @Test
    void editarComTituloVazioRetorna400() throws Exception {
        String id = criar();

        mvc.perform(patch("/tarefas/" + id).with(ana()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.titulo").isArray());
    }

    @Test
    void editarTarefaDeOutroRetorna404() throws Exception {
        String id = criar();
        RequestPostProcessor bruno =
                jwt().jwt(j -> j.subject("bruno").claim("email", "bruno-ed@x.com").claim("name", "Bruno"));

        mvc.perform(patch("/tarefas/" + id).with(bruno).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Hack\"}"))
                .andExpect(status().isNotFound());
    }
}
