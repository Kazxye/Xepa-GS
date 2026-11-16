package br.com.xepasolidaria.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FluxoDeDoacaoIntegrationTest {

    private static final String SENHA = "senhaSegura123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cicloCompletoDeUmaDoacao() throws Exception {
        BigDecimal quilosAntes = new BigDecimal(painel().get("quilosRecuperados").asText());

        String tokenFeirante = cadastrarFeirante();
        String tokenCozinha = cadastrarCozinha(-23.5890, -46.6365);
        String tokenOutraCozinha = cadastrarCozinha(-23.5862, -46.6380);

        JsonNode oferta = executar(post("/api/ofertas"), tokenFeirante, Map.of(
                "tipo", "FRUTA",
                "descricao", "Banana nanica madura",
                "quantidadeKg", 15,
                "horarioLimite", Instant.now().plus(Duration.ofHours(2)).toString()
        ), status().isCreated());
        long ofertaId = oferta.get("id").asLong();
        assertThat(oferta.get("status").asText()).isEqualTo("DISPONIVEL");

        JsonNode proximas = executar(get("/api/ofertas/proximas"), tokenCozinha, null, status().isOk());
        assertThat(proximas.findValuesAsText("id")).contains(String.valueOf(ofertaId));

        JsonNode reservada = executar(post("/api/ofertas/{id}/reserva", ofertaId), tokenCozinha, null,
                status().isOk());
        assertThat(reservada.get("status").asText()).isEqualTo("RESERVADA");

        executar(post("/api/ofertas/{id}/reserva", ofertaId), tokenOutraCozinha, null, status().isConflict());

        executar(post("/api/ofertas/{id}/retirada", ofertaId), tokenCozinha, Map.of("pesoKg", 14.5),
                status().isOk());
        JsonNode concluida = executar(post("/api/ofertas/{id}/entrega", ofertaId), tokenFeirante, null,
                status().isOk());
        assertThat(concluida.get("status").asText()).isEqualTo("RETIRADA");
        assertThat(concluida.at("/reserva/retirada/pesoKg").decimalValue()).isEqualByComparingTo("14.5");

        BigDecimal quilosDepois = new BigDecimal(painel().get("quilosRecuperados").asText());
        assertThat(quilosDepois.subtract(quilosAntes)).isEqualByComparingTo("14.5");
    }

    @Test
    void loginComCredenciaisDeDemonstracao() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "joana@demo.xepa.app", "senha", "xepa2026"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.perfil.perfil").value("FEIRANTE"))
                .andExpect(jsonPath("$.perfil.feirante.nomeBarraca").value("Frutas da Joana"));
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "joana@demo.xepa.app", "senha", "senhaErrada"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotasProtegidasExigemToken() throws Exception {
        mockMvc.perform(get("/api/ofertas/minhas")).andExpect(status().isUnauthorized());
    }

    @Test
    void cozinhaNaoPublicaOferta() throws Exception {
        String tokenCozinha = cadastrarCozinha(-23.5890, -46.6365);

        executar(post("/api/ofertas"), tokenCozinha, Map.of(
                "tipo", "FRUTA",
                "descricao", "Banana",
                "quantidadeKg", 5,
                "horarioLimite", Instant.now().plus(Duration.ofHours(1)).toString()
        ), status().isForbidden());
    }

    @Test
    void emailDuplicadoRetorna409() throws Exception {
        mockMvc.perform(post("/api/auth/cadastro/feirante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dadosFeirante("joana@demo.xepa.app"))))
                .andExpect(status().isConflict());
    }

    @Test
    void cadastroInvalidoRetornaCamposComErro() throws Exception {
        mockMvc.perform(post("/api/auth/cadastro/feirante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "invalido"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.feiraId").exists());
    }

    @Test
    void painelEFeirasSaoPublicos() throws Exception {
        mockMvc.perform(get("/api/feiras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
        mockMvc.perform(get("/api/painel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retiradasConcluidas").isNumber())
                .andExpect(jsonPath("$.porFeira").isArray());
    }

    private String cadastrarFeirante() throws Exception {
        return token(mockMvc.perform(post("/api/auth/cadastro/feirante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dadosFeirante(emailUnico()))))
                .andExpect(status().isCreated())
                .andReturn());
    }

    private String cadastrarCozinha(double latitude, double longitude) throws Exception {
        Map<String, Object> dados = Map.of(
                "nome", "Cozinha Teste",
                "responsavel", "Responsável Teste",
                "email", emailUnico(),
                "senha", SENHA,
                "telefone", "11988887777",
                "endereco", "Rua Teste, 100",
                "latitude", latitude,
                "longitude", longitude,
                "raioBuscaKm", 2.0,
                "capacidadeCargaKg", 50
        );
        return token(mockMvc.perform(post("/api/auth/cadastro/cozinha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dados)))
                .andExpect(status().isCreated())
                .andReturn());
    }

    private Map<String, Object> dadosFeirante(String email) {
        return Map.of(
                "nome", "Feirante Teste",
                "email", email,
                "senha", SENHA,
                "telefone", "11999990000",
                "nomeBarraca", "Barraca Teste",
                "feiraId", 1
        );
    }

    private JsonNode executar(MockHttpServletRequestBuilder requisicao, String token, Object corpo,
                              ResultMatcher statusEsperado) throws Exception {
        requisicao.header("Authorization", "Bearer " + token);
        if (corpo != null) {
            requisicao.contentType(MediaType.APPLICATION_JSON).content(json(corpo));
        }
        MvcResult resultado = mockMvc.perform(requisicao).andExpect(statusEsperado).andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString());
    }

    private JsonNode painel() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/painel")).andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString());
    }

    private String token(MvcResult resultado) throws Exception {
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("token").asText();
    }

    private String json(Object valor) throws Exception {
        return objectMapper.writeValueAsString(valor);
    }

    private static String emailUnico() {
        return "teste-" + UUID.randomUUID() + "@xepa.app";
    }
}
