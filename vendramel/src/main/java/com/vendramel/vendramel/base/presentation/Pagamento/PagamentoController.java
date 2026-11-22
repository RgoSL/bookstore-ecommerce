package com.vendramel.vendramel.base.presentation.Pagamento;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.vendramel.vendramel.base.application.usecase.pagamento.CalcularDescontoUseCase;
import com.vendramel.vendramel.base.application.usecase.pagamento.CriarPagamentoUseCase;
import com.vendramel.vendramel.base.domain.entidades.Pagamento;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final CriarPagamentoUseCase criarPagamentoUseCase;
    private final CalcularDescontoUseCase calcularDescontoUseCase;

    public PagamentoController(
            CriarPagamentoUseCase criarPagamentoUseCase,
            CalcularDescontoUseCase calcularDescontoUseCase) {

        this.criarPagamentoUseCase = criarPagamentoUseCase;
        this.calcularDescontoUseCase = calcularDescontoUseCase;
    }

    @PostMapping
    public ResponseEntity<Pagamento> criarPagamento(
            @RequestBody CriarPagamentoRequest request) {

        Pagamento pagamento = criarPagamentoUseCase.executar(
                request.pedidoId(),
                request.tipo(),
                request.parcelas()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(pagamento);
    }

    /**
     * Mostra o valor final (RN04) antes de pagar. Não grava nada.
     */
    @GetMapping("/simulacao")
    public ResponseEntity<SimulacaoResponse> simular(
            @RequestParam BigDecimal valor,
            @RequestParam String tipo,
            @RequestParam int parcelas) {

        BigDecimal valorFinal =
                calcularDescontoUseCase.executar(valor, tipo, parcelas);

        BigDecimal valorParcela = valorFinal.divide(
                BigDecimal.valueOf(parcelas), 2, java.math.RoundingMode.HALF_UP);

        return ResponseEntity.ok(
                new SimulacaoResponse(valorFinal, parcelas, valorParcela));
    }

    public record CriarPagamentoRequest(
            int pedidoId,
            String tipo,
            int parcelas
    ) {}

    public record SimulacaoResponse(
            BigDecimal valorFinal,
            int parcelas,
            BigDecimal valorParcela
    ) {}
}
