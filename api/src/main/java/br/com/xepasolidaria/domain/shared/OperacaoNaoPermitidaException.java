package br.com.xepasolidaria.domain.shared;

public class OperacaoNaoPermitidaException extends RegraDeNegocioException {

    public OperacaoNaoPermitidaException(String mensagem) {
        super(mensagem);
    }
}
