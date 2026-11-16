package br.com.xepasolidaria.application.feira;

import br.com.xepasolidaria.domain.feira.FeiraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeiraService {

    private final FeiraRepository feiraRepository;

    public FeiraService(FeiraRepository feiraRepository) {
        this.feiraRepository = feiraRepository;
    }

    @Transactional(readOnly = true)
    public List<FeiraView> listar() {
        return feiraRepository.findAllByOrderByNomeAsc().stream()
                .map(FeiraView::de)
                .toList();
    }
}
