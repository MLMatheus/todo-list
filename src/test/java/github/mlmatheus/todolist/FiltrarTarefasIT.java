package github.mlmatheus.todolist;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** US5: filtrar tarefas por status, prioridade e data de vencimento (combináveis). */
class FiltrarTarefasIT extends AbstractIntegrationTest {

    private RequestPostProcessor user() {
        return jwt().jwt(j -> j.subject("filtro").claim("email", "filtro@x.com").claim("name", "Filtro"));
    }

    private String criar(String titulo, int prioridade, String venc) throws Exception {
        String body = "{\"titulo\":\"" + titulo + "\",\"prioridade\":" + prioridade
                + ",\"data_vencimento\":\"" + venc + "\"}";
        String resp = mvc.perform(post("/tarefas").with(user()).contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asText();
    }

    @BeforeEach
    void seed() throws Exception {
        criar("A", 1, "2026-06-01");
        String b = criar("B", 2, "2026-06-02");
        criar("C", 1, "2026-06-01");
        mvc.perform(patch("/tarefas/" + b).with(user()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONCLUIDA\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void filtraPorStatus() throws Exception {
        mvc.perform(get("/tarefas").param("status", "CONCLUIDA").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.titulo=='B')]").exists())
                .andExpect(jsonPath("$.content[?(@.status=='PENDENTE')]").doesNotExist());
    }

    @Test
    void filtraPorPrioridade() throws Exception {
        mvc.perform(get("/tarefas").param("prioridade", "1").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.prioridade==2)]").doesNotExist());
    }

    @Test
    void filtraPorDataVencimento() throws Exception {
        mvc.perform(get("/tarefas").param("data_vencimento", "2026-06-02").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.titulo=='B')]").exists())
                .andExpect(jsonPath("$.content[?(@.titulo=='A')]").doesNotExist());
    }

    @Test
    void combinaFiltros() throws Exception {
        mvc.perform(get("/tarefas").param("status", "PENDENTE").param("prioridade", "1").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.titulo=='B')]").doesNotExist())
                .andExpect(jsonPath("$.content[?(@.titulo=='A')]").exists());
    }

    @Test
    void semCorrespondenciaRetornaPaginaVazia() throws Exception {
        mvc.perform(get("/tarefas").param("prioridade", "3").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
