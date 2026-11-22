package com.vendramel.vendramel.base.domain.Services;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.vendramel.vendramel.base.domain.interfaces.AplicarDescontoInterface;

/**
 * RN04: pix somente à vista com 8% de desconto; cartão à vista com 3% de
 * desconto; cartão em até 3x sem juros. Acima de 3x não é permitido.
 */
public class CalcularDescontoJuros implements AplicarDescontoInterface {

    private static final BigDecimal DESCONTO_CARTAO = new BigDecimal("0.03");
    private static final BigDecimal DESCONTO_PIX = new BigDecimal("0.08");
    private static final int MAXIMO_PARCELAS = 3;

    @Override
    public BigDecimal calcularDesconto(
            BigDecimal valorOriginal,
            String tipo,
            int parcelas) {

        if (valorOriginal == null || valorOriginal.signum() <= 0) {
            throw new IllegalArgumentException("Valor inválido.");
        }
        if (parcelas < 1) {
            throw new IllegalArgumentException("Parcelas inválidas.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Forma de pagamento inválida.");
        }

        BigDecimal valorFinal;

        switch (tipo.toUpperCase()) {
            case "CARTAO":
                if (parcelas == 1) {
                    valorFinal = valorOriginal.multiply(
                            BigDecimal.ONE.subtract(DESCONTO_CARTAO));
                } else if (parcelas <= MAXIMO_PARCELAS) {
                    valorFinal = valorOriginal;
                } else {
                    throw new IllegalArgumentException(
                            "Parcelamento acima de 3x não é permitido (RN04).");
                }
                break;

            case "PIX":
                if (parcelas != 1) {
                    throw new IllegalArgumentException("PIX somente à vista.");
                }
                valorFinal = valorOriginal.multiply(
                        BigDecimal.ONE.subtract(DESCONTO_PIX));
                break;

            default:
                throw new IllegalArgumentException("Forma de pagamento inválida.");
        }

        return valorFinal.setScale(2, RoundingMode.HALF_UP);
    }
}
