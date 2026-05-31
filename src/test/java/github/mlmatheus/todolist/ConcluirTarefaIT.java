package github.mlmatheus.todolist;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** US2: concluir e reabrir tarefa (status no PATCH), idempotente, com isolamento por dono. */
class ConcluirTarefaIT extends AbstractIntegrationTest {

    private RequestPostProcessor ana() {
        return jwt().jwt(j -> j.subject("ana").claim("email", "ana-cc@x.com").claim("name", "Ana"));
    }

    private String criar() throws Exception {
        String resp = mvc.perform(post("/tarefas").with(ana()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Tarefa\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asText();
    }

    @Test
    void concluiReabreEIdempotente() throws Exception {
        String id = criar();

        mvc.perform(patch("/tarefas/" + id).with(ana()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONCLUIDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));

        // idempotente
        mvc.perform(patch("/tarefas/" + id).with(ana()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONCLUIDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));

        // reabrir
        mvc.perform(patch("/tarefas/" + id).with(ana()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PENDENTE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void naoConcluiTarefaDeOutroUsuario() throws Exception {
        String id = criar();
        RequestPostProcessor bruno =
                jwt().jwt(j -> j.subject("bruno").claim("email", "bruno-cc@x.com").claim("name", "Bruno"));

        mvc.perform(patch("/tarefas/" + id).with(bruno).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONCLUIDA\"}"))
                .andExpect(status().isNotFound());
    }
}
