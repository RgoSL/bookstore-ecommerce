package com.vendramel.vendramel.base.domain.regras;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * RN02: o preço de cada formato é calculado de forma diferente.
 * Capa dura: sem desconto. Brochura: 5%. Digital: 10%.
 */
public final class RegraPreco {

    private static final BigDecimal DESCONTO_BROCHURA = new BigDecimal("0.05");
    private static final BigDecimal DESCONTO_DIGITAL = new BigDecimal("0.10");

    private RegraPreco() {
    }

    public static BigDecimal aplicar(String formato, BigDecimal precoVenda) {
        if (precoVenda == null) {
            throw new IllegalArgumentException("Preço de venda inválido.");
        }
        if (formato == null) {
            throw new IllegalArgumentException("Formato de livro inválido.");
        }
        BigDecimal desconto;
        switch (formato.trim().toUpperCase()) {
            case FormatoLivro.CAPA_DURA:
                desconto = BigDecimal.ZERO;
                break;
            case FormatoLivro.BROCHURA:
                desconto = DESCONTO_BROCHURA;
                break;
            case FormatoLivro.DIGITAL:
                desconto = DESCONTO_DIGITAL;
                break;
            default:
                throw new IllegalArgumentException("Formato de livro inválido.");
        }
        return precoVenda
                .multiply(BigDecimal.ONE.subtract(desconto))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
