package github.mlmatheus.todolist.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import github.mlmatheus.todolist.infrastructure.constants.Log;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class RequestLoggingFilterTest {

    @Test
    void populaRequestIdEDepoisLimpaMdc() throws Exception {
        RequestLoggingFilter filter = new RequestLoggingFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        String[] capturado = new String[1];
        FilterChain chain = (req, res) -> capturado[0] = MDC.get(Log.REQUEST_ID);

        filter.doFilterInternal(request, response, chain);

        assertThat(capturado[0]).isNotBlank();
        assertThat(MDC.get(Log.REQUEST_ID)).isNull();
    }

    @Test
    void limpaMdcMesmoComExcecao() {
        RequestLoggingFilter filter = new RequestLoggingFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = (req, res) -> {
            throw new RuntimeException("falha");
        };

        try {
            filter.doFilterInternal(request, response, chain);
        } catch (Exception ignored) {
            // esperado
        }

        assertThat(MDC.get(Log.REQUEST_ID)).isNull();
        verify(request, org.mockito.Mockito.never()).getAttribute("x");
    }
}
