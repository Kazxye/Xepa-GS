package br.com.xepasolidaria.infrastructure.scheduling;

import br.com.xepasolidaria.application.oferta.OfertaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpiracaoDeOfertasJob {

    private static final Logger LOG = LoggerFactory.getLogger(ExpiracaoDeOfertasJob.class);

    private final OfertaService ofertaService;

    public ExpiracaoDeOfertasJob(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
    }

    @Scheduled(fixedDelayString = "${xepa.expiracao.intervalo:PT1M}", initialDelayString = "${xepa.expiracao.intervalo:PT1M}")
    public void expirarOfertasVencidas() {
        int expiradas = ofertaService.expirarVencidas();
        if (expiradas > 0) {
            LOG.info("{} oferta(s) marcada(s) como expirada(s)", expiradas);
        }
    }
}
