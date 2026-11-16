package br.com.xepasolidaria.web;

import br.com.xepasolidaria.application.auth.CredenciaisInvalidasException;
import br.com.xepasolidaria.domain.shared.ConflitoException;
import br.com.xepasolidaria.domain.shared.OperacaoNaoPermitidaException;
import br.com.xepasolidaria.domain.shared.RecursoNaoEncontradoException;
import br.com.xepasolidaria.domain.shared.RegraDeNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail dadosInvalidos(MethodArgumentNotValidException excecao) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : excecao.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Revise os campos destacados e tente novamente.");
        problema.setProperty("campos", campos);
        return problema;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail requisicaoMalFormada() {
        return problema(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "Não foi possível ler os dados enviados. Verifique os formatos e valores.");
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ProblemDetail credenciaisInvalidas(CredenciaisInvalidasException excecao) {
        return problema(HttpStatus.UNAUTHORIZED, "Não autorizado", excecao.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail acessoNegado() {
        return problema(HttpStatus.FORBIDDEN, "Acesso negado", "Seu perfil não tem permissão para esta ação.");
    }

    @ExceptionHandler(OperacaoNaoPermitidaException.class)
    public ProblemDetail operacaoNaoPermitida(OperacaoNaoPermitidaException excecao) {
        return problema(HttpStatus.FORBIDDEN, "Acesso negado", excecao.getMessage());
    }

    @ExceptionHandler({RecursoNaoEncontradoException.class})
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException excecao) {
        return problema(HttpStatus.NOT_FOUND, "Não encontrado", excecao.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail rotaInexistente() {
        return problema(HttpStatus.NOT_FOUND, "Não encontrado", "O endereço solicitado não existe.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail metodoNaoSuportado() {
        return problema(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido",
                "Este endereço não aceita o método utilizado.");
    }

    @ExceptionHandler(ConflitoException.class)
    public ProblemDetail conflito(ConflitoException excecao) {
        return problema(HttpStatus.CONFLICT, "Conflito", excecao.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail edicaoSimultanea() {
        return problema(HttpStatus.CONFLICT, "Conflito",
                "Esta oferta acabou de ser alterada por outra pessoa. Atualize a página e tente novamente.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail violacaoDeIntegridade(DataIntegrityViolationException excecao) {
        LOG.warn("Violação de integridade: {}", excecao.getMostSpecificCause().getMessage());
        return problema(HttpStatus.CONFLICT, "Conflito",
                "A operação conflita com dados já registrados. Atualize a página e tente novamente.");
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ProblemDetail regraDeNegocio(RegraDeNegocioException excecao) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "Operação não realizada", excecao.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail erroInesperado(Exception excecao) {
        LOG.error("Erro inesperado", excecao);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente em instantes.");
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
