package br.com.xepasolidaria.domain.shared;

public class ConflitoException extends RegraDeNegocioException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
